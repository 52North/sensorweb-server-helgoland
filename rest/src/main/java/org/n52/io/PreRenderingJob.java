/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.n52.io;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.quartz.InterruptableJob;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.ServletConfigAware;

import org.n52.io.PrerenderingJobConfig.RenderingConfig;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.io.task.ScheduledJob;
import org.n52.series.spi.srv.DataService;
import org.n52.series.spi.srv.ParameterService;
import org.n52.web.common.Stopwatch;
import org.n52.web.exception.ResourceNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PreRenderingJob extends ScheduledJob implements InterruptableJob, ServletConfigAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreRenderingJob.class);

    private static final int WIDTH_DEFAULT = 800;
    private static final int HEIGHT_DEFAULT = 500;
    private static final String LANGUAGE_DEFAULT = "en";
    private static final boolean GRID_DEFAULT = true;
    private static final boolean LEGEND_DEFAULT = false;
    private static final boolean GENERALIZE_DEFAULT = false;

    private static final String JOB_DATA_CONFIG_FILE = "configFile";
    private static final String JOB_DATA_WEBAPP_FOLDER = "webappFolder";
    private static final String IMAGE_EXTENSION = "png";

    @Autowired
    @Qualifier("datasetService")
    // autowired due to quartz job creation
    private ParameterService<DatasetOutput<AbstractValue< ? >>> datasetService;

    @Autowired
    @Qualifier("datasetService")
    // autowired due to quartz job creation
    private DataService<Data<AbstractValue< ? >>> dataService;

    private PrerenderingJobConfig taskConfigPrerendering;

    private String webappFolder;

    private String configFile;

    private boolean interrupted;

    private PrerenderingJobConfig readJobConfig(String file) {
        try (InputStream taskConfig = getClass().getResourceAsStream(file)) {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(taskConfig, PrerenderingJobConfig.class);
        } catch (IOException e) {
            LOGGER.error("Could not load {}. Using empty config.", file, e);
            return new PrerenderingJobConfig();
        }
    }

    @Override
    public JobDetail createJobDetails() {
        return JobBuilder.newJob(PreRenderingJob.class)
                         .withIdentity(getJobName())
                         .withDescription(getJobDescription())
                         .usingJobData(JOB_DATA_CONFIG_FILE, configFile)
                         .usingJobData(JOB_DATA_WEBAPP_FOLDER, webappFolder)
                         .build();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (interrupted) {
            return;
        }

        LOGGER.info("Start prerendering task");
        final Stopwatch stopwatch = Stopwatch.startStopwatch();
        final JobDetail details = context.getJobDetail();
        JobDataMap jobDataMap = details.getJobDataMap();
        taskConfigPrerendering = readJobConfig(jobDataMap.getString(JOB_DATA_CONFIG_FILE));
        webappFolder = jobDataMap.getString(JOB_DATA_WEBAPP_FOLDER);

        List<RenderingConfig> phenomenonStyles = taskConfigPrerendering.getPhenomenonStyles();
        List<RenderingConfig> styles = taskConfigPrerendering.getDatasetStyles();
        for (RenderingConfig config : phenomenonStyles) {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("phenomenon", config.getId());
            IoParameters query = IoParameters.createFromSingleValueMap(parameters);
            for (DatasetOutput< ? > metadata : datasetService.getCondensedParameters(query)) {
                String timeseriesId = metadata.getId();
                renderConfiguredIntervals(timeseriesId, config);
                if (interrupted) {
                    return;
                }
            }
        }

        for (RenderingConfig config : styles) {
            renderConfiguredIntervals(config.getId(), config);

            if (interrupted) {
                return;
            }
        }

        LOGGER.debug("prerendering took '{}'", stopwatch.stopInSeconds());
    }

    private void renderConfiguredIntervals(String datasetId, RenderingConfig style) {
        try {
            for (String interval : style.getInterval()) {
                renderWithStyle(datasetId, style, interval);
            }
        } catch (Throwable e) {
            LOGGER.error("Error occured while prerendering timeseries {}.", datasetId, e);
        }
    }

    private void renderWithStyle(String datasetId, RenderingConfig renderingConfig, String interval)
            throws IOException, DatasetFactoryException, URISyntaxException {
        IntervalWithTimeZone timespan = createTimespanFromInterval(datasetId, interval);
        IoParameters parameters = createConfig(datasetId, timespan.toString(), renderingConfig);

        String chartQualifier = renderingConfig.getChartQualifier();
        FileOutputStream fos = createFile(datasetId, interval, chartQualifier);

        try (FileOutputStream out = fos;) {
            createIoFactory(parameters).createHandler(IMAGE_EXTENSION)
                                       .writeBinary(out);
            fos.flush();
        } catch (IoHandlerException | IOException e) {
            LOGGER.error("Image creation occures error.", e);
        }
    }

    private IoFactory<DatasetOutput<AbstractValue< ? >>,
                      AbstractValue< ? >> createIoFactory(IoParameters parameters)
                              throws DatasetFactoryException, URISyntaxException, MalformedURLException {
        return createDefaultIoFactory().create(QuantityValue.TYPE)
                                       .setParameters(parameters)
                                       .setDataService(dataService)
                                       .setDatasetService(datasetService);
    }

    private DefaultIoFactory<DatasetOutput<AbstractValue< ? >>,
                             AbstractValue< ? >> createDefaultIoFactory() {
        return new DefaultIoFactory<DatasetOutput<AbstractValue< ? >>,
                                    AbstractValue< ? >>();
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        interrupted = true;
        LOGGER.info("Marked job to interrupt.");
    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        webappFolder = servletConfig.getServletContext()
                                    .getRealPath("/");
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public List<String> getPrerenderedImages(final String datasetId) {
        if (taskConfigPrerendering == null) {
            taskConfigPrerendering = readJobConfig(configFile);
        }
        Path outputPath = getOutputFolder();
        ArrayList<String> files = new ArrayList<>();
        File outputDir = outputPath.toFile();
        if (outputDir.isDirectory()) {
            FilenameFilter startsWithIdFilter = (dir, name) -> name.startsWith(datasetId);
            String[] filtered = outputDir.list(startsWithIdFilter);
            if (filtered != null) {
                files.addAll(Arrays.asList(filtered));
            }
        }
        return files;
    }

    public boolean hasPrerenderedImage(String fileName) {
        return hasPrerenderedImage(fileName, null);
    }

    public boolean hasPrerenderedImage(String datasetId, String chartQualifier) {
        return createFileName(datasetId, chartQualifier).exists();
    }

    public void writePrerenderedGraphToOutputStream(String filename, OutputStream outputStream) {
        writePrerenderedGraphToOutputStream(filename, null, outputStream);
    }

    public void writePrerenderedGraphToOutputStream(String datasetId, String qualifier, OutputStream outputStream) {
        if (taskConfigPrerendering == null) {
            taskConfigPrerendering = readJobConfig(configFile);
        }
        try {
            BufferedImage image = loadImage(datasetId, qualifier);
            if (image == null) {
                ResourceNotFoundException ex = new ResourceNotFoundException("Could not find image on server.");
                ex.addHint("Perhaps the image is being rendered at the moment. Try again later.");
                throw ex;
            }
            LOGGER.debug("write prerendered image '{}'", createFileName(datasetId, qualifier));
            ImageIO.write(image, IMAGE_EXTENSION, outputStream);
        } catch (IOException e) {
            LOGGER.error("Error while loading pre rendered image", e);
        }
    }

    private BufferedImage loadImage(String datasetId, String qualifier) throws IOException {
        return ImageIO.read(new FileInputStream(createFileName(datasetId, qualifier)));
    }

    private IntervalWithTimeZone createTimespanFromInterval(String datasetId, String period) {
        DateTime now = new DateTime();
        if (period.equals("lastDay")) {
            Interval interval = new Interval(now.minusDays(1), now);
            return new IntervalWithTimeZone(interval.toString());
        } else if (period.equals("lastWeek")) {
            Interval interval = new Interval(now.minusWeeks(1), now);
            return new IntervalWithTimeZone(interval.toString());
        } else if (period.equals("lastMonth")) {
            Interval interval = new Interval(now.minusMonths(1), now);
            return new IntervalWithTimeZone(interval.toString());
        } else {
            throw new ResourceNotFoundException("Unknown interval '" + period + "' for datatset " + datasetId);
        }
    }

    private FileOutputStream createFile(String datasetId, String interval, String postfix) throws IOException {
        String chartQualifier = postfix != null
                ? interval + "_" + postfix
                : interval;
        File file = createFileName(datasetId, chartQualifier);
        if (!file.exists() && !file.createNewFile()) {
            LOGGER.warn("Can't create file '{}'", file.getAbsolutePath());
        }
        if (!file.setLastModified(new Date().getTime())) {
            LOGGER.debug("Can't set last modified date at '{}'", file.getAbsolutePath());
        }
        return new FileOutputStream(file);
    }

    private File createFileName(String datasetId, String qualifier) {
        if (taskConfigPrerendering == null) {
            taskConfigPrerendering = readJobConfig(configFile);
        }
        Path outputDirectory = getOutputFolder();
        String filename = qualifier != null
                ? datasetId + "_" + qualifier
                : datasetId;
        return outputDirectory.resolve(filename + ".png")
                              .toFile();
    }

    private Path getOutputFolder() {
        final Map<String, String> generalConfig = taskConfigPrerendering.getGeneralConfig();
        String outputPath = generalConfig.get("outputPath");
        Path outputDirectory = Paths.get(webappFolder)
                                    .resolve(outputPath);
        File dir = outputDirectory.toFile();
        if (!dir.exists() && !dir.mkdirs()) {
            LOGGER.warn("Unable to create output folder '{}'.", outputDirectory);
        }
        return outputDirectory;
    }

    private IoParameters createConfig(String datasetId, String interval, RenderingConfig renderingConfig) {
        Map<String, String> configuration = new HashMap<>();

        // set defaults
        configuration.put("width", Integer.toString(WIDTH_DEFAULT));
        configuration.put("height", Integer.toString(HEIGHT_DEFAULT));
        configuration.put("grid", Boolean.toString(GRID_DEFAULT));
        configuration.put("legend", Boolean.toString(LEGEND_DEFAULT));
        configuration.put("generalize", Boolean.toString(GENERALIZE_DEFAULT));
        configuration.put("locale", LANGUAGE_DEFAULT);
        configuration.put("timespan", interval);

        // overrides the above defaults (from json config)
        configuration.putAll(taskConfigPrerendering.getGeneralConfig());
        if (renderingConfig.getConfig() != null) {
            configuration.putAll(renderingConfig.getConfig());
        }

        // set flag to mark this config comes from prerendering
        configuration.put(IoParameters.RENDERING_TRIGGER, "prerendering");

        try {
            ObjectMapper om = new ObjectMapper();
            configuration.put(Parameters.DATASETS, datasetId);
            configuration.put(Parameters.STYLE, om.writeValueAsString(renderingConfig.getStyle()));
            configuration.put("title", renderingConfig.getTitle());
        } catch (JsonProcessingException e) {
            LOGGER.warn("Invalid rendering style.", e);
        }

        return IoParameters.createFromSingleValueMap(configuration);
    }
}
