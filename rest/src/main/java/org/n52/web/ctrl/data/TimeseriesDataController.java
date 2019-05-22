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
package org.n52.web.ctrl.data;

import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.n52.io.Constants;
import org.n52.io.PreRenderingJob;
import org.n52.io.handler.DefaultIoFactory;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.series.spi.srv.DataService;
import org.n52.series.spi.srv.ParameterService;
import org.n52.web.ctrl.UrlSettings;
import org.n52.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(value = UrlSettings.COLLECTION_TIMESERIES, produces = {
    "application/json"
})
public class TimeseriesDataController extends DataController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeseriesDataController.class);

    private PreRenderingJob preRenderingTask;

    @Autowired
    public TimeseriesDataController(DefaultIoFactory<DatasetOutput<AbstractValue<?>>, AbstractValue<?>> ioFactory,
            ParameterService<DatasetOutput<AbstractValue<?>>> datasetService,
            DataService<Data<AbstractValue<?>>> dataService) {
        super(ioFactory, datasetService, dataService);
    }

    @RequestMapping(value = "/observations",
        produces = {
            Constants.IMAGE_PNG
        },
        method = RequestMethod.POST)
    public void getSeriesCollectionChart(HttpServletRequest request,
                                         HttpServletResponse response,
                                         @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                             required = false) String locale,
                                         @RequestBody RequestStyledParameterSet simpleParameters)
            throws Exception {
        IoParameters parameters = createParameters(simpleParameters, locale, response);
        LOGGER.debug("get data collection chart with query: {}", parameters);
        checkForUnknownDatasetIds(parameters, parameters.getDatasets());

        // final String datasetType = getValueType(parameters);
        final String valueType = getValueType(parameters, request.getRequestURI());
        String outputFormat = Constants.IMAGE_PNG;
        response.setContentType(outputFormat);
        createIoFactory(valueType).setParameters(parameters)
                                  .createHandler(outputFormat)
                                  .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{datasetId}/observations",
        produces = {
            Constants.IMAGE_PNG
        },
        method = RequestMethod.GET)
    public void getSeriesChart(HttpServletRequest request,
                               HttpServletResponse response,
                               @PathVariable String datasetId,
                               @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                   required = false) String locale,
                               @RequestParam(required = false) MultiValueMap<String, String> query)
            throws Exception {
        IoParameters parameters = createParameters(datasetId, query, locale, response);
        LOGGER.debug("get data collection chart for '{}' with query: {}", datasetId, parameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        checkForUnknownDatasetId(parameters, datasetId);

        // String handleAsValueTypeFallback = parameters.getAsString(Parameters.HANDLE_AS_VALUE_TYPE);
        // String valueType = ValueType.extractType(datasetId, handleAsValueTypeFallback);
        String valueType = getValueType(parameters, request.getRequestURI());
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

    @Deprecated
    @RequestMapping(value = "/{datasetId}/getData",
            produces = {
                Constants.APPLICATION_JSON
            },
            method = RequestMethod.GET)
        public ModelAndView getTimeseriesData(HttpServletRequest request,
                                          HttpServletResponse response,
                                          @PathVariable String datasetId,
                                          @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                              required = false) String locale,
                                          @RequestParam(required = false) MultiValueMap<String, String> query)
                throws Exception {
        query.add(Parameters.UNIX_TIME, "true");
        return getSeriesData(request, response, datasetId, locale, query);
    }

    @Override
    protected String getValueType(IoParameters map, String requestUrl) {
        DatasetOutput<AbstractValue<?>> item = getFirstDatasetOutput(map);
        String datasetType = item.getDatasetType();
        if ( !"timeseries".equalsIgnoreCase(datasetType)) {
            String expectedType = UrlSettings.COLLECTION_TIMESERIES;
            String template = "The dataset with id ''{0}'' was not found for ''{1}''.";
            String message = MessageFormat.format(template, item.getId(), expectedType);
            throw new ResourceNotFoundException(message);
        }
        return isProfileType(item)
                ? PROFILE
                : item.getValueType();
    }

    // TODO set preredering config instead of task

    public PreRenderingJob getPreRenderingTask() {
        return preRenderingTask;
    }

    public void setPreRenderingTask(PreRenderingJob prerenderingTask) {
        this.preRenderingTask = prerenderingTask;
    }

    private void assertPrerenderingIsEnabled() {
        if (preRenderingTask == null) {
            throw new ResourceNotFoundException("Diagram prerendering is not enabled.");
        }
    }

    private void assertPrerenderedImageIsAvailable(String seriesId, String chartQualifier) {
        if ( !preRenderingTask.hasPrerenderedImage(seriesId, chartQualifier)) {
            throw new ResourceNotFoundException("No pre-rendered chart found for datasetId '"
                    + seriesId
                    + " (qualifier: "
                    + chartQualifier
                    + ")'.");
        }
    }
}
