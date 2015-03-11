/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.n52.io.ConfigTaskPrerendering.ConfiguredStyle;
import static org.n52.io.IoParameters.GRID;
import static org.n52.io.IoParameters.HEIGHT;
import static org.n52.io.IoParameters.LOCALE;
import static org.n52.io.IoParameters.PHENOMENON;
import static org.n52.io.IoParameters.TIMESPAN;
import static org.n52.io.IoParameters.WIDTH;
import org.n52.io.format.TvpDataCollection;
import org.n52.io.img.ChartDimension;
import org.n52.io.img.RenderingContext;
import static org.n52.io.img.RenderingContext.createContextForSingleTimeseries;
import org.n52.io.v1.data.PhenomenonOutput;
import org.n52.io.v1.data.StyleProperties;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.n52.io.v1.data.UndesignedParameterSet;
import static org.n52.io.v1.data.UndesignedParameterSet.createForSingleTimeseries;
import org.n52.sensorweb.v1.spi.ParameterService;
import org.n52.sensorweb.v1.spi.TimeseriesDataService;
import org.n52.web.ResourceNotFoundException;
import org.n52.web.v1.ctrl.Stopwatch;
import static org.n52.web.v1.ctrl.Stopwatch.startStopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletConfigAware;

public class PreRenderingTask implements ServletConfigAware {

    private final static Logger LOGGER = LoggerFactory.getLogger(PreRenderingTask.class);

    private static final String TASK_CONFIG_FILE = "/config-task-prerendering.json";

    private ParameterService<TimeseriesMetadataOutput> timeseriesMetadataService;

    private TimeseriesDataService timeseriesDataService;

    private final ConfigTaskPrerendering taskConfigPrerendering;

    private final RenderTask taskToRun;

    private String webappFolder;

    private String outputPath;

    private int periodInMinutes;

    private boolean enabled;

    private int width = 800;
    private int height = 500;
    private String language = "en";
    private boolean showGrid = true;

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

    private ConfigTaskPrerendering readTaskConfig() {
        InputStream taskConfig = getClass().getResourceAsStream(TASK_CONFIG_FILE);
        try {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(taskConfig, ConfigTaskPrerendering.class);
        }
        catch (IOException e) {
            LOGGER.error("Could not load {}. Using empty config.", TASK_CONFIG_FILE, e);
            return new ConfigTaskPrerendering();
        }
        finally {
            if (taskConfig != null) {
                try {
                    taskConfig.close();
                }
                catch (IOException e) {
                    LOGGER.debug("Stream already closed.");
                }
            }
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
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

    public boolean hasPrerenderedImage(String timeseriesId, String interval) {
        File name = createFileName(timeseriesId, interval);
        return name.exists();
    }

    public void writePrerenderedGraphToOutputStream(String timeseriesId,
                                                    String interval,
                                                    OutputStream outputStream) {
        try {
            BufferedImage image = loadImage(timeseriesId, interval);
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

    private BufferedImage loadImage(String timeseriesId, String interval) throws IOException {
        return ImageIO.read(new FileInputStream(createFileName(timeseriesId, interval)));
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

    private FileOutputStream createFile(String timeseriesId, String interval) throws IOException {
        File file = createFileName(timeseriesId, interval);
        if (file.exists()) {
            file.setLastModified(new Date().getTime());
        } else {
            file.createNewFile();
        }
        return new FileOutputStream(file);
    }

    private File createFileName(String timeseriesId, String interval) {
        String outputDirectory = getOutputFolder();
        String filename = timeseriesId + "_" + interval + ".png";
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

    private IoParameters createConfig(String interval) {
        Map<String, String> configuration = new HashMap<String, String>();

        // for backward compatibility (from xml config)
        configuration.put(WIDTH, Integer.toString(width));
        configuration.put(HEIGHT, Integer.toString(height));
        configuration.put(GRID, Boolean.toString(showGrid));
        configuration.put(TIMESPAN, interval);
        configuration.put(LOCALE, language);

        // overrides the above parameters (from json config)
        configuration.putAll(taskConfigPrerendering.getGeneralConfig());
        this.width = Integer.parseInt(configuration.get(WIDTH));
        this.height = Integer.parseInt(configuration.get(HEIGHT));
        this.showGrid = Boolean.parseBoolean(configuration.get(GRID));
        this.language = configuration.get(LOCALE);

        return IoParameters.createFromQuery(configuration);
    }

    private TvpDataCollection getTimeseriesData(UndesignedParameterSet parameters) {
        Stopwatch stopwatch = startStopwatch();
        TvpDataCollection timeseriesData = timeseriesDataService.getTimeseriesData(parameters);
        LOGGER.debug("Processing request took {} seconds.", stopwatch.stopInSeconds());
        return timeseriesData;
    }

    private final class RenderTask extends TimerTask {

        @Override
        public void run() {
            LOGGER.info("Start prerendering task");
            try {
                Map<String, ConfiguredStyle> phenomenonStyles = taskConfigPrerendering.getPhenomenonStyles();
                Map<String, ConfiguredStyle> timeseriesStyles = taskConfigPrerendering.getTimeseriesStyles();
                for (String phenomenonId : phenomenonStyles.keySet()) {
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put(PHENOMENON, phenomenonId);
                    IoParameters query = IoParameters.createFromQuery(parameters);
                    TimeseriesMetadataOutput[] metadatas = timeseriesMetadataService.getCondensedParameters(query);
                    for (TimeseriesMetadataOutput metadata : metadatas) {
                        String timeseriesId = metadata.getId();
                        ConfiguredStyle style = timeseriesStyles.containsKey(timeseriesId)
                            ? timeseriesStyles.get(timeseriesId)
                            : phenomenonStyles.get(phenomenonId);

                        renderConfiguredIntervals(timeseriesId, style);
                    }
                }

                for (String timeseriesId : timeseriesStyles.keySet()) {
                    TimeseriesMetadataOutput metadata = timeseriesMetadataService.getParameter(timeseriesId);
                    PhenomenonOutput phenomenon = metadata.getParameters().getPhenomenon();
                    if (!phenomenonStyles.containsKey(phenomenon.getId())) {
                        // overridden phenomena styles have been rendered already
                        ConfiguredStyle style = timeseriesStyles.get(timeseriesId);
                        renderConfiguredIntervals(timeseriesId, style);
                    }
                }
            }
            catch (IOException e) {
                LOGGER.error("Error while reading prerendering configuration file!", e);
            }
        }

        private void renderConfiguredIntervals(String timeseriesId, ConfiguredStyle style) throws IOException {
            for (String interval : style.getInterval()) {
                renderWithStyle(timeseriesId, style.getStyle(), interval);
            }
        }

        private void renderWithStyle(String timeseriesId, StyleProperties style, String interval) throws IOException {
            IntervalWithTimeZone timespan = createTimespanFromInterval(timeseriesId, interval);
            IoParameters config = createConfig(timespan.toString());

            TimeseriesMetadataOutput metadata = timeseriesMetadataService.getParameter(timeseriesId, config);
            RenderingContext context = createContextForSingleTimeseries(metadata, style, timespan);
            context.setDimensions(new ChartDimension(width, height));
            UndesignedParameterSet parameters = createForSingleTimeseries(timeseriesId, config);
            IoHandler renderer = IoFactory
                    .createWith(config)
                    .createIOHandler(context);
            FileOutputStream fos = createFile(timeseriesId, interval);
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
