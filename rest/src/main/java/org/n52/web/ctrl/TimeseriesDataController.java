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

package org.n52.web.ctrl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.n52.io.DatasetFactoryException;
import org.n52.io.DefaultIoFactory;
import org.n52.io.IntervalWithTimeZone;
import org.n52.io.IoFactory;
import org.n52.io.IoHandlerException;
import org.n52.io.MimeType;
import org.n52.io.PreRenderingJob;
import org.n52.io.format.FormatterFactory;
import org.n52.io.generalize.quantity.GeneralizingQuantityService;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.request.QueryParameters;
import org.n52.io.request.RequestParameterSet;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.quantity.QuantityData;
import org.n52.io.response.dataset.quantity.QuantityDatasetOutput;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.series.spi.srv.DataService;
import org.n52.series.spi.srv.ParameterService;
import org.n52.series.spi.srv.RawDataService;
import org.n52.series.spi.srv.RawFormats;
import org.n52.web.common.RequestUtils;
import org.n52.web.common.Stopwatch;
import org.n52.web.exception.BadRequestException;
import org.n52.web.exception.InternalServerException;
import org.n52.web.exception.ResourceNotFoundException;
import org.n52.web.exception.WebExceptionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Deprecated
@RestController
@RequestMapping(value = UrlSettings.COLLECTION_TIMESERIES, produces = {
    "application/json"
})
public class TimeseriesDataController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeseriesDataController.class);

    private ParameterService<QuantityDatasetOutput> timeseriesMetadataService;

    private DataService<QuantityData> timeseriesDataService;

    private PreRenderingJob preRenderingTask;

    private String requestIntervalRestriction;

    @RequestMapping(value = "/getData",
        produces = {
            "application/json"
        },
        method = RequestMethod.POST)
    public ModelAndView getCollectionData(HttpServletResponse response,
                                          @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                              required = false) String locale,
                                          @RequestBody RequestSimpleParameterSet query)
            throws Exception {
        RequestUtils.overrideQueryLocaleWhenSet(locale, query);
        checkIfUnknownTimeseries(query, query.getDatasets());
        if (query.isSetRawFormat()) {
            getRawCollectionData(response, locale, query);
            return null;
        }
        DataCollection<QuantityData> seriesData = getTimeseriesData(query);
        DataCollection< ? > formattedDataCollection = format(seriesData, query.getFormat());
        return new ModelAndView().addObject(formattedDataCollection.getAllSeries());
    }

    @RequestMapping(value = "/{timeseriesId}/getData",
        produces = {
            "application/json"
        },
        method = RequestMethod.GET)
    public ModelAndView getData(HttpServletResponse response,
                                @PathVariable String timeseriesId,
                                @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                    required = false) String locale,
                                @RequestParam(required = false) MultiValueMap<String, String> query) {
        RequestUtils.overrideQueryLocaleWhenSet(locale, query);
        IoParameters map = QueryParameters.createFromQuery(query);
        checkIfUnknownTimeseries(map, timeseriesId);

        IntervalWithTimeZone timespan = map.getTimespan();
        checkAgainstTimespanRestriction(timespan.toString());
        RequestSimpleParameterSet parameters = RequestSimpleParameterSet.createForSingleSeries(timeseriesId, map);
        if (map.getResultTime() != null) {
            parameters.setResultTime(map.getResultTime()
                                        .toString());
        }

        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        // TODO add paging
        DataCollection<QuantityData> seriesData = getTimeseriesData(parameters);
        DataCollection< ? > formattedDataCollection = format(seriesData, map.getFormat());
        if (map.isExpanded()) {
            return new ModelAndView().addObject(formattedDataCollection.getAllSeries());
        }
        Object formattedTimeseries = formattedDataCollection.getAllSeries()
                                                            .get(timeseriesId);
        return new ModelAndView().addObject(formattedTimeseries);
    }

    private DataCollection<QuantityData> getTimeseriesData(RequestSimpleParameterSet parameters) {
        Stopwatch stopwatch = Stopwatch.startStopwatch();
        DataCollection<QuantityData> timeseriesData = parameters.isGeneralize()
                ? new GeneralizingQuantityService(timeseriesDataService).getData(parameters)
                : timeseriesDataService.getData(parameters);
        LOGGER.debug("Processing request took {} seconds.", stopwatch.stopInSeconds());
        return timeseriesData;
    }

    @RequestMapping(value = "/getData",
        method = RequestMethod.POST,
        params = {
            RawFormats.RAW_FORMAT
        })
    public void getRawCollectionData(HttpServletResponse response,
                                     @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                         required = false) String locale,
                                     @RequestBody RequestSimpleParameterSet query)
            throws Exception {
        RequestUtils.overrideQueryLocaleWhenSet(locale, query);
        checkIfUnknownTimeseries(query, query.getDatasets());
        processRawDataRequest(response, query);
    }

    @RequestMapping(value = "/{timeseriesId}/getData",
        method = RequestMethod.GET,
        params = {
            RawFormats.RAW_FORMAT
        })
    public void getRawData(HttpServletResponse response,
                           @PathVariable String timeseriesId,
                           @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                               required = false) String locale,
                           @RequestParam MultiValueMap<String, String> query) {
        RequestUtils.overrideQueryLocaleWhenSet(locale, query);
        IoParameters map = QueryParameters.createFromQuery(query);
        checkIfUnknownTimeseries(map, timeseriesId);
        RequestSimpleParameterSet parameters = RequestSimpleParameterSet.createForSingleSeries(timeseriesId, map);
        processRawDataRequest(response, parameters);
    }

    private void processRawDataRequest(HttpServletResponse response,
                                       RequestSimpleParameterSet query) {
        if (!timeseriesDataService.supportsRawData()) {
            throwNewRawDataQueryNotSupportedException();
        }
        final RawDataService rawDataService = timeseriesDataService.getRawDataService();
        try (InputStream inputStream = rawDataService.getRawData(query)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("No raw data found.");
            }
            response.setContentType(query.getFormat());
            IOUtils.copyLarge(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new InternalServerException("Error while querying raw data", e);
        }
    }

    private DataCollection< ? > format(DataCollection<QuantityData> timeseriesData, String format) {
        return FormatterFactory.createFormatterFactory(format)
                               .create()
                               .format(timeseriesData);
    }

    @RequestMapping(value = "/getData",
        produces = {
            "application/pdf"
        },
        method = RequestMethod.POST)
    public void getCollectionReport(HttpServletResponse response,
                                    @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                        required = false) String locale,
                                    @RequestBody RequestStyledParameterSet query)
            throws Exception {
        RequestUtils.overrideQueryLocaleWhenSet(locale, query);
        IoParameters map = QueryParameters.createFromQuery(query);
        checkIfUnknownTimeseries(map, query.getDatasets());

        RequestSimpleParameterSet parameterSet = map.mergeToSimpleParameterSet(query);
        checkAgainstTimespanRestriction(parameterSet.getTimespan());
        parameterSet.setGeneralize(map.isGeneralize());
        parameterSet.setExpanded(map.isExpanded());

        response.setContentType(MimeType.APPLICATION_PDF.getMimeType());
        createIoFactory(parameterSet).withStyledRequest(map.mergeToStyledParameterSet(query))
                                     .createHandler(MimeType.APPLICATION_PDF.getMimeType())
                                     .writeBinary(response.getOutputStream());
    }

    private IoFactory<QuantityData,
                      QuantityDatasetOutput,
                      QuantityValue> createIoFactory(RequestSimpleParameterSet parameters)
                              throws DatasetFactoryException, URISyntaxException, MalformedURLException {
        return createDefaultIoFactory().create(QuantityDatasetOutput.VALUE_TYPE)
                                       .withSimpleRequest(parameters)
                                       .withBasePath(getRootResource())
                                       .withDataService(timeseriesDataService)
                                       .withDatasetService(timeseriesMetadataService);
    }

    private DefaultIoFactory<QuantityData, QuantityDatasetOutput, QuantityValue> createDefaultIoFactory() {
        return new DefaultIoFactory<QuantityData, QuantityDatasetOutput, QuantityValue>();
    }

    private URI getRootResource() throws URISyntaxException, MalformedURLException {
        return getServletConfig().getServletContext()
                                 .getResource("/")
                                 .toURI();
    }

    @RequestMapping(value = "/{timeseriesId}/getData",
        produces = {
            "application/pdf"
        },
        method = RequestMethod.GET)
    public void getReport(HttpServletResponse response,
                          @PathVariable String timeseriesId,
                          @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE, required = false) String locale,
                          @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        RequestUtils.overrideQueryLocaleWhenSet(locale, query);
        IoParameters map = QueryParameters.createFromQuery(query);
        checkIfUnknownTimeseries(map, timeseriesId);

        RequestSimpleParameterSet parameters = RequestSimpleParameterSet.createForSingleSeries(timeseriesId, map);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        response.setContentType(MimeType.APPLICATION_PDF.getMimeType());
        createIoFactory(parameters).createHandler(MimeType.APPLICATION_PDF.getMimeType())
                                   .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{timeseriesId}/getData",
        produces = {
            "application/zip"
        },
        method = RequestMethod.GET)
    public void getAsZippedCsv(HttpServletResponse response,
                               @PathVariable String timeseriesId,
                               @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                   required = false) String locale,
                               @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        RequestUtils.overrideQueryLocaleWhenSet(locale, query);
        IoParameters map = QueryParameters.createFromQuery(query)
                                          .extendWith(MimeType.APPLICATION_ZIP.name(), Boolean.TRUE.toString());
        response.setContentType(MimeType.APPLICATION_ZIP.getMimeType());
        getTimeseriesAsCsv(timeseriesId, map, response);
    }

    @RequestMapping(value = "/{timeseriesId}/getData",
        produces = {
            "text/csv"
        },
        method = RequestMethod.GET)
    public void getAsCsv(HttpServletResponse response,
                         @PathVariable String timeseriesId,
                         @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE, required = false) String locale,
                         @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        RequestUtils.overrideQueryLocaleWhenSet(locale, query);
        IoParameters map = QueryParameters.createFromQuery(query);
        getTimeseriesAsCsv(timeseriesId, map, response);
    }

    private void getTimeseriesAsCsv(String timeseriesId, IoParameters query, HttpServletResponse response)
            throws IoHandlerException, DatasetFactoryException, URISyntaxException, MalformedURLException, IOException {
        checkIfUnknownTimeseries(query, timeseriesId);

        RequestSimpleParameterSet parameters = RequestSimpleParameterSet.createForSingleSeries(timeseriesId, query);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(query.isGeneralize());
        parameters.setExpanded(query.isExpanded());

        response.setCharacterEncoding("UTF-8");
        if (Boolean.parseBoolean(query.getOther(MimeType.APPLICATION_ZIP.name()))) {
            response.setContentType(MimeType.APPLICATION_ZIP.toString());
        } else {
            response.setContentType(MimeType.TEXT_CSV.toString());
        }
        createIoFactory(parameters).createHandler(MimeType.TEXT_CSV.toString())
                                   .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/getData",
        produces = {
            "image/png"
        },
        method = RequestMethod.POST)
    public void getCollectionChart(HttpServletResponse response,
                                   @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                       required = false) String locale,
                                   @RequestBody RequestStyledParameterSet query)
            throws Exception {
        RequestUtils.overrideQueryLocaleWhenSet(locale, query);
        IoParameters map = QueryParameters.createFromQuery(query);
        checkIfUnknownTimeseries(map, query.getDatasets());

        RequestSimpleParameterSet parameterSet = map.mergeToSimpleParameterSet(query);
        checkAgainstTimespanRestriction(parameterSet.getTimespan());
        parameterSet.setGeneralize(map.isGeneralize());
        parameterSet.setExpanded(map.isExpanded());
        parameterSet.setBase64(map.isBase64());

        response.setContentType(MimeType.IMAGE_PNG.getMimeType());
        createIoFactory(parameterSet).withStyledRequest(query)
                                     .createHandler(MimeType.IMAGE_PNG.toString())
                                     .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{timeseriesId}/getData",
        produces = {
            "image/png"
        },
        method = RequestMethod.GET)
    public void getChart(HttpServletResponse response,
                         @PathVariable String timeseriesId,
                         @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE, required = false) String locale,
                         @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        RequestUtils.overrideQueryLocaleWhenSet(locale, query);
        IoParameters map = QueryParameters.createFromQuery(query);
        checkIfUnknownTimeseries(map, timeseriesId);

        RequestSimpleParameterSet parameterSet = RequestSimpleParameterSet.createForSingleSeries(timeseriesId, map);
        RequestStyledParameterSet styledParameters = map.toStyledParameterSet();
        checkAgainstTimespanRestriction(parameterSet.getTimespan());

        parameterSet.setGeneralize(map.isGeneralize());
        parameterSet.setBase64(map.isBase64());
        parameterSet.setExpanded(map.isExpanded());

        response.setContentType(MimeType.IMAGE_PNG.getMimeType());
        createIoFactory(parameterSet).withStyledRequest(styledParameters)
                                     .createHandler(MimeType.IMAGE_PNG.toString())
                                     .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{timeseriesId}/{chartQualifier}",
        produces = {
            "image/png"
        },
        method = RequestMethod.GET)
    public void getChartByInterval(HttpServletResponse response,
                                   @PathVariable String seriesId,
                                   @PathVariable String chartQualifier,
                                   @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                       required = false) String locale,
                                   @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        RequestUtils.overrideQueryLocaleWhenSet(locale, query);
        if (preRenderingTask == null) {
            throw new ResourceNotFoundException("Diagram prerendering is not enabled.");
        }
        if (!preRenderingTask.hasPrerenderedImage(seriesId, chartQualifier)) {
            throw new ResourceNotFoundException("No pre-rendered chart found for timeseries '"
                    + seriesId
                    + "'.");
        }
        preRenderingTask.writePrerenderedGraphToOutputStream(seriesId, chartQualifier, response.getOutputStream());
    }

    private void checkAgainstTimespanRestriction(String timespan) {
        Duration duration = Period.parse(requestIntervalRestriction)
                                  .toDurationFrom(new DateTime());
        if (duration.getMillis() < Interval.parse(timespan)
                                           .toDurationMillis()) {
            throw new BadRequestException("Requested timespan is to long, please use a period shorter than '"
                    + requestIntervalRestriction
                    + "'");
        }
    }

    private void checkIfUnknownTimeseries(RequestParameterSet parameters, String... timeseriesIds) {
        checkIfUnknownTimeseries(IoParameters.createFromQuery(parameters), timeseriesIds);
    }

    private void checkIfUnknownTimeseries(IoParameters parameters, String... timeseriesIds) {
        for (String timeseriesId : timeseriesIds) {
            if (!timeseriesMetadataService.exists(timeseriesId, parameters)) {
                throw new ResourceNotFoundException("The timeseries with id '"
                        + timeseriesId
                        + "' was not found.");
            }
        }
    }

    public ParameterService<QuantityDatasetOutput> getTimeseriesMetadataService() {
        return timeseriesMetadataService;
    }

    public void setTimeseriesMetadataService(ParameterService<QuantityDatasetOutput> timeseriesMetadataService) {
        this.timeseriesMetadataService = new WebExceptionAdapter<>(timeseriesMetadataService);
    }

    public DataService<QuantityData> getTimeseriesDataService() {
        return timeseriesDataService;
    }

    public void setTimeseriesDataService(DataService<QuantityData> timeseriesDataService) {
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

    private void throwNewRawDataQueryNotSupportedException() {
        throw new BadRequestException("Querying of raw procedure data is "
                + "not supported by the underlying service!");
    }

}
