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

package org.n52.web.ctrl.v1.ext;

import static org.n52.io.MimeType.APPLICATION_PDF;
import static org.n52.io.MimeType.APPLICATION_ZIP;
import static org.n52.io.MimeType.TEXT_CSV;
import static org.n52.io.measurement.format.FormatterFactory.createFormatterFactory;
import static org.n52.io.measurement.img.RenderingContext.createContextForSingleTimeseries;
import static org.n52.io.measurement.img.RenderingContext.createContextWith;
import static org.n52.io.request.IoParameters.createFromQuery;
import static org.n52.io.request.QueryParameters.createFromQuery;
import static org.n52.io.request.RequestSimpleParameterSet.createForSingleTimeseries;
import static org.n52.io.request.RequestSimpleParameterSet.createFromDesignedParameters;
import static org.n52.sensorweb.spi.GeneralizingMeasurementDataService.composeDataService;
import static org.n52.web.common.Stopwatch.startStopwatch;
import static org.n52.web.ctrl.v1.ext.ExtUrlSettings.COLLECTION_SERIES;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
import org.n52.io.IoHandler;
import org.n52.io.IoParseException;
import org.n52.io.PreRenderingJob;
import org.n52.io.measurement.MeasurementIoFactory;
import org.n52.io.measurement.img.RenderingContext;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.series.MeasurementData;
import org.n52.io.response.series.MeasurementSeriesOutput;
import org.n52.io.response.series.SeriesDataCollection;
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
@RequestMapping(value = COLLECTION_SERIES, produces = {"application/json"})
public class MeasurementSeriesDataController extends BaseController {

    private final static Logger LOGGER = LoggerFactory.getLogger(MeasurementSeriesDataController.class);

    private ParameterService<MeasurementSeriesOutput> metadataService;

    private SeriesDataService<MeasurementData> dataService;

    private PreRenderingJob preRenderingTask;

    private String requestIntervalRestriction;

    @RequestMapping(value = "/data", produces = {"application/json"}, method = POST)
    public ModelAndView getSeriesCollectionData(HttpServletResponse response,
                                                @RequestBody RequestSimpleParameterSet parameters) throws Exception {

        checkForUnknownSeriesIds(parameters.getSeriesIds());
        if (parameters.isSetRawFormat()) {
            getRawSeriesCollectionData(response, parameters);
            return null;
        }

        SeriesDataCollection<MeasurementData> data = getSeriesData(parameters);
        SeriesDataCollection< ? > formattedDataCollection = format(data, parameters.getFormat());
        return new ModelAndView().addObject(formattedDataCollection.getAllSeries());
    }

    @RequestMapping(value = "/{observationType}/{seriesId}/data", produces = {"application/json"}, method = GET)
    public ModelAndView getSeriesData(HttpServletResponse response,
                                      @PathVariable String observationType,
                                      @PathVariable String seriesId,
                                      @RequestParam(required = false) MultiValueMap<String, String> query) {

        seriesId = qualifySeriesId(observationType, seriesId);
        checkForUnknownSeriesIds(seriesId);

        IoParameters map = createFromQuery(query);
        IntervalWithTimeZone timespan = map.getTimespan();
        checkAgainstTimespanRestriction(timespan.toString());
        RequestSimpleParameterSet parameters = createForSingleTimeseries(seriesId, map);
        if (map.getResultTime() != null) {
            parameters.setResultTime(map.getResultTime().toString());
        }

        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        // TODO add paging
        SeriesDataCollection<MeasurementData> data = getSeriesData(parameters);
        SeriesDataCollection< ? > formattedDataCollection = format(data, map.getFormat());
        if (map.isExpanded()) {
            return new ModelAndView().addObject(formattedDataCollection.getAllSeries());
        }
        Object formattedTimeseries = formattedDataCollection.getAllSeries().get(seriesId);
        return new ModelAndView().addObject(formattedTimeseries);
    }

    private String qualifySeriesId(String observationType, String seriesId) {
        return observationType + "/" + seriesId;
    }

    @RequestMapping(value = "/data", method = POST, params = {RawFormats.RAW_FORMAT})
    public void getRawSeriesCollectionData(HttpServletResponse response,
                                           @RequestBody RequestSimpleParameterSet parameters) throws Exception {
        checkForUnknownSeriesIds(parameters.getSeriesIds());
        if ( !dataService.supportsRawData()) {
            throw new BadRequestException("Querying of raw timeseries data is not supported by the underlying service!");
        }

        try (InputStream inputStream = dataService.getRawDataService().getRawData(parameters)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("No raw data found.");
            }
            IOUtils.copyLarge(inputStream, response.getOutputStream());
        }
        catch (IOException e) {
            throw new InternalServerException("Error while querying raw data", e);
        }
    }

    @RequestMapping(value = "/{observationType}/{seriesId}/data", method = GET, params = {RawFormats.RAW_FORMAT})
    public void getRawSeriesData(HttpServletResponse response,
                                 @PathVariable String observationType,
                                 @PathVariable String seriesId,
                                 @RequestParam MultiValueMap<String, String> query) {

        seriesId = qualifySeriesId(observationType, seriesId);
        checkForUnknownSeriesIds(seriesId);
        IoParameters map = createFromQuery(query);
        RequestSimpleParameterSet parameters = createForSingleTimeseries(seriesId, map);
        if ( !dataService.supportsRawData()) {
            throw new BadRequestException("Querying of raw procedure data is not supported by the underlying service!");
        }
        try (InputStream inputStream = dataService.getRawDataService().getRawData(parameters)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("No raw data found for id '" + seriesId + "'.");
            }
            IOUtils.copyLarge(inputStream, response.getOutputStream());
        }
        catch (IOException e) {
            throw new InternalServerException("Error while querying raw data", e);
        }
    }

    private SeriesDataCollection< ? > format(SeriesDataCollection<MeasurementData> timeseriesData, String format) {
        return createFormatterFactory(format).create().format(timeseriesData);
    }

    @RequestMapping(value = "/data", produces = {"application/pdf"}, method = POST)
    public void getSeriesCollectionReport(HttpServletResponse response,
                                          @RequestBody RequestStyledParameterSet requestParameters) throws Exception {

        checkForUnknownSeriesIds(requestParameters.getSeriesIds());

        IoParameters map = createFromQuery(requestParameters);
        RequestSimpleParameterSet parameters = createFromDesignedParameters(requestParameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        String[] timeseriesIds = parameters.getSeriesIds();
        OutputCollection<MeasurementSeriesOutput> timeseriesMetadatas = metadataService.getParameters(timeseriesIds,
                                                                                                      map);
        RenderingContext context = createContextWith(requestParameters, timeseriesMetadatas.getItems());

        IoHandler<MeasurementData> renderer = MeasurementIoFactory.createWith(map).forMimeType(APPLICATION_PDF).withServletContextRoot(getRootResource()).createIOHandler(context);

        handleBinaryResponse(response, parameters, renderer);

    }

    private URI getRootResource() throws URISyntaxException, MalformedURLException {
        return getServletConfig().getServletContext().getResource("/").toURI();
    }

    @RequestMapping(value = "/{observationType}/{seriesId}/data", produces = {"application/pdf"}, method = GET)
    public void getSeriesReport(HttpServletResponse response,
                                @PathVariable String observationType,
                                @PathVariable String seriesId,
                                @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {

        seriesId = qualifySeriesId(observationType, seriesId);
        checkForUnknownSeriesIds(seriesId);

        IoParameters map = createFromQuery(query);
        MeasurementSeriesOutput metadata = metadataService.getParameter(seriesId, map);
        RequestSimpleParameterSet parameters = createForSingleTimeseries(seriesId, map);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        RenderingContext context = createContextForSingleTimeseries(metadata, map);
        IoHandler<MeasurementData> renderer = MeasurementIoFactory.createWith(map).forMimeType(APPLICATION_PDF).withServletContextRoot(getRootResource()).createIOHandler(context);

        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/{observationType}/{seriesId}/data", produces = {"application/zip"}, method = GET)
    public void getSeriesAsZippedCsv(HttpServletResponse response,
                                     @PathVariable String observationType,
                                     @PathVariable String seriesId,
                                     @RequestParam(required = false) MultiValueMap<String, String> query)
                                             throws Exception {
        query.put("zip", Arrays.asList(new String[] {Boolean.TRUE.toString()}));
        getSeriesAsCsv(response, observationType, seriesId, query);
    }

    @RequestMapping(value = "/{observationType}/{seriesId}/data", produces = {"text/csv"}, method = GET)
    public void getSeriesAsCsv(HttpServletResponse response,
                              @PathVariable String observationType,
                              @PathVariable String seriesId,
                              @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {

        seriesId = qualifySeriesId(observationType, seriesId);
        checkForUnknownSeriesIds(seriesId);

        IoParameters map = createFromQuery(query);
        MeasurementSeriesOutput metadata = metadataService.getParameter(seriesId, map);
        RequestSimpleParameterSet parameters = createForSingleTimeseries(seriesId, map);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        RenderingContext context = createContextForSingleTimeseries(metadata, map);
        IoHandler<MeasurementData> renderer = MeasurementIoFactory.createWith(map).forMimeType(TEXT_CSV).createIOHandler(context);

        response.setCharacterEncoding("UTF-8");
        if (Boolean.parseBoolean(map.getOther("zip"))) {
            response.setContentType(APPLICATION_ZIP.toString());
        }
        else {
            response.setContentType(TEXT_CSV.toString());
        }
        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/data", produces = {"image/png"}, method = POST)
    public void getSeriesCollectionChart(HttpServletResponse response,
                                         @RequestBody RequestStyledParameterSet requestParameters) throws Exception {

        checkForUnknownSeriesIds(requestParameters.getSeriesIds());

        IoParameters map = createFromQuery(requestParameters);
        RequestSimpleParameterSet parameters = createFromDesignedParameters(requestParameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());
        parameters.setBase64(map.isBase64());

        String[] timeseriesIds = parameters.getSeriesIds();
        OutputCollection<MeasurementSeriesOutput> timeseriesMetadatas = metadataService.getParameters(timeseriesIds,
                                                                                                      map);
        RenderingContext context = createContextWith(requestParameters, timeseriesMetadatas.getItems());
        IoHandler<MeasurementData> renderer = MeasurementIoFactory.createWith(map).createIOHandler(context);

        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/{observationType}/{seriesId}/data", produces = {"image/png"}, method = GET)
    public void getSeriesChart(HttpServletResponse response,
                               @PathVariable String observationType,
                               @PathVariable String seriesId,
                               @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {

        seriesId = qualifySeriesId(observationType, seriesId);
        checkForUnknownSeriesIds(seriesId);

        IoParameters map = createFromQuery(query);
        MeasurementSeriesOutput metadata = metadataService.getParameter(seriesId, map);
        RenderingContext context = createContextForSingleTimeseries(metadata, map);
        context.setDimensions(map.getChartDimension());

        RequestSimpleParameterSet parameters = createForSingleTimeseries(seriesId, map);
        checkAgainstTimespanRestriction(parameters.getTimespan());

        parameters.setGeneralize(map.isGeneralize());
        parameters.setBase64(map.isBase64());
        parameters.setExpanded(map.isExpanded());

        IoHandler<MeasurementData> renderer = MeasurementIoFactory.createWith(map).createIOHandler(context);
        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/{observationType}/{seriesId}/{chartQualifier}", produces = {"image/png"}, method = GET)
    public void getSeriesChartByInterval(HttpServletResponse response,
                                         @PathVariable String seriesId,
                                         @PathVariable String chartQualifier,
                                         @RequestParam(required = false) MultiValueMap<String, String> query)
                                                 throws Exception {
        if (preRenderingTask == null) {
            throw new ResourceNotFoundException("Diagram prerendering is not enabled.");
        }
        if ( !preRenderingTask.hasPrerenderedImage(seriesId, chartQualifier)) {
            throw new ResourceNotFoundException("No pre-rendered chart found for timeseries '" + seriesId + "'.");
        }
        preRenderingTask.writePrerenderedGraphToOutputStream(seriesId, chartQualifier, response.getOutputStream());
    }

    private void checkAgainstTimespanRestriction(String timespan) {
        Duration duration = Period.parse(requestIntervalRestriction).toDurationFrom(new DateTime());
        if (duration.getMillis() < Interval.parse(timespan).toDurationMillis()) {
            throw new BadRequestException("Requested timespan is to long, please use a period shorter than '"
                    + requestIntervalRestriction + "'");
        }
    }

    private void checkForUnknownSeriesIds(String... seriesIds) {
        for (String id : seriesIds) {
            if ( !metadataService.exists(id)) {
                throw new ResourceNotFoundException("The series with id '" + id + "' was not found.");
            }
        }
    }

    /**
     * @param response
     *        the response to write binary on.
     * @param parameters
     *        the timeseries parameter to request raw data.
     * @param renderer
     *        an output renderer.
     * @throws InternalServerException
     *         if data processing fails for some reason.
     */
    private void handleBinaryResponse(HttpServletResponse response,
                                      RequestSimpleParameterSet parameters,
                                      IoHandler<MeasurementData> renderer) {
        try {
            renderer.generateOutput(getSeriesData(parameters));
            if (parameters.isBase64()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                renderer.encodeAndWriteTo(baos);
                byte[] imageData = baos.toByteArray();
                byte[] encode = Base64.encodeBase64(imageData);
                response.getOutputStream().write(encode);
            }
            else {
                renderer.encodeAndWriteTo(response.getOutputStream());
            }
        }
        catch (IOException e) { // handled by BaseController
            throw new InternalServerException("Error handling output stream.", e);
        }
        catch (IoParseException e) { // handled by BaseController
            throw new InternalServerException("Could not write binary to stream.", e);
        }
    }

    private SeriesDataCollection<MeasurementData> getSeriesData(RequestSimpleParameterSet parameters) {
        Stopwatch stopwatch = startStopwatch();
        SeriesDataCollection<MeasurementData> timeseriesData = parameters.isGeneralize()
            ? composeDataService(dataService).getSeriesData(parameters)
            : dataService.getSeriesData(parameters);
        LOGGER.debug("Processing request took {} seconds.", stopwatch.stopInSeconds());
        return timeseriesData;
    }

    public ParameterService<MeasurementSeriesOutput> getMetadataService() {
        return metadataService;
    }

    public void setMetadataService(ParameterService<MeasurementSeriesOutput> seriesMetadataService) {
        this.metadataService = new WebExceptionAdapter<>(seriesMetadataService);
    }

    public SeriesDataService<MeasurementData> getDataService() {
        return dataService;
    }

    public void setDataService(SeriesDataService<MeasurementData> seriesDataService) {
        this.dataService = seriesDataService;
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
