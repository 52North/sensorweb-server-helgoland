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
import java.util.Iterator;
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
import org.n52.io.IoProcessChain;
import org.n52.io.PreRenderingJob;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.ValueType;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    private DefaultIoFactory<DatasetOutput<AbstractValue< ? >>,
                             AbstractValue< ? >> ioFactoryCreator;

    private DataService<Data<AbstractValue< ? >>> dataService;

    private ParameterService<DatasetOutput<AbstractValue< ? >>> datasetService;

    private PreRenderingJob preRenderingTask;

    @Value("${requestIntervalRestriction:P370D}")
    private String requestIntervalRestriction;

    @RequestMapping(value = "/{datasetId}/data",
        produces = {
            Constants.APPLICATION_JSON
        },
        method = RequestMethod.GET)
    public ModelAndView getSeriesData(HttpServletResponse response,
                                      @PathVariable String datasetId,
                                      @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                          required = false) String locale,
                                      @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        IoParameters map = createParameters(datasetId, query, locale);
        LOGGER.debug("get data for item '{}' with query: {}", datasetId, map);
        checkAgainstTimespanRestriction(map.getTimespan());
        checkForUnknownDatasetId(map.removeAllOf(Parameters.BBOX)
                                    .removeAllOf(Parameters.NEAR), datasetId);

        // RequestSimpleIoParameters parameters = RequestSimpleIoParameters.createForSingleSeries(seriesId,
        // map);
        String handleAsValueTypeFallback = map.getAsString(Parameters.HANDLE_AS_VALUE_TYPE);
        String valueType = ValueType.extractType(datasetId, handleAsValueTypeFallback);
        IoProcessChain< ? > ioChain = createIoFactory(valueType).setParameters(map)
                                                                .createProcessChain();

        DataCollection< ? > formattedDataCollection = ioChain.getProcessedData();
        final Map<String, ? > processed = formattedDataCollection.getAllSeries();
        return map.isExpanded()
                ? new ModelAndView().addObject(processed)
                : new ModelAndView().addObject(processed.get(datasetId));
    }

    @RequestMapping(value = "/data",
        produces = {
            Constants.APPLICATION_JSON
        },
        method = RequestMethod.POST)
    public ModelAndView getCollectionData(HttpServletResponse response,
                                          @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                              required = false) String locale,
                                          @RequestBody RequestSimpleParameterSet simpleParameters)
            throws Exception {
        IoParameters parameters = createParameters(simpleParameters, locale);
        LOGGER.debug("get data collection with parameter set: {}", parameters);
        checkForUnknownDatasetIds(parameters, parameters.getDatasets());
        checkAgainstTimespanRestriction(parameters.getTimespan());

        final String datasetType = getValueType(parameters);
        IoProcessChain< ? > ioChain = createIoFactory(datasetType).setParameters(parameters)
                                                                  .createProcessChain();

        DataCollection< ? > processed = ioChain.getData();
        return new ModelAndView().addObject(processed.getAllSeries());
    }

    private String getValueType(IoParameters parameters) {
        String handleAs = parameters.getOther(Parameters.HANDLE_AS_VALUE_TYPE);
        Set<String> datasetIds = parameters.getDatasets();
        Iterator<String> iterator = datasetIds.iterator();
        return iterator.hasNext()
                ? ValueType.extractType(iterator.next(), handleAs)
                : ValueType.DEFAULT_VALUE_TYPE;
    }

    @RequestMapping(value = "/data",
        params = {
            RawFormats.RAW_FORMAT
        },
        method = RequestMethod.POST)
    public void getRawSeriesCollectionData(HttpServletResponse response,
                                           @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                               required = false) String locale,
                                           @RequestBody RequestSimpleParameterSet simpleParameters)
            throws Exception {
        IoParameters parameters = createParameters(simpleParameters, locale);
        checkForUnknownDatasetIds(parameters, parameters.getDatasets());
        writeRawData(parameters, response);
    }

    @RequestMapping(value = "/{datasetId}/data",
        method = RequestMethod.GET,
        params = {
            RawFormats.RAW_FORMAT
        })
    public void getRawSeriesData(HttpServletResponse response,
                                 @PathVariable String datasetId,
                                 @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                     required = false) String locale,
                                 @RequestParam MultiValueMap<String, String> query) {
        IoParameters parameters = createParameters(datasetId, query, locale);
        checkForUnknownDatasetId(parameters, datasetId);
        writeRawData(parameters, response);
    }

    private void writeRawData(IoParameters parameters, HttpServletResponse response)
            throws InternalServerException, ResourceNotFoundException, BadRequestException {
        LOGGER.debug("get raw data collection with parameters: {}", parameters);
        if (!dataService.supportsRawData()) {
            throw new BadRequestException("Querying of raw timeseries data is not supported "
                    + "by the underlying service!");
        }
        final RawDataService rawDataService = dataService.getRawDataService();
        try (InputStream inputStream = rawDataService.getRawData(parameters)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("No raw data found.");
            }
            response.setContentType(parameters.getRawFormat());
            IOUtils.copyLarge(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new InternalServerException("Error while querying raw data", e);
        }
    }

    @RequestMapping(value = "/data",
        produces = {
            Constants.APPLICATION_PDF
        },
        method = RequestMethod.POST)
    public void getSeriesCollectionReport(HttpServletResponse response,
                                          @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                              required = false) String locale,
                                          @RequestBody RequestStyledParameterSet request)
            throws Exception {
        IoParameters parameters = createParameters(request, locale);
        LOGGER.debug("get data collection report with query: {}", parameters);
        checkForUnknownDatasetIds(parameters, parameters.getDatasets());
        checkAgainstTimespanRestriction(parameters.getTimespan());

        final String datasetType = getValueType(parameters);
        String outputFormat = Constants.APPLICATION_PDF;
        response.setContentType(outputFormat);
        createIoFactory(datasetType).setParameters(parameters)
                                    .createHandler(outputFormat)
                                    .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{datasetId}/data",
        produces = {
            Constants.APPLICATION_PDF
        },
        method = RequestMethod.GET)
    public void getSeriesReport(HttpServletResponse response,
                                @PathVariable String datasetId,
                                @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                    required = false) String locale,
                                @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        IoParameters parameters = createParameters(datasetId, query, locale);
        LOGGER.debug("get data collection report for '{}' with query: {}", datasetId, parameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        checkForUnknownDatasetId(parameters, datasetId);

        final String datasetType = getValueType(parameters);
        String outputFormat = Constants.APPLICATION_PDF;
        response.setContentType(outputFormat);
        createIoFactory(datasetType).setParameters(parameters)
                                    .createHandler(outputFormat)
                                    .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{datasetId}/data",
        produces = {
            Constants.APPLICATION_ZIP
        },
        method = RequestMethod.GET)
    public void getSeriesAsZippedCsv(HttpServletResponse response,
                                     @PathVariable String datasetId,
                                     @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                         required = false) String locale,
                                     @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        IoParameters parameters = createParameters(datasetId, query, locale);
        LOGGER.debug("get data collection zip for '{}' with query: {}", datasetId, parameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        checkForUnknownDatasetId(parameters, datasetId);

        response.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
        response.setContentType(Constants.APPLICATION_ZIP);

        final String datasetType = getValueType(parameters);
        createIoFactory(datasetType).setParameters(parameters)
                                    .createHandler(Constants.APPLICATION_ZIP)
                                    .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{datasetId}/data",
        produces = {
            Constants.TEXT_CSV
        },
        method = RequestMethod.GET)
    public void getSeriesAsCsv(HttpServletResponse response,
                               @PathVariable String datasetId,
                               @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                   required = false) String locale,
                               @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        IoParameters parameters = createParameters(datasetId, query, locale);
        LOGGER.debug("get data collection csv for '{}' with query: {}", datasetId, parameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        checkForUnknownDatasetId(parameters, datasetId);

        response.setCharacterEncoding(DEFAULT_RESPONSE_ENCODING);
        if (Boolean.parseBoolean(parameters.getOther("zip"))) {
            response.setContentType(Constants.APPLICATION_ZIP);
        } else {
            response.setContentType(Constants.TEXT_CSV);
        }

        final String datasetType = getValueType(parameters);
        createIoFactory(datasetType).setParameters(parameters)
                                    .createHandler(Constants.TEXT_CSV)
                                    .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/data",
        produces = {
            Constants.IMAGE_PNG
        },
        method = RequestMethod.POST)
    public void getSeriesCollectionChart(HttpServletResponse response,
                                         @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                             required = false) String locale,
                                         @RequestBody RequestStyledParameterSet request)
            throws Exception {
        IoParameters parameters = createParameters(request, locale);
        LOGGER.debug("get data collection chart with query: {}", parameters);
        checkForUnknownDatasetIds(parameters, parameters.getDatasets());

        final String datasetType = getValueType(parameters);
        String outputFormat = Constants.IMAGE_PNG;
        response.setContentType(outputFormat);
        createIoFactory(datasetType).setParameters(parameters)
                                    .createHandler(outputFormat)
                                    .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{datasetId}/data",
        produces = {
            Constants.IMAGE_PNG
        },
        method = RequestMethod.GET)
    public void getSeriesChart(HttpServletResponse response,
                               @PathVariable String datasetId,
                               @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                   required = false) String locale,
                               @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        IoParameters parameters = createParameters(datasetId, query, locale);
        LOGGER.debug("get data collection chart for '{}' with query: {}", datasetId, parameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        checkForUnknownDatasetId(parameters, datasetId);

        String handleAsValueTypeFallback = parameters.getAsString(Parameters.HANDLE_AS_VALUE_TYPE);
        String valueType = ValueType.extractType(datasetId, handleAsValueTypeFallback);
        String outputFormat = Constants.IMAGE_PNG;
        response.setContentType(outputFormat);
        createIoFactory(valueType).setParameters(parameters)
                                  .createHandler(outputFormat)
                                  .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{datasetId}/images", method = RequestMethod.GET)
    public ModelAndView getSeriesChartByInterval(@PathVariable String datasetId) {
        assertPrerenderingIsEnabled();
        ModelAndView response = new ModelAndView();
        return response.addObject(preRenderingTask.getPrerenderedImages(datasetId));
    }

    @RequestMapping(value = "/{datasetId}/{chartQualifier}",
        produces = {
            Constants.IMAGE_PNG
        },
        method = RequestMethod.GET)
    public void getSeriesChartByInterval(HttpServletResponse response,
                                         @PathVariable String datasetId,
                                         @PathVariable String chartQualifier)
            throws Exception {
        assertPrerenderingIsEnabled();
        assertPrerenderedImageIsAvailable(datasetId, chartQualifier);

        response.setContentType(Constants.IMAGE_PNG);
        LOGGER.debug("get prerendered chart for '{}' ({})", datasetId, chartQualifier);
        preRenderingTask.writePrerenderedGraphToOutputStream(datasetId, chartQualifier, response.getOutputStream());
    }

    @RequestMapping(value = "/{datasetId}/images/{fileName}",
        produces = {
            Constants.IMAGE_PNG
        },
        method = RequestMethod.GET)
    public void getSeriesChartByFilename(HttpServletResponse response,
                                         @PathVariable String datasetId,
                                         @PathVariable String fileName)
            throws Exception {
        assertPrerenderingIsEnabled();
        assertPrerenderedImageIsAvailable(fileName, null);

        response.setContentType(Constants.IMAGE_PNG);
        LOGGER.debug("get prerendered chart for '{}'", fileName);
        preRenderingTask.writePrerenderedGraphToOutputStream(fileName, response.getOutputStream());

    }

    private void checkAgainstTimespanRestriction(IntervalWithTimeZone timespan) {
        if (requestIntervalRestriction != null) {
            Duration duration = Period.parse(requestIntervalRestriction)
                                      .toDurationFrom(new DateTime());
            if (duration.getMillis() < Interval.parse(timespan.toString())
                                               .toDurationMillis()) {
                throw new BadRequestException("Timespan too long, please use a period shorter than '"
                        + requestIntervalRestriction
                        + "'");
            }
        }
    }

    private void checkForUnknownDatasetId(IoParameters parameters, String seriesId) {
        checkForUnknownDatasetIds(parameters, Collections.singleton(seriesId));
    }

    private void checkForUnknownDatasetIds(IoParameters parameters, Set<String> seriesIds) {
        if (seriesIds != null) {
            for (String id : seriesIds) {
                if (!datasetService.exists(id, parameters)) {
                    throw new ResourceNotFoundException("Series with id '" + id + "' wasn't found.");
                }
            }
        }
    }

    private IoFactory<DatasetOutput<AbstractValue< ? >>,
                      AbstractValue< ? >> createIoFactory(final String valueType)
                              throws DatasetFactoryException {
        if (!ioFactoryCreator.isKnown(valueType)) {
            throw new ResourceNotFoundException("unknown dataset type: " + valueType);
        }
        return ioFactoryCreator.create(valueType)
                               // .withBasePath(getRootResource())
                               .setDataService(dataService)
                               .setDatasetService(datasetService);
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

    public ParameterService<DatasetOutput<AbstractValue< ? >>> getDatasetService() {
        return datasetService;
    }

    public void setDatasetService(ParameterService<DatasetOutput<AbstractValue< ? >>> datasetService) {
        this.datasetService = datasetService;
    }

    private void assertPrerenderingIsEnabled() {
        if (preRenderingTask == null) {
            throw new ResourceNotFoundException("Diagram prerendering is not enabled.");
        }
    }

    private void assertPrerenderedImageIsAvailable(String seriesId, String chartQualifier) {
        if (!preRenderingTask.hasPrerenderedImage(seriesId, chartQualifier)) {
            throw new ResourceNotFoundException("No pre-rendered chart found for datasetId '"
                    + seriesId
                    + " (qualifier: "
                    + chartQualifier
                    + ")'.");
        }
    }

}
