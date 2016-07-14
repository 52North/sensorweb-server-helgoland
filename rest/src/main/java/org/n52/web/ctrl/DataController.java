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

package org.n52.web.ctrl;

import static org.n52.io.MimeType.APPLICATION_PDF;
import static org.n52.io.MimeType.APPLICATION_ZIP;
import static org.n52.io.MimeType.TEXT_CSV;
import static org.n52.io.request.QueryParameters.createFromQuery;
import static org.n52.io.request.RequestSimpleParameterSet.createForSingleSeries;
import static org.n52.io.request.RequestSimpleParameterSet.createFromDesignedParameters;
import static org.n52.web.common.Stopwatch.startStopwatch;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.jfree.data.general.DatasetUtilities;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.n52.io.IntervalWithTimeZone;
import org.n52.io.IoHandler;
import org.n52.io.IoParseException;
import org.n52.io.MimeType;
import org.n52.io.PreRenderingJob;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.series.DataCollection;
import org.n52.io.response.v1.ext.ObservationType;
import org.n52.io.v1.data.RawFormats;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.web.common.Stopwatch;
import org.n52.web.exception.BadRequestException;
import org.n52.web.exception.InternalServerException;
import org.n52.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import static org.n52.web.ctrl.UrlSettings.COLLECTION_DATASETS;
import org.n52.sensorweb.spi.DataService;
import static org.n52.io.request.IoParameters.createFromQuery;
import org.n52.io.response.v1.ext.DatasetType;

@RestController
@RequestMapping(value = COLLECTION_DATASETS, produces = {"application/json"})
public class DataController extends BaseController {

    private final static Logger LOGGER = LoggerFactory.getLogger(DataController.class);

    // TODO remove when IO Handler (data specific IO) gets created via factory
    // lean on DataRepositoryFactory plus test and create a common utility
    // loading factory properties
    private Map<ObservationType, DatasetServiceBundle> serviceByDatasetType;

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

        String observationType = parameters.getObservationType();

        DataCollection< ? > data = getSeriesData(parameters);
        DataCollection< ? > formattedDataCollection = format(observationType, data, parameters.getFormat());
        return new ModelAndView().addObject(formattedDataCollection.getAllSeries());
    }

    @RequestMapping(value = "/{seriesId}/data", produces = {"application/json"}, method = GET)
    public ModelAndView getSeriesData(HttpServletResponse response,
                                      @PathVariable String seriesId,
                                      @RequestParam(required = false) MultiValueMap<String, String> query) {

        checkForUnknownSeriesIds(seriesId);

        IoParameters map = createFromQuery(query);
        IntervalWithTimeZone timespan = map.getTimespan();
        checkAgainstTimespanRestriction(timespan.toString());
        RequestSimpleParameterSet parameters = createForSingleSeries(seriesId, map);
        if (map.getResultTime() != null) {
            parameters.setResultTime(map.getResultTime().toString());
        }

        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        DataCollection<?> data = getSeriesData(parameters);
        String observationType = DatasetType.extractType(seriesId);
        DataCollection<?> formattedDataCollection = format(observationType, data, map.getFormat());
        if (map.isExpanded()) {
            return new ModelAndView().addObject(formattedDataCollection.getAllSeries());
        }
        Object formattedTimeseries = formattedDataCollection.getAllSeries().get(seriesId);
        return new ModelAndView().addObject(formattedTimeseries);
    }

    @RequestMapping(value = "/data", method = POST, params = {RawFormats.RAW_FORMAT})
    public void getRawSeriesCollectionData(HttpServletResponse response,
                                           @RequestBody RequestSimpleParameterSet parameters) throws Exception {

        checkForUnknownSeriesIds(parameters.getSeriesIds());

        String observationType = parameters.getObservationType();
        DataService<?> dataService = getDataService(observationType);
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

    @RequestMapping(value = "/{seriesId}/data", method = GET, params = {RawFormats.RAW_FORMAT})
    public void getRawSeriesData(HttpServletResponse response,
                                 @PathVariable String seriesId,
                                 @RequestParam MultiValueMap<String, String> query) {

        checkForUnknownSeriesIds(seriesId);
        IoParameters map = createFromQuery(query);
        RequestSimpleParameterSet parameters = createForSingleSeries(seriesId, map);

        String observationType = DatasetType.extractType(seriesId);
        DataService<?> dataService = getDataService(observationType);
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

    @RequestMapping(value = "/data", produces = {"application/pdf"}, method = POST)
    public void getSeriesCollectionReport(HttpServletResponse response,
                                          @RequestBody RequestStyledParameterSet requestParameters) throws Exception {

        checkForUnknownSeriesIds(requestParameters.getSeriesIds());

        IoParameters map = createFromQuery(requestParameters);
        RequestSimpleParameterSet parameters = createFromDesignedParameters(requestParameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        IoHandler<?> renderer = getIoHandler(parameters.getObservationType(), requestParameters, map, APPLICATION_PDF);
        handleBinaryResponse(response, parameters, renderer);

    }

    private IoHandler<?> getIoHandler(String observationType, RequestStyledParameterSet parameters, IoParameters map) throws MalformedURLException, URISyntaxException {
        return getServiceByObservationType(observationType).getIoHandler(parameters, map);
    }

    private IoHandler<?> getIoHandler(String observationType, RequestStyledParameterSet parameters, IoParameters map,
            MimeType mimeType) throws MalformedURLException, URISyntaxException {
        return getServiceByObservationType(observationType).getIoHandler(parameters, map, mimeType, getRootResource());
    }

    private IoHandler<?> getIoHandler(String observationType, RequestSimpleParameterSet parameters, IoParameters map) throws MalformedURLException, URISyntaxException {
        return getServiceByObservationType(observationType).getIoHandler(parameters, map);
    }

    private IoHandler<?> getIoHandler(String observationType, RequestSimpleParameterSet parameters, IoParameters map,
            MimeType mimeType) throws MalformedURLException, URISyntaxException {
        return getServiceByObservationType(observationType).getIoHandler(parameters, map,
                mimeType, getRootResource());
    }

    @RequestMapping(value = "/{seriesId}/data", produces = {"application/pdf"}, method = GET)
    public void getSeriesReport(HttpServletResponse response,
                                @PathVariable String seriesId,
                                @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {

        checkForUnknownSeriesIds(seriesId);

        IoParameters map = createFromQuery(query);
        RequestSimpleParameterSet parameters = createForSingleSeries(seriesId, map);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        IoHandler<?> renderer = getIoHandler(parameters.getObservationType(), parameters, map, APPLICATION_PDF);
        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/{seriesId}/data", produces = {"application/zip"}, method = GET)
    public void getSeriesAsZippedCsv(HttpServletResponse response,
                                     @PathVariable String seriesId,
                                     @RequestParam(required = false) MultiValueMap<String, String> query)
                                             throws Exception {
        query.put("zip", Arrays.asList(new String[] {Boolean.TRUE.toString()}));
        getSeriesAsCsv(response, seriesId, query);
    }

    @RequestMapping(value = "/{seriesId}/data", produces = {"text/csv"}, method = GET)
    public void getSeriesAsCsv(HttpServletResponse response,
                              @PathVariable String seriesId,
                              @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {

        checkForUnknownSeriesIds(seriesId);

        IoParameters map = createFromQuery(query);
        RequestSimpleParameterSet parameters = createForSingleSeries(seriesId, map);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        String observationType = DatasetType.extractType(seriesId);
        IoHandler<?> renderer = getIoHandler(observationType, parameters, map, TEXT_CSV);

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

        String observationType = parameters.getObservationType();
        IoHandler<?> renderer = getIoHandler(observationType, requestParameters, map);
        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/{seriesId}/data", produces = {"image/png"}, method = GET)
    public void getSeriesChart(HttpServletResponse response,
                               @PathVariable String seriesId,
                               @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {

        checkForUnknownSeriesIds(seriesId);

        IoParameters map = createFromQuery(query);
        RequestSimpleParameterSet parameters = createForSingleSeries(seriesId, map);
        checkAgainstTimespanRestriction(parameters.getTimespan());

        parameters.setGeneralize(map.isGeneralize());
        parameters.setBase64(map.isBase64());
        parameters.setExpanded(map.isExpanded());

        String observationType = DatasetType.extractType(seriesId);
        IoHandler<?> renderer = getIoHandler(observationType, parameters, map);
        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/{seriesId}/{chartQualifier}", produces = {"image/png"}, method = GET)
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

    private URI getRootResource() throws URISyntaxException, MalformedURLException {
        return getServletConfig().getServletContext().getResource("/").toURI();
    }

    private DataCollection< ? > format(String observationType, DataCollection<?> timeseriesData, String format) {
        return getServiceByObservationType(observationType).format(timeseriesData, format);
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

            // TODO abstract from observationType
            ParameterService<?> metadataService = getMetadataService(ObservationType.extractType(id));
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
                                      IoHandler renderer) {
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

    private DataCollection<?> getSeriesData(RequestSimpleParameterSet parameters) {
        Stopwatch stopwatch = startStopwatch();
        DataCollection<?> seriesData = getServiceByObservationType(parameters.getObservationType()).getSeriesData(parameters);
        LOGGER.debug("Processing request took {} seconds.", stopwatch.stopInSeconds());
        return seriesData;
    }

    @SuppressWarnings("rawtypes")
    private ParameterService getMetadataService(String observationType) {
        return getMetadataService(ObservationType.toInstance(observationType));
    }

    @SuppressWarnings("rawtypes")
    private ParameterService getMetadataService(ObservationType observationType) {
        return getServiceByObservationType(observationType).getMetadataService();
    }

    @SuppressWarnings("rawtypes")
    private DataService getDataService(String observationType) {
        return getDataService(ObservationType.toInstance(observationType));
    }

    @SuppressWarnings("rawtypes")
    private DataService getDataService(ObservationType observationType) {
        return getServiceByObservationType(observationType).getDataService();
    }

    private DatasetServiceBundle getServiceByObservationType(String observationType) {
        return getServiceByObservationType(ObservationType.toInstance(observationType));
    }

    private DatasetServiceBundle getServiceByObservationType(ObservationType observationType) {
        return getServiceByObservationType().get(observationType);
    }

    public Map<ObservationType, DatasetServiceBundle> getServiceByObservationType() {
        return serviceByDatasetType;
    }

    public void setServiceByObservationType(Map<ObservationType, DatasetServiceBundle> serviceByObservationType) {
        this.serviceByDatasetType = serviceByObservationType;
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
