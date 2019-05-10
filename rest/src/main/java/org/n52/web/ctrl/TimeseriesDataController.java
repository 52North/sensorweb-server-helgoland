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
package org.n52.web.ctrl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.n52.io.Constants;
import org.n52.io.DatasetFactoryException;
import org.n52.io.DefaultIoFactory;
import org.n52.io.IntervalWithTimeZone;
import org.n52.io.IoFactory;
import org.n52.io.IoHandlerException;
import org.n52.io.PreRenderingJob;
import org.n52.io.format.quantity.FormatterFactory;
import org.n52.io.generalize.quantity.GeneralizingQuantityService;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.ValueType;
import org.n52.io.response.dataset.quantity.QuantityDatasetOutput;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.series.spi.srv.DataService;
import org.n52.series.spi.srv.ParameterService;
import org.n52.series.spi.srv.RawDataService;
import org.n52.series.spi.srv.RawFormats;
import org.n52.web.common.Stopwatch;
import org.n52.web.exception.BadRequestException;
import org.n52.web.exception.InternalServerException;
import org.n52.web.exception.ResourceNotFoundException;
import org.n52.web.exception.SpiAssertionExceptionAdapter;
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

    private DataService<Data<QuantityValue>> timeseriesDataService;

    private PreRenderingJob preRenderingTask;

    private boolean handlingPreRenderingTask;

    private String requestIntervalRestriction;

    @Override
    protected IoParameters createParameters(RequestSimpleParameterSet query, String locale) {
        return super.createParameters(query, locale).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(RequestStyledParameterSet query, String locale) {
        return super.createParameters(query, locale).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(MultiValueMap<String, String> query, String locale) {
        return super.createParameters(query, locale).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(String datasetId, MultiValueMap<String, String> query, String locale) {
        return super.createParameters(datasetId, query, locale).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(Map<String, String> query, String locale) {
        return super.createParameters(query, locale).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(String datasetId, Map<String, String> query, String locale) {
        return super.createParameters(datasetId, query, locale).respectBackwardsCompatibility();
    }

    @RequestMapping(value = "/getData", produces = {
        Constants.APPLICATION_JSON
    }, method = RequestMethod.POST)
    public ModelAndView getCollectionData(HttpServletResponse response,
                                          @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                              required = false) String locale,
                                          @RequestBody RequestSimpleParameterSet request)
            throws Exception {
        IoParameters parameters = createParameters(request, locale);
        checkIfUnknownTimeseriesIds(parameters, parameters.getDatasets());
        if (parameters.isSetRawFormat()) {
            getRawCollectionData(response, locale, request);
            return null;
        }

        DataCollection<Data<QuantityValue>> seriesData = getTimeseriesData(parameters);
        DataCollection< ? > formattedDataCollection = format(seriesData, parameters);
        return new ModelAndView().addObject(formattedDataCollection.getAllSeries());
    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {
        Constants.APPLICATION_JSON
    }, method = RequestMethod.GET)
    public ModelAndView getData(HttpServletResponse response,
                                @PathVariable String timeseriesId,
                                @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                    required = false) String locale,
                                @RequestParam(required = false) MultiValueMap<String, String> request) {
        IoParameters parameters = createParameters(timeseriesId, request, locale);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        checkIfUnknownTimeseriesId(parameters, timeseriesId);

        // TODO add paging
        DataCollection<Data<QuantityValue>> seriesData = getTimeseriesData(parameters);
        DataCollection< ? > formattedDataCollection = format(seriesData, parameters);
        if (parameters.isExpanded()) {
            return new ModelAndView().addObject(formattedDataCollection.getAllSeries());
        }
        Object formattedTimeseries = formattedDataCollection.getAllSeries()
                                                            .get(timeseriesId);
        return new ModelAndView().addObject(formattedTimeseries);
    }

    private DataCollection<Data<QuantityValue>> getTimeseriesData(IoParameters parameters) {
        Stopwatch stopwatch = Stopwatch.startStopwatch();
        DataCollection<Data<QuantityValue>> timeseriesData = parameters.isGeneralize()
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
                                     @RequestBody RequestSimpleParameterSet request)
            throws Exception {
        IoParameters parameters = createParameters(request, locale);
        checkIfUnknownTimeseriesIds(parameters, parameters.getDatasets());
        processRawDataRequest(response, parameters);
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
                           @RequestParam MultiValueMap<String, String> request) {
        IoParameters parameters = createParameters(timeseriesId, request, locale);
        checkIfUnknownTimeseriesId(parameters, timeseriesId);
        processRawDataRequest(response, parameters);
    }

    private void processRawDataRequest(HttpServletResponse response,
                                       IoParameters parameters) {
        if (!timeseriesDataService.supportsRawData()) {
            throwNewRawDataQueryNotSupportedException();
        }
        final RawDataService rawDataService = timeseriesDataService.getRawDataService();
        try (InputStream inputStream = rawDataService.getRawData(parameters)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("No raw data found.");
            }
            response.setContentType(parameters.getFormat());
            IOUtils.copyLarge(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new InternalServerException("Error while querying raw data", e);
        }
    }

    private DataCollection< ? > format(DataCollection<Data<QuantityValue>> timeseriesData, IoParameters parameters) {
        return FormatterFactory.createFormatterFactory(parameters)
                               .create()
                               .format(timeseriesData);
    }

    @RequestMapping(value = "/getData",
        produces = {
            Constants.APPLICATION_PDF
        },
        method = RequestMethod.POST)
    public void getCollectionReport(HttpServletResponse response,
                                    @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                        required = false) String locale,
                                    @RequestBody RequestStyledParameterSet request)
            throws Exception {
        IoParameters parameters = createParameters(request, locale);
        checkIfUnknownTimeseriesIds(parameters, parameters.getDatasets());
        checkAgainstTimespanRestriction(parameters.getTimespan());

        response.setContentType(Constants.APPLICATION_PDF);
        createIoFactory(parameters).createHandler(Constants.APPLICATION_PDF)
                                   .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{timeseriesId}/getData",
        produces = {
            Constants.APPLICATION_PDF
        },
        method = RequestMethod.GET)
    public void getReport(HttpServletResponse response,
                          @PathVariable String timeseriesId,
                          @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE, required = false) String locale,
                          @RequestParam(required = false) MultiValueMap<String, String> request)
            throws Exception {
        IoParameters parameters = createParameters(timeseriesId, request, locale);
        checkIfUnknownTimeseriesId(parameters, timeseriesId);
        checkAgainstTimespanRestriction(parameters.getTimespan());

        response.setContentType(Constants.APPLICATION_PDF);
        createIoFactory(parameters).createHandler(Constants.APPLICATION_PDF)
                                   .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{timeseriesId}/getData",
        produces = {
            Constants.APPLICATION_ZIP
        },
        method = RequestMethod.GET)
    public void getAsZippedCsv(HttpServletResponse response,
                               @PathVariable String timeseriesId,
                               @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                   required = false) String locale,
                               @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        IoParameters parameters = createParameters(timeseriesId, query, locale);
        parameters = parameters.extendWith(Parameters.ZIP, Boolean.TRUE.toString());
        response.setContentType(Constants.APPLICATION_ZIP);
        getTimeseriesAsCsv(timeseriesId, parameters, response);
    }

    @RequestMapping(value = "/{timeseriesId}/getData",
        produces = {
            Constants.TEXT_CSV
        },
        method = RequestMethod.GET)
    public void getAsCsv(HttpServletResponse response,
                         @PathVariable String timeseriesId,
                         @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE, required = false) String locale,
                         @RequestParam(required = false) MultiValueMap<String, String> request)
            throws Exception {
        IoParameters parameters = createParameters(timeseriesId, request, locale);
        getTimeseriesAsCsv(timeseriesId, parameters, response);
    }

    private void getTimeseriesAsCsv(String timeseriesId, IoParameters parameters, HttpServletResponse response)
            throws IoHandlerException, DatasetFactoryException, URISyntaxException, MalformedURLException, IOException {
        checkAgainstTimespanRestriction(parameters.getTimespan());
        checkIfUnknownTimeseriesId(parameters, timeseriesId);

        response.setCharacterEncoding("UTF-8");
        if (Boolean.parseBoolean(parameters.getOther(Parameters.ZIP))) {
            response.setContentType(Constants.APPLICATION_ZIP);
        } else {
            response.setContentType(Constants.TEXT_CSV);
        }
        createIoFactory(parameters).createHandler(Constants.TEXT_CSV)
                                   .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/getData",
        produces = {
            Constants.IMAGE_PNG
        },
        method = RequestMethod.POST)
    public void getCollectionChart(HttpServletResponse response,
                                   @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                       required = false) String locale,
                                   @RequestBody RequestStyledParameterSet request)
            throws Exception {
        IoParameters parameters = createParameters(request, locale);
        checkIfUnknownTimeseriesIds(parameters, parameters.getDatasets());
        checkAgainstTimespanRestriction(parameters.getTimespan());

        response.setContentType(Constants.IMAGE_PNG);
        createIoFactory(parameters).createHandler(Constants.IMAGE_PNG)
                                   .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{timeseriesId}/getData",
        produces = {
            Constants.IMAGE_PNG
        },
        method = RequestMethod.GET)
    public void getChart(HttpServletResponse response,
                         @PathVariable String timeseriesId,
                         @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE, required = false) String locale,
                         @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        IoParameters parameters = createParameters(timeseriesId, query, locale);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        checkIfUnknownTimeseriesId(parameters, timeseriesId);

        response.setContentType(Constants.IMAGE_PNG);
        createIoFactory(parameters).createHandler(Constants.IMAGE_PNG)
                                   .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{timeseriesId}/{chartQualifier}",
        produces = {
            Constants.IMAGE_PNG
        },
        method = RequestMethod.GET)
    public void getChartByInterval(HttpServletResponse response,
                                   @PathVariable String timeseriesId,
                                   @PathVariable String chartQualifier,
                                   @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                       required = false) String locale,
                                   @RequestParam(required = false) MultiValueMap<String, String> request)
            throws Exception {
        if (preRenderingTask == null || isHandlingPreRenderingTask()) {
            throw new ResourceNotFoundException("Diagram prerendering is not enabled.");
        }
        String datasetId = ValueType.createId(ValueType.DEFAULT_VALUE_TYPE, timeseriesId);
        if (!preRenderingTask.hasPrerenderedImage(datasetId, chartQualifier)) {
            throw new ResourceNotFoundException("No pre-rendered chart found for timeseries '"
                    + timeseriesId
                    + "'.");
        }
        preRenderingTask.writePrerenderedGraphToOutputStream(datasetId, chartQualifier, response.getOutputStream());
    }

    private void checkAgainstTimespanRestriction(IntervalWithTimeZone timespan) {
        Duration duration = Period.parse(requestIntervalRestriction)
                                  .toDurationFrom(new DateTime());
        if (duration.getMillis() < Interval.parse(timespan.toString())
                                           .toDurationMillis()) {
            throw new BadRequestException("Requested timespan is to long, please use a period shorter than '"
                    + requestIntervalRestriction
                    + "'");
        }
    }

    private void checkIfUnknownTimeseriesId(IoParameters parameters, String timeseriesId) {
        checkIfUnknownTimeseriesIds(parameters, Collections.singleton(timeseriesId));
    }

    private void checkIfUnknownTimeseriesIds(IoParameters parameters, Set<String> timeseriesIds) {
        for (String timeseriesId : timeseriesIds) {
            if (!timeseriesMetadataService.exists(timeseriesId, parameters)) {
                throw new ResourceNotFoundException("The timeseries with id '"
                        + timeseriesId
                        + "' was not found.");
            }
        }
    }

    private IoFactory<QuantityDatasetOutput,
                      QuantityValue> createIoFactory(IoParameters parameters)
                              throws DatasetFactoryException, URISyntaxException, MalformedURLException {
        return createDefaultIoFactory().create(QuantityValue.TYPE)
                                       .setParameters(parameters)
                                       .setBasePath(getRootResource())
                                       .setDataService(timeseriesDataService)
                                       .setDatasetService(timeseriesMetadataService);
    }

    private DefaultIoFactory<QuantityDatasetOutput, QuantityValue> createDefaultIoFactory() {
        return new DefaultIoFactory<QuantityDatasetOutput, QuantityValue>();
    }

    private URI getRootResource() throws URISyntaxException, MalformedURLException {
        return getServletConfig().getServletContext()
                                 .getResource("/")
                                 .toURI();
    }

    public ParameterService<QuantityDatasetOutput> getTimeseriesMetadataService() {
        return timeseriesMetadataService;
    }

    public void setTimeseriesMetadataService(ParameterService<QuantityDatasetOutput> timeseriesMetadataService) {
        this.timeseriesMetadataService = new SpiAssertionExceptionAdapter<>(timeseriesMetadataService);
    }

    public DataService<Data<QuantityValue>> getTimeseriesDataService() {
        return timeseriesDataService;
    }

    public void setTimeseriesDataService(DataService<Data<QuantityValue>> timeseriesDataService) {
        this.timeseriesDataService = timeseriesDataService;
    }

    public PreRenderingJob getPreRenderingTask() {
        return preRenderingTask;
    }

    public boolean isHandlingPreRenderingTask() {
        return handlingPreRenderingTask;
    }

    public void setHandlingPreRenderingTask(boolean handlingPreRenderingTask) {
        this.handlingPreRenderingTask = handlingPreRenderingTask;
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
