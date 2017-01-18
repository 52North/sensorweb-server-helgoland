/**
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.n52.io.PreRenderingTaskConfig.RenderingConfig;
import org.n52.io.format.TvpDataCollection;
import org.n52.io.img.ChartDimension;
import org.n52.io.img.RenderingContext;
import static org.n52.io.img.RenderingContext.createContextForSingleTimeseries;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.n52.io.v1.data.UndesignedParameterSet;
import static org.n52.io.v1.data.UndesignedParameterSet.createForSingleTimeseries;
import static org.n52.sensorweb.v1.spi.GeneralizingTimeseriesDataService.composeDataService;
import org.n52.sensorweb.v1.spi.ParameterService;
import org.n52.sensorweb.v1.spi.TimeseriesDataService;
import org.n52.web.ResourceNotFoundException;
import org.n52.web.v1.ctrl.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletConfigAware;

public class PreRenderingTask implements ServletConfigAware {

    private final static Logger LOGGER = LoggerFactory.getLogger(PreRenderingTask.class);

    private static final String TASK_CONFIG_FILE = "/config-task-prerendering.json";
    
    private static final int WIDTH_DEFAULT = 800;
    private static final int HEIGHT_DEFAULT = 500;
    private static final String LANGUAGE_DEFAULT = "en";
    private static final boolean GRID_DEFAULT = true;
    private static final boolean LEGEND_DEFAULT = false;
    private static final boolean GENERALIZE_DEFAULT = false;

    private ParameterService<TimeseriesMetadataOutput> timeseriesMetadataService;

    private TimeseriesDataService timeseriesDataService;

    private final PreRenderingTaskConfig taskConfigPrerendering;

    private final RenderTask taskToRun;

    private String webappFolder;

    private String outputPath;

    private int periodInMinutes;

    private boolean enabled;

    // factory method
    public static PreRenderingTask createTask() {
        return new PreRenderingTask();
    }

    // destroy method
    public void shutdownTask() {
        this.enabled = false;
        this.taskToRun.cancel();
        LOGGER.info("Render task successfully shutted down.");
    }

    PreRenderingTask() {
        this.taskToRun = new RenderTask();
        this.taskConfigPrerendering = readTaskConfig();
    }

    private PreRenderingTaskConfig readTaskConfig() {
        try (InputStream taskConfig = getClass().getResourceAsStream(TASK_CONFIG_FILE)) {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(taskConfig, PreRenderingTaskConfig.class);
        }
        catch (IOException e) {
            LOGGER.error("Could not load {}. Using empty config.", TASK_CONFIG_FILE, e);
            return new PreRenderingTaskConfig();
        }
    }

    public void startTask() {
        if (taskToRun != null) {
            this.enabled = true;
            Timer timer = new Timer("Prerender charts timer task");
            timer.schedule(taskToRun, 10000, getPeriodInMilliseconds());
        }
    }

    private int getPeriodInMilliseconds() {
        return 1000 * 60 * periodInMinutes;
    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        webappFolder = servletConfig.getServletContext().getRealPath("/");
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public ParameterService<TimeseriesMetadataOutput> getTimeseriesMetadataService() {
        return timeseriesMetadataService;
    }

    public void setTimeseriesMetadataService(ParameterService<TimeseriesMetadataOutput> timeseriesMetadataService) {
        this.timeseriesMetadataService = timeseriesMetadataService;
    }

    public TimeseriesDataService getTimeseriesDataService() {
        return timeseriesDataService;
    }

    public void setTimeseriesDataService(TimeseriesDataService timeseriesDataService) {
        this.timeseriesDataService = timeseriesDataService;
    }

    public int getPeriodInMinutes() {
        return periodInMinutes;
    }

    public void setPeriodInMinutes(int period) {
        this.periodInMinutes = period;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            startTask();
        }
    }

    public boolean hasPrerenderedImage(String timeseriesId, String chartQualifier) {
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
            ImageIO.write(image, "png", outputStream);
        }
        catch (IOException e) {
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
        }
        else if (period.equals("lastWeek")) {
            Interval interval = new Interval(now.minusWeeks(1), now);
            return new IntervalWithTimeZone(interval.toString());
        }
        else if (period.equals("lastMonth")) {
            Interval interval = new Interval(now.minusMonths(1), now);
            return new IntervalWithTimeZone(interval.toString());
        }
        else {
            throw new ResourceNotFoundException("Unknown interval definition '" + period + "' for timeseriesId "
                    + timeseriesId);
        }
    }

    private FileOutputStream createFile(String timeseriesId, String interval, String postfix) throws IOException {
        String chartQualifier = postfix != null
                ? interval + "_" + postfix
                : interval;
        File file = createFileName(timeseriesId, chartQualifier);
        if (file.exists()) {
            file.setLastModified(new Date().getTime());
        } else {
            file.createNewFile();
        }
        return new FileOutputStream(file);
    }

    private File createFileName(String timeseriesId, String chartQualifier) {
        String outputDirectory = getOutputFolder();
        String filename = timeseriesId + "_" + chartQualifier + ".png";
        return new File(outputDirectory + filename);
    }

    private String getOutputFolder() {
        String outputDirectory = webappFolder + File.separator + outputPath + File.separator;
        File dir = new File(outputDirectory);
        if ( !dir.exists()) {
            dir.mkdirs();
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
        
        try {
            ObjectMapper om = new ObjectMapper();
            configuration.put("style", om.writeValueAsString(renderingConfig.getStyle()));
            configuration.put("title", renderingConfig.getTitle());
        } catch (JsonProcessingException e) {
            LOGGER.warn("Invalid rendering style.", e);
        }

        return IoParameters.createFromQuery(configuration);
    }

    private TvpDataCollection getTimeseriesData(UndesignedParameterSet parameters) {
        return parameters.isGeneralize()
            ? composeDataService(timeseriesDataService).getTimeseriesData(parameters)
            : timeseriesDataService.getTimeseriesData(parameters);
    }

    private final class RenderTask extends TimerTask {

        @Override
        public void run() {
            Stopwatch stopwatch = Stopwatch.startStopwatch();
            LOGGER.info("Start prerendering task at '{}'", new DateTime(stopwatch.getStartInMillis()));
            List<RenderingConfig> phenomenonStyles = taskConfigPrerendering.getPhenomenonStyles();
            List<RenderingConfig> timeseriesStyles = taskConfigPrerendering.getTimeseriesStyles();
            for (RenderingConfig config : phenomenonStyles) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put("phenomenon", config.getId());
                IoParameters query = IoParameters.createFromQuery(parameters);
                TimeseriesMetadataOutput[] metadatas = timeseriesMetadataService.getCondensedParameters(query);
                for (TimeseriesMetadataOutput metadata : metadatas) {
                    String timeseriesId = metadata.getId();

                    renderConfiguredIntervals(timeseriesId, config);
                }
            }

            for (RenderingConfig config : timeseriesStyles) {
                renderConfiguredIntervals(config.getId(), config);
            }
            
            DateTime startedAt = new DateTime(stopwatch.getStartInMillis());
            LOGGER.info("Finished prerendering task (took '{}'s). Next run at '{}'", 
                    stopwatch.stopInSeconds(), startedAt.plus(getPeriodInMilliseconds()).toString());
        }

        private void renderConfiguredIntervals(String timeseriesId, RenderingConfig renderingConfig) {
            for (String interval : renderingConfig.getInterval()) {
                try {
                    renderWithStyle(timeseriesId, renderingConfig, interval);
                } catch (Throwable e) {
                    LOGGER.error("Error occured while prerendering timeseries {}.", timeseriesId, e);
                }
            }
        }

        private void renderWithStyle(String timeseriesId, RenderingConfig renderingConfig, String interval) throws IOException {
            IntervalWithTimeZone timespan = createTimespanFromInterval(timeseriesId, interval);
            IoParameters config = createConfig(timespan.toString(), renderingConfig);

            TimeseriesMetadataOutput metadata = timeseriesMetadataService.getParameter(timeseriesId, config);
            RenderingContext context = createContextForSingleTimeseries(metadata, config);
            int width = context.getChartStyleDefinitions().getWidth();
            int height = context.getChartStyleDefinitions().getHeight();
            context.setDimensions(new ChartDimension(width, height));
            UndesignedParameterSet parameters = createForSingleTimeseries(timeseriesId, config);
            IoHandler renderer = IoFactory
                    .createWith(config)
                    .createIOHandler(context);
            String chartQualifier = renderingConfig.getChartQualifier();
            FileOutputStream fos = createFile(timeseriesId, interval, chartQualifier);
            renderChartFile(renderer, parameters, fos);
        }

        private void renderChartFile(IoHandler renderer, UndesignedParameterSet parameters, FileOutputStream fos) {
            try {
                renderer.generateOutput(getTimeseriesData(parameters));
                renderer.encodeAndWriteTo(fos);
            }
            catch (IoParseException e) {
                LOGGER.error("Image creation occures error.", e);
            }
            finally {
                try {
                    fos.flush();
                    fos.close();
                }
                catch (IOException e) {
                    LOGGER.error("File stream already flushed/closed.", e);
                }
            }
        }
    }
}
