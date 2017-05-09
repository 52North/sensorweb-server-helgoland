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

import org.n52.io.request.IoParameters;
import org.n52.io.request.QueryParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.dataset.StationOutput;
import org.n52.series.spi.geo.TransformingStationOutputService;
import org.n52.series.spi.srv.LocaleAwareSortService;
import org.n52.series.spi.srv.ParameterService;
import org.n52.web.common.Stopwatch;
import org.n52.web.exception.ResourceNotFoundException;
import org.n52.web.exception.WebExceptionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Deprecated
@RestController
@RequestMapping(value = UrlSettings.COLLECTION_STATIONS, produces = {"application/json"})
public class StationsParameterController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StationsParameterController.class);

    private ParameterService<StationOutput> parameterService;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView getCollection(@RequestParam(required = false) MultiValueMap<String, String> query) {
        IoParameters map = QueryParameters.createFromQuery(query);
        map = IoParameters.ensureBackwardsCompatibility(map);

        if (map.isExpanded()) {
            Stopwatch stopwatch = Stopwatch.startStopwatch();
            OutputCollection<?> result = parameterService.getExpandedParameters(map);
            logRequestTime(stopwatch);

            // TODO add paging
            return new ModelAndView().addObject(result.getItems());
        } else {
            Stopwatch stopwatch = Stopwatch.startStopwatch();
            OutputCollection<?> result = parameterService.getCondensedParameters(map);
            logRequestTime(stopwatch);

            // TODO add paging
            return new ModelAndView().addObject(result.getItems());
        }
    }

    @RequestMapping(value = "/{item}", method = RequestMethod.GET)
    public ModelAndView getItem(@PathVariable("item") String procedureId,
            @RequestParam(required = false) MultiValueMap<String, String> query) {
        IoParameters map = QueryParameters.createFromQuery(query);
        map = IoParameters.ensureBackwardsCompatibility(map);

        // TODO check parameters and throw BAD_REQUEST if invalid
        Stopwatch stopwatch = Stopwatch.startStopwatch();
        Object result = parameterService.getParameter(procedureId, map);
        logRequestTime(stopwatch);

        if (result == null) {
            throw new ResourceNotFoundException("Found no station with given id.");
        }

        return new ModelAndView().addObject(result);
    }

    public ParameterService<StationOutput> getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService<StationOutput> stationParameterService) {
        ParameterService<StationOutput> service = new TransformingStationOutputService(stationParameterService);
        this.parameterService = new LocaleAwareSortService<>(new WebExceptionAdapter<>(service));
    }

    private void logRequestTime(Stopwatch stopwatch) {
        LOGGER.debug("Processing request took {} seconds.", stopwatch.stopInSeconds());
    }

}
