/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.n52.io.PrerenderingJobConfig.RenderingConfig;
import org.n52.io.quantity.img.ChartDimension;
import org.n52.io.request.IoParameters;
import org.n52.io.request.QueryParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.dataset.TimeseriesMetadataOutput;
import org.n52.io.response.dataset.quantity.QuantityData;
import org.n52.io.response.dataset.quantity.QuantityDatasetOutput;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.io.task.ScheduledJob;
import org.n52.series.spi.srv.DataService;
import org.n52.series.spi.srv.ParameterService;
import org.n52.web.common.Stopwatch;
import org.n52.web.exception.ResourceNotFoundException;
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
    @Qualifier("timeseriesService")
    private ParameterService<TimeseriesMetadataOutput> timeseriesMetadataService;

    @Autowired
    @Qualifier("timeseriesService")
    private DataService<QuantityData> timeseriesDataService;

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
        List<RenderingConfig> timeseriesStyles = taskConfigPrerendering.getTimeseriesStyles();
        for (RenderingConfig config : phenomenonStyles) {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("phenomenon", config.getId());
            IoParameters query = QueryParameters.createFromQuery(parameters);
            OutputCollection<TimeseriesMetadataOutput> metadatas = timeseriesMetadataService
                        .getCondensedParameters(query);
            for (TimeseriesMetadataOutput metadata : metadatas) {
                String timeseriesId = metadata.getId();
//                RenderingConfig style = timeseriesStyles.containsKey(timeseriesId)
//                    ? timeseriesStyles.get(timeseriesId)
//                    : phenomenonStyles.get(phenomenonId);
                renderConfiguredIntervals(timeseriesId, config);

                if (interrupted) {
                    return;
                }
            }
        }

        for (RenderingConfig config : timeseriesStyles) {
//            RenderingConfig style = timeseriesStyles.get(timeseriesId);
            renderConfiguredIntervals(config.getId(), config);

            if (interrupted) {
                return;
            }
        }

        LOGGER.debug("prerendering took '{}'", stopwatch.stopInSeconds());
    }

    private void renderConfiguredIntervals(String timeseriesId, RenderingConfig style) {
        try {
            for (String interval : style.getInterval()) {
                renderWithStyle(timeseriesId, style, interval);
            }
        } catch (Throwable e) {
            LOGGER.error("Error occured while prerendering timeseries {}.", timeseriesId, e);
        }
    }

    private void renderWithStyle(String timeseriesId, RenderingConfig renderingConfig, String interval)
                throws IOException, DatasetFactoryException, URISyntaxException {
        IntervalWithTimeZone timespan = createTimespanFromInterval(timeseriesId, interval);
        IoParameters config = createConfig(timespan.toString(), renderingConfig);

        TimeseriesMetadataOutput metadata = timeseriesMetadataService.getParameter(timeseriesId, config);
        IoStyleContext context = IoStyleContext.createContextForSingleSeries(metadata, config);
        int width = context.getChartStyleDefinitions().getWidth();
        int height = context.getChartStyleDefinitions().getHeight();
        context.setDimensions(new ChartDimension(width, height));

        RequestSimpleParameterSet parameters = RequestSimpleParameterSet.createForSingleSeries(timeseriesId, config);


        String chartQualifier = renderingConfig.getChartQualifier();
        FileOutputStream fos = createFile(timeseriesId, interval, chartQualifier);

        try (FileOutputStream out = fos;) {
            createIoFactory(parameters)
                .createHandler(IMAGE_EXTENSION)
                .writeBinary(out);
            fos.flush();
        } catch (IoHandlerException | IOException e) {
            LOGGER.error("Image creation occures error.", e);
        }
    }

    private IoFactory<QuantityData, TimeseriesMetadataOutput, QuantityValue>
            createIoFactory(RequestSimpleParameterSet parameters)
            throws DatasetFactoryException, URISyntaxException, MalformedURLException {
        return new DefaultIoFactory<QuantityData, TimeseriesMetadataOutput, QuantityValue>()
                .create(QuantityDatasetOutput.DATASET_TYPE)
                .withSimpleRequest(parameters)
                .withDataService(timeseriesDataService)
                .withDatasetService(timeseriesMetadataService);
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        interrupted = true;
        LOGGER.info("Marked job to interrupt.");
    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        webappFolder = servletConfig.getServletContext().getRealPath("/");
    }

    public String getConfigFile() {
        return configFile;
    }

    public void setConfigFile(String configFile) {
        this.configFile = configFile;
    }

    public ParameterService<TimeseriesMetadataOutput> getTimeseriesMetadataService() {
        return timeseriesMetadataService;
    }

    public void setTimeseriesMetadataService(ParameterService<TimeseriesMetadataOutput> timeseriesMetadataService) {
        this.timeseriesMetadataService = timeseriesMetadataService;
    }

    public DataService<QuantityData> getTimeseriesDataService() {
        return timeseriesDataService;
    }

    public void setTimeseriesDataService(DataService<QuantityData> timeseriesDataService) {
        this.timeseriesDataService = timeseriesDataService;
    }

    public boolean hasPrerenderedImage(String timeseriesId, String chartQualifier) {
        taskConfigPrerendering = readJobConfig(configFile);
        File fileName = createFileName(timeseriesId, chartQualifier);
        return fileName.exists();
    }

    public void writePrerenderedGraphToOutputStream(String timeseriesId,
            String chartQualifier,
            OutputStream outputStream) {
        try {
            BufferedImage image = loadImage(timeseriesId, chartQualifier);
            if (image == null) {
                ResourceNotFoundException ex = new ResourceNotFoundException("Could not find image on server.");
                ex.addHint("Perhaps the image is being rendered at the moment. Try again later.");
                throw ex;
            }
            ImageIO.write(image, IMAGE_EXTENSION, outputStream);
        } catch (IOException e) {
            LOGGER.error("Error while loading pre rendered image", e);
        }
    }

    private BufferedImage loadImage(String timeseriesId, String chartQualifier) throws IOException {
        return ImageIO.read(new FileInputStream(createFileName(timeseriesId, chartQualifier)));
    }

    public IntervalWithTimeZone createTimespanFromInterval(String timeseriesId, String period) {
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
            throw new ResourceNotFoundException("Unknown interval definition '" + period + "' for timeseriesId "
                    + timeseriesId);
        }
    }

    private FileOutputStream createFile(String timeseriesId, String interval, String postfix) throws IOException {
        String chartQualifier = postfix != null
                ? interval + "_" + postfix
                : interval;
        File file = createFileName(timeseriesId, chartQualifier);
        if (file.exists() && !file.setLastModified(new Date().getTime())) {
            LOGGER.debug("Can't set last modified date at '{}'", file.getAbsolutePath());
        } else {
            if (!file.createNewFile()) {
                LOGGER.warn("Can't create file '{}'", file.getAbsolutePath());
            }
        }
        return new FileOutputStream(file);
    }

    private File createFileName(String timeseriesId, String chartQualifier) {
        String outputDirectory = getOutputFolder();
        String filename = timeseriesId + "_" + chartQualifier + ".png";
        return new File(outputDirectory + filename);
    }

    private String getOutputFolder() {
        final Map<String, String> generalConfig = taskConfigPrerendering.getGeneralConfig();
        String outputPath = generalConfig.get("outputPath");
        String outputDirectory = webappFolder + File.separator + outputPath + File.separator;
        File dir = new File(outputDirectory);
        if (!dir.exists() && !dir.mkdirs()) {
            LOGGER.warn("Unable to create output folder '{}'.", outputDirectory);
        }
        return outputDirectory;
    }

    private IoParameters createConfig(String interval, RenderingConfig renderingConfig) {
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
            configuration.put("style", om.writeValueAsString(renderingConfig.getStyle()));
            configuration.put("title", renderingConfig.getTitle());
        } catch (JsonProcessingException e) {
            LOGGER.warn("Invalid rendering style.", e);
        }

        return QueryParameters.createFromQuery(configuration);
    }


}
