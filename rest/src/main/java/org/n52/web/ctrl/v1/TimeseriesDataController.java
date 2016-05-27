/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.web.ctrl.v1;

import static org.n52.io.MimeType.APPLICATION_PDF;
import static org.n52.io.MimeType.APPLICATION_ZIP;
import static org.n52.io.MimeType.TEXT_CSV;
import static org.n52.io.format.FormatterFactory.createFormatterFactory;
import static org.n52.io.img.RenderingContext.createContextForSingleTimeseries;
import static org.n52.io.img.RenderingContext.createContextWith;
import static org.n52.io.request.IoParameters.createFromQuery;
import static org.n52.io.request.QueryParameters.createFromQuery;
import static org.n52.io.request.RequestSimpleParameterSet.createForSingleTimeseries;
import static org.n52.io.request.RequestSimpleParameterSet.createFromDesignedParameters;
import static org.n52.sensorweb.spi.GeneralizingTimeseriesDataService.composeDataService;
import static org.n52.web.common.Stopwatch.startStopwatch;
import static org.n52.web.ctrl.v1.RestfulUrls.COLLECTION_TIMESERIES;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.n52.io.IntervalWithTimeZone;
import org.n52.io.IoFactory;
import org.n52.io.IoHandler;
import org.n52.io.IoParseException;
import org.n52.io.PreRenderingJob;
import org.n52.io.format.TimeseriesDataFormatter;
import org.n52.io.format.TvpDataCollection;
import org.n52.io.img.RenderingContext;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.TimeseriesDataCollection;
import org.n52.io.response.v1.ext.MeasurementSeriesOutput;
import org.n52.io.v1.data.RawFormats;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.sensorweb.spi.SeriesDataService;
import org.n52.web.common.Stopwatch;
import org.n52.web.ctrl.BaseController;
import org.n52.web.exception.BadRequestException;
import org.n52.web.exception.InternalServerException;
import org.n52.web.exception.ResourceNotFoundException;
import org.n52.web.exception.WebExceptionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping(value = COLLECTION_TIMESERIES, produces = {"application/json"})
public class TimeseriesDataController extends BaseController {

    private final static Logger LOGGER = LoggerFactory.getLogger(TimeseriesDataController.class);

    private ParameterService<MeasurementSeriesOutput> timeseriesMetadataService;

    private SeriesDataService timeseriesDataService;

    private PreRenderingJob preRenderingTask;

    private String requestIntervalRestriction;

    @RequestMapping(value = "/getData", produces = {"application/json"}, method = POST)
    public ModelAndView getTimeseriesCollectionData(HttpServletResponse response,
            @RequestBody RequestSimpleParameterSet parameters) throws Exception {

        checkIfUnknownTimeseries(parameters.getSeriesIds());
        if (parameters.isSetRawFormat()) {
            getRawTimeseriesCollectionData(response, parameters);
            return null;
        }

        TvpDataCollection timeseriesData = getTimeseriesData(parameters);
        TimeseriesDataCollection< ?> formattedDataCollection = format(timeseriesData, parameters.getFormat());
        return new ModelAndView().addObject(formattedDataCollection.getTimeseriesOutput());
    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {"application/json"}, method = GET)
    public ModelAndView getTimeseriesData(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query) {

        checkIfUnknownTimeseries(timeseriesId);

        IoParameters map = createFromQuery(query);
        IntervalWithTimeZone timespan = map.getTimespan();
        checkAgainstTimespanRestriction(timespan.toString());
        RequestSimpleParameterSet parameters = createForSingleTimeseries(timeseriesId, map);
        if (map.getResultTime() != null) {
            parameters.setResultTime(map.getResultTime().toString());
        }

        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        // TODO add paging
        TvpDataCollection timeseriesData = getTimeseriesData(parameters);
        TimeseriesDataCollection< ?> formattedDataCollection = format(timeseriesData, map.getFormat());
        if (map.isExpanded()) {
            return new ModelAndView().addObject(formattedDataCollection.getTimeseriesOutput());
        }
        Object formattedTimeseries = formattedDataCollection.getAllTimeseries().get(timeseriesId);
        return new ModelAndView().addObject(formattedTimeseries);
    }

    @RequestMapping(value = "/getData", method = POST, params = {RawFormats.RAW_FORMAT})
    public void getRawTimeseriesCollectionData(HttpServletResponse response, @RequestBody RequestSimpleParameterSet parameters) throws Exception {
        checkIfUnknownTimeseries(parameters.getSeriesIds());
        if (!timeseriesDataService.supportsRawData()) {
            throw new BadRequestException("Querying of raw timeseries data is not supported by the underlying service!");
        }

        try (InputStream inputStream = timeseriesDataService.getRawDataService().getRawData(parameters)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("No raw data found.");
            }
            IOUtils.copyLarge(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new InternalServerException("Error while querying raw data", e);
        }
    }

    @RequestMapping(value = "/{timeseriesId}/getData", method = GET, params = {RawFormats.RAW_FORMAT})
    public void getRawTimeseriesData(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @RequestParam MultiValueMap<String, String> query) {
        checkIfUnknownTimeseries(timeseriesId);
        IoParameters map = createFromQuery(query);
        RequestSimpleParameterSet parameters = createForSingleTimeseries(timeseriesId, map);
        if (!timeseriesDataService.supportsRawData()) {
            throw new BadRequestException("Querying of raw procedure data is not supported by the underlying service!");
        }
        try (InputStream inputStream = timeseriesDataService.getRawDataService().getRawData(parameters)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("No raw data found for id '" + timeseriesId + "'.");
            }
            IOUtils.copyLarge(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new InternalServerException("Error while querying raw data", e);
        }
    }

    private TimeseriesDataCollection< ?> format(TvpDataCollection timeseriesData, String format) {
        TimeseriesDataFormatter< ?> formatter = createFormatterFactory(format).create();
        return formatter.format(timeseriesData);
    }

    @RequestMapping(value = "/getData", produces = {"application/pdf"}, method = POST)
    public void getTimeseriesCollectionReport(HttpServletResponse response,
            @RequestBody RequestStyledParameterSet requestParameters) throws Exception {

        checkIfUnknownTimeseries(requestParameters.getSeriesIds());

        IoParameters map = createFromQuery(requestParameters);
        RequestSimpleParameterSet parameters = createFromDesignedParameters(requestParameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        String[] timeseriesIds = parameters.getSeriesIds();
        OutputCollection<MeasurementSeriesOutput> timeseriesMetadatas = timeseriesMetadataService.getParameters(timeseriesIds, map);
        RenderingContext context = createContextWith(requestParameters, timeseriesMetadatas.getItems());

        IoHandler renderer = IoFactory
                .createWith(map)
                .forMimeType(APPLICATION_PDF)
                .withServletContextRoot(getRootResource())
                .createIOHandler(context);

        handleBinaryResponse(response, parameters, renderer);

    }

    private URI getRootResource() throws URISyntaxException {
        return new URI(getServletConfig().getServletContext().getRealPath("/"));
    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {"application/pdf"}, method = GET)
    public void getTimeseriesReport(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {

        checkIfUnknownTimeseries(timeseriesId);

        IoParameters map = createFromQuery(query);
        MeasurementSeriesOutput metadata = timeseriesMetadataService.getParameter(timeseriesId, map);
        RequestSimpleParameterSet parameters = createForSingleTimeseries(timeseriesId, map);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        RenderingContext context = createContextForSingleTimeseries(metadata, map);
        IoHandler renderer = IoFactory
                .createWith(map)
                .forMimeType(APPLICATION_PDF)
                .withServletContextRoot(getRootResource())
                .createIOHandler(context);

        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {"application/zip"}, method = GET)
    public void getTimeseriesAsZippedCsv(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {
        query.put("zip", Arrays.asList(new String[]{Boolean.TRUE.toString()}));
        getTimeseriesAsCsv(response, timeseriesId, query);
    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {"text/csv"}, method = GET)
    public void getTimeseriesAsCsv(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {

        checkIfUnknownTimeseries(timeseriesId);

        IoParameters map = createFromQuery(query);
        MeasurementSeriesOutput metadata = timeseriesMetadataService.getParameter(timeseriesId, map);
        RequestSimpleParameterSet parameters = createForSingleTimeseries(timeseriesId, map);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        RenderingContext context = createContextForSingleTimeseries(metadata, map);
        IoHandler renderer = IoFactory.createWith(map).forMimeType(TEXT_CSV).createIOHandler(context);

        response.setCharacterEncoding("UTF-8");
        if (Boolean.parseBoolean(map.getOther("zip"))) {
            response.setContentType(APPLICATION_ZIP.toString());
        } else {
            response.setContentType(TEXT_CSV.toString());
        }
        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/getData", produces = {"image/png"}, method = POST)
    public void getTimeseriesCollectionChart(HttpServletResponse response,
            @RequestBody RequestStyledParameterSet requestParameters) throws Exception {

        checkIfUnknownTimeseries(requestParameters.getSeriesIds());

        IoParameters map = createFromQuery(requestParameters);
        RequestSimpleParameterSet parameters = createFromDesignedParameters(requestParameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());
        parameters.setBase64(map.isBase64());

        String[] timeseriesIds = parameters.getSeriesIds();
        OutputCollection<MeasurementSeriesOutput> timeseriesMetadatas = timeseriesMetadataService.getParameters(timeseriesIds, map);
        RenderingContext context = createContextWith(requestParameters, timeseriesMetadatas.getItems());
        IoHandler renderer = IoFactory.createWith(map).createIOHandler(context);

        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {"image/png"}, method = GET)
    public void getTimeseriesChart(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {

        checkIfUnknownTimeseries(timeseriesId);

        IoParameters map = createFromQuery(query);
        MeasurementSeriesOutput metadata = timeseriesMetadataService.getParameter(timeseriesId, map);
        RenderingContext context = createContextForSingleTimeseries(metadata, map);
        context.setDimensions(map.getChartDimension());

        RequestSimpleParameterSet parameters = createForSingleTimeseries(timeseriesId, map);
        checkAgainstTimespanRestriction(parameters.getTimespan());

        parameters.setGeneralize(map.isGeneralize());
        parameters.setBase64(map.isBase64());
        parameters.setExpanded(map.isExpanded());

        IoHandler renderer = IoFactory.createWith(map).createIOHandler(context);
        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/{timeseriesId}/{chartQualifier}", produces = {"image/png"}, method = GET)
    public void getTimeseriesChartByInterval(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @PathVariable String chartQualifier,
            @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {
        if (preRenderingTask == null) {
            throw new ResourceNotFoundException("Diagram prerendering is not enabled.");
        }
        if (!preRenderingTask.hasPrerenderedImage(timeseriesId, chartQualifier)) {
            throw new ResourceNotFoundException("No pre-rendered chart found for timeseries '" + timeseriesId + "'.");
        }
        preRenderingTask.writePrerenderedGraphToOutputStream(timeseriesId, chartQualifier, response.getOutputStream());
    }

    private void checkAgainstTimespanRestriction(String timespan) {
        Duration duration = Period.parse(requestIntervalRestriction).toDurationFrom(new DateTime());
        if (duration.getMillis() < Interval.parse(timespan).toDurationMillis()) {
            throw new BadRequestException("Requested timespan is to long, please use a period shorter than '"
                    + requestIntervalRestriction + "'");
        }
    }

    private void checkIfUnknownTimeseries(String... timeseriesIds) {
        for (String timeseriesId : timeseriesIds) {
            if (!timeseriesMetadataService.exists(timeseriesId)) {
                throw new ResourceNotFoundException("The timeseries with id '" + timeseriesId + "' was not found.");
            }
        }
    }

    /**
     * @param response the response to write binary on.
     * @param parameters the timeseries parameter to request raw data.
     * @param renderer an output renderer.
     * @throws InternalServerException if data processing fails for some reason.
     */
    private void handleBinaryResponse(HttpServletResponse response,
            RequestSimpleParameterSet parameters,
            IoHandler renderer) {
        try {
            renderer.generateOutput(getTimeseriesData(parameters));
            if (parameters.isBase64()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                renderer.encodeAndWriteTo(baos);
                byte[] imageData = baos.toByteArray();
                byte[] encode = Base64.encodeBase64(imageData);
                response.getOutputStream().write(encode);
            } else {
                renderer.encodeAndWriteTo(response.getOutputStream());
            }
        } catch (IOException e) { // handled by BaseController
            throw new InternalServerException("Error handling output stream.", e);
        } catch (IoParseException e) { // handled by BaseController
            throw new InternalServerException("Could not write binary to stream.", e);
        }
    }

    private TvpDataCollection getTimeseriesData(RequestSimpleParameterSet parameters) {
        Stopwatch stopwatch = startStopwatch();
        TvpDataCollection timeseriesData = parameters.isGeneralize()
                ? composeDataService(timeseriesDataService).getSeriesData(parameters)
                : timeseriesDataService.getSeriesData(parameters);
        LOGGER.debug("Processing request took {} seconds.", stopwatch.stopInSeconds());
        return timeseriesData;
    }

    public ParameterService<MeasurementSeriesOutput> getTimeseriesMetadataService() {
        return timeseriesMetadataService;
    }

    public void setTimeseriesMetadataService(ParameterService<MeasurementSeriesOutput> timeseriesMetadataService) {
        this.timeseriesMetadataService = new WebExceptionAdapter<>(timeseriesMetadataService);
    }

    public SeriesDataService getTimeseriesDataService() {
        return timeseriesDataService;
    }

    public void setTimeseriesDataService(SeriesDataService timeseriesDataService) {
        this.timeseriesDataService = timeseriesDataService;
    }

    public PreRenderingJob getPreRenderingTask() {
        return preRenderingTask;
    }

    public void setPreRenderingTask(PreRenderingJob prerenderingTask) {
        this.preRenderingTask = prerenderingTask;
    }

    public String getRequestIntervalRestriction() {
        return requestIntervalRestriction;
    }

    public void setRequestIntervalRestriction(String requestIntervalRestriction) {
        // validate requestIntervalRestriction, if it's no period an exception occured
        Period.parse(requestIntervalRestriction);
        this.requestIntervalRestriction = requestIntervalRestriction;
    }

}
