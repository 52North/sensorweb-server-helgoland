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
import org.n52.io.handler.DefaultIoFactory;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.series.spi.srv.DataService;
import org.n52.series.spi.srv.ParameterService;
import org.n52.web.ctrl.UrlSettings;
import org.n52.web.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
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

    @Autowired
    public TimeseriesDataController(DefaultIoFactory<DatasetOutput<AbstractValue<?>>, AbstractValue<?>> ioFactory,
            ParameterService<DatasetOutput<AbstractValue<?>>> datasetService,
            DataService<Data<AbstractValue<?>>> dataService) {
        super(ioFactory, datasetService, dataService);
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

}
