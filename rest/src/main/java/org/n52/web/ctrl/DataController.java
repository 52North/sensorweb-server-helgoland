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
import java.util.Map;

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
import org.n52.io.IoProcessChain;
import org.n52.io.MimeType;
import org.n52.io.PreRenderingJob;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.request.QueryParameters;
import org.n52.io.request.RequestParameterSet;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.DatasetType;
import org.n52.series.spi.srv.DataService;
import org.n52.series.spi.srv.ParameterService;
import org.n52.series.spi.srv.RawDataService;
import org.n52.series.spi.srv.RawFormats;
import org.n52.web.exception.BadRequestException;
import org.n52.web.exception.InternalServerException;
import org.n52.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping(value = UrlSettings.COLLECTION_DATASETS, produces = {
    "application/json"
})
public class DataController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataController.class);

    private static final String DEFAULT_RESPONSE_ENCODING = "UTF-8";

    @Autowired
    private DefaultIoFactory<Data<AbstractValue< ? >>,
                             DatasetOutput<AbstractValue< ? >, ? >,
                             AbstractValue< ? >> ioFactoryCreator;

    private DataService<Data<AbstractValue< ? >>> dataService;

    private ParameterService<DatasetOutput<AbstractValue< ? >, ? >> datasetService;

    private PreRenderingJob preRenderingTask;

    // optional
    private String requestIntervalRestriction;

    @RequestMapping(value = "/data",
        produces = {
            "application/json"
        },
        method = RequestMethod.GET)
    public ModelAndView getSeriesData(HttpServletResponse response,
                                      @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        IoParameters parameters = QueryParameters.createFromQuery(query);
        LOGGER.debug("get data with query: {}", parameters);
        return getSeriesCollectionData(response, parameters.toSimpleParameterSet());
    }

    @RequestMapping(value = "/{seriesId}/data",
        produces = {
            "application/json"
        },
        method = RequestMethod.GET)
    public ModelAndView getSeriesData(HttpServletResponse response,
                                      @PathVariable String seriesId,
                                      @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {

        IoParameters map = QueryParameters.createFromQuery(query);
        LOGGER.debug("get data for item '{}' with query: {}", seriesId, map);

        IntervalWithTimeZone timespan = map.getTimespan();
        checkAgainstTimespanRestriction(timespan.toString());
        checkForUnknownSeriesIds(map, seriesId);

        RequestSimpleParameterSet parameters = RequestSimpleParameterSet.createForSingleSeries(seriesId, map);
        String handleAsDatasetFallback = map.getAsString(Parameters.HANDLE_AS_DATASET_TYPE);
        String datasetType = DatasetType.extractType(seriesId, handleAsDatasetFallback);
        IoProcessChain< ? > ioChain = createIoFactory(datasetType).withSimpleRequest(parameters)
                                                                  .createProcessChain();

        DataCollection< ? > formattedDataCollection = ioChain.getProcessedData();
        final Map<String, ? > processed = formattedDataCollection.getAllSeries();
        return map.isExpanded()
                ? new ModelAndView().addObject(processed)
                : new ModelAndView().addObject(processed.get(seriesId));
    }

    @RequestMapping(value = "/data", produces = {"application/json"}, method = RequestMethod.POST)
    public ModelAndView getSeriesCollectionData(HttpServletResponse response,
            @RequestBody RequestSimpleParameterSet parameters) throws Exception {

        LOGGER.debug("get data collection with parameter set: {}", parameters);

        checkForUnknownSeriesIds(parameters, parameters.getDatasets());
        checkAgainstTimespanRestriction(parameters.getTimespan());

        final String datasetType = parameters.getDatasetType();
        IoProcessChain< ? > ioChain = createIoFactory(datasetType)
                .withSimpleRequest(parameters)
                .createProcessChain();

        DataCollection<?> processed = ioChain.getData();
        return new ModelAndView().addObject(processed.getAllSeries());
    }

    @RequestMapping(value = "/data",
        params = {
            RawFormats.RAW_FORMAT
        },
        method = RequestMethod.POST)
    public void getRawSeriesCollectionData(HttpServletResponse response,
                                           @RequestBody RequestSimpleParameterSet parameters)
            throws Exception {
        checkForUnknownSeriesIds(parameters, parameters.getDatasets());
        LOGGER.debug("get raw data collection with parameters: {}", parameters);
        writeRawData(parameters, response);
    }

    @RequestMapping(value = "/{seriesId}/data",
        method = RequestMethod.GET,
        params = {
            RawFormats.RAW_FORMAT
        })
    public void getRawSeriesData(HttpServletResponse response,
                                 @PathVariable String seriesId,
                                 @RequestParam MultiValueMap<String, String> query) {
        IoParameters map = QueryParameters.createFromQuery(query);
        checkForUnknownSeriesIds(map, seriesId);
        LOGGER.debug("getSeriesCollection() with query: {}", map);
        RequestSimpleParameterSet parameters = RequestSimpleParameterSet.createForSingleSeries(seriesId, map);
        writeRawData(parameters, response);
    }

    private void writeRawData(RequestSimpleParameterSet parameters, HttpServletResponse response)
            throws InternalServerException, ResourceNotFoundException, BadRequestException {
        if (!dataService.supportsRawData()) {
            throw new BadRequestException("Querying of raw timeseries data is not supported "
                    + "by the underlying service!");
        }
        final RawDataService rawDataService = dataService.getRawDataService();
        try (InputStream inputStream = rawDataService.getRawData(parameters)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("No raw data found.");
            }
            IOUtils.copyLarge(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new InternalServerException("Error while querying raw data", e);
        }
    }

    @RequestMapping(value = "/data",
        produces = {
            "application/pdf"
        },
        method = RequestMethod.POST)
    public void getSeriesCollectionReport(HttpServletResponse response,
                                          @RequestBody RequestStyledParameterSet parameters)
            throws Exception {
        IoParameters map = QueryParameters.createFromQuery(parameters);
        LOGGER.debug("get data collection report with query: {}", map);

        checkForUnknownSeriesIds(parameters, parameters.getDatasets());
        checkAgainstTimespanRestriction(parameters.getTimespan());

        final String datasetType = parameters.getDatasetType();
        createIoFactory(datasetType).withStyledRequest(map.mergeToStyledParameterSet(parameters))
                                    .withSimpleRequest(map.mergeToSimpleParameterSet(parameters))
                                    .createHandler(MimeType.APPLICATION_PDF.toString())
                                    .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{seriesId}/data",
        produces = {
            "application/pdf"
        },
        method = RequestMethod.GET)
    public void getSeriesReport(HttpServletResponse response,
                                @PathVariable String seriesId,
                                @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {

        IoParameters map = QueryParameters.createFromQuery(query);
        LOGGER.debug("get data collection report for '{}' with query: {}", seriesId, map);
        RequestSimpleParameterSet parameters = RequestSimpleParameterSet
                                                                        .createForSingleSeries(seriesId, map);

        checkAgainstTimespanRestriction(parameters.getTimespan());
        checkForUnknownSeriesIds(map, seriesId);

        final String datasetType = parameters.getDatasetType();
        createIoFactory(datasetType).withSimpleRequest(parameters)
                                    .createHandler(MimeType.APPLICATION_PDF.toString())
                                    .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{seriesId}/data",
        produces = {
            "application/zip"
        },
        method = RequestMethod.GET)
    public void getSeriesAsZippedCsv(HttpServletResponse response,
                                     @PathVariable String seriesId,
                                     @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        IoParameters map = QueryParameters.createFromQuery(query);
        LOGGER.debug("get data collection zip for '{}' with query: {}", seriesId, map);
        RequestSimpleParameterSet parameters = RequestSimpleParameterSet.createForSingleSeries(seriesId, map);

        checkAgainstTimespanRestriction(parameters.getTimespan());
        checkForUnknownSeriesIds(map, seriesId);

        response.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
        response.setContentType(MimeType.APPLICATION_ZIP.toString());

        final String datasetType = parameters.getDatasetType();
        createIoFactory(datasetType)
                                    .withSimpleRequest(parameters)
                                    .createHandler(MimeType.APPLICATION_ZIP.toString())
                                    .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{seriesId}/data",
        produces = {
            "text/csv"
        },
        method = RequestMethod.GET)
    public void getSeriesAsCsv(HttpServletResponse response,
                               @PathVariable String seriesId,
                               @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {

        IoParameters map = QueryParameters.createFromQuery(query);
        LOGGER.debug("get data collection csv for '{}' with query: {}", seriesId, map);
        RequestSimpleParameterSet parameters = RequestSimpleParameterSet.createForSingleSeries(seriesId, map);

        checkAgainstTimespanRestriction(parameters.getTimespan());
        checkForUnknownSeriesIds(map, seriesId);

        response.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
        if (Boolean.parseBoolean(map.getOther("zip"))) {
            response.setContentType(MimeType.APPLICATION_ZIP.toString());
        } else {
            response.setContentType(MimeType.TEXT_CSV.toString());
        }

        final String datasetType = parameters.getDatasetType();
        createIoFactory(datasetType).withSimpleRequest(parameters)
                                    .createHandler(MimeType.TEXT_CSV.toString())
                                    .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/data",
        produces = {
            "image/png"
        },
        method = RequestMethod.POST)
    public void getSeriesCollectionChart(HttpServletResponse response,
                                         @RequestBody RequestStyledParameterSet parameters)
            throws Exception {

        IoParameters map = QueryParameters.createFromQuery(parameters);
        checkForUnknownSeriesIds(map, parameters.getDatasets());

        LOGGER.debug("get data collection chart with query: {}", map);

        final String datasetType = parameters.getDatasetType();
        createIoFactory(datasetType).withStyledRequest(parameters)
                                    .createHandler(MimeType.IMAGE_PNG.toString())
                                    .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{seriesId}/data",
        produces = {
            "image/png"
        },
        method = RequestMethod.GET)
    public void getSeriesChart(HttpServletResponse response,
                               @PathVariable String seriesId,
                               @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {

        IoParameters map = QueryParameters.createFromQuery(query);
        LOGGER.debug("get data collection chart for '{}' with query: {}", seriesId, map);
        checkAgainstTimespanRestriction(map.getTimespan()
                                           .toString());
        checkForUnknownSeriesIds(map, seriesId);

        String handleAsDatasetFallback = map.getAsString(Parameters.HANDLE_AS_DATASET_TYPE);
        String observationType = DatasetType.extractType(seriesId, handleAsDatasetFallback);
        RequestSimpleParameterSet parameters = map.toSimpleParameterSet();
        createIoFactory(observationType).withSimpleRequest(parameters)
                                        .createHandler(MimeType.IMAGE_PNG.toString())
                                        .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{seriesId}/{chartQualifier}",
        produces = {
            "image/png"
        },
        method = RequestMethod.GET)
    public void getSeriesChartByInterval(HttpServletResponse response,
                                         @PathVariable String seriesId,
                                         @PathVariable String chartQualifier,
                                         @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        if (preRenderingTask == null) {
            throw new ResourceNotFoundException("Diagram prerendering is not enabled.");
        }
        if (!preRenderingTask.hasPrerenderedImage(seriesId, chartQualifier)) {
            throw new ResourceNotFoundException("No pre-rendered chart found for timeseries '" + seriesId + "'.");
        }

        LOGGER.debug("get prerendered chart for '{}' ({})", seriesId, chartQualifier);
        preRenderingTask.writePrerenderedGraphToOutputStream(seriesId, chartQualifier, response.getOutputStream());
    }

    private void checkAgainstTimespanRestriction(String timespan) {
        if (requestIntervalRestriction != null) {
            Duration duration = Period.parse(requestIntervalRestriction)
                                      .toDurationFrom(new DateTime());
            if (duration.getMillis() < Interval.parse(timespan)
                                               .toDurationMillis()) {
                throw new BadRequestException("Timespan too long, please use a period shorter than '"
                        + requestIntervalRestriction
                        + "'");
            }
        }
    }

    private void checkForUnknownSeriesIds(RequestParameterSet parameters, String... seriesIds) {
        checkForUnknownSeriesIds(IoParameters.createFromQuery(parameters), seriesIds);
    }

    private void checkForUnknownSeriesIds(IoParameters parameters, String... seriesIds) {
        if (seriesIds != null) {
            for (String id : seriesIds) {
                if (!datasetService.exists(id, parameters)) {
                    throw new ResourceNotFoundException("Series with id '" + id + "' wasn't found.");
                }
            }
        }
    }

    private IoFactory<Data<AbstractValue< ? >>,
                      DatasetOutput<AbstractValue< ? >, ? >,
                      AbstractValue< ? >> createIoFactory(final String datasetType)
                              throws DatasetFactoryException {
        if (!ioFactoryCreator.isKnown(datasetType)) {
            throw new ResourceNotFoundException("unknown dataset type: " + datasetType);
        }
        return ioFactoryCreator.create(datasetType)
                               // .withBasePath(getRootResource())
                               .withDataService(dataService)
                               .withDatasetService(datasetService);
    }

    private URI getRootResource() throws URISyntaxException, MalformedURLException {
        return getServletConfig().getServletContext()
                                 .getResource("/")
                                 .toURI();
    }

    // TODO set preredering config instead of task

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
        LOGGER.debug("CONFIG: request.interval.restriction={}", requestIntervalRestriction);
        this.requestIntervalRestriction = requestIntervalRestriction;
    }

    public DataService<Data<AbstractValue< ? >>> getDataService() {
        return dataService;
    }

    public void setDataService(DataService<Data<AbstractValue< ? >>> dataService) {
        this.dataService = dataService;
    }

    public ParameterService<DatasetOutput<AbstractValue< ? >, ? >> getDatasetService() {
        return datasetService;
    }

    public void setDatasetService(ParameterService<DatasetOutput<AbstractValue< ? >, ? >> datasetService) {
        this.datasetService = datasetService;
    }
}
