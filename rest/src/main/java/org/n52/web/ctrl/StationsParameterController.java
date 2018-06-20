/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.dataset.StationOutput;
import org.n52.series.spi.geo.TransformingStationOutputService;
import org.n52.series.spi.srv.CountingMetadataService;
import org.n52.series.spi.srv.LocaleAwareSortService;
import org.n52.series.spi.srv.ParameterService;
import org.n52.web.common.OffsetBasedPagination;
import org.n52.web.common.PageLinkUtil;
import org.n52.web.common.Paginated;
import org.n52.web.common.Stopwatch;
import org.n52.web.exception.ResourceNotFoundException;
import org.n52.web.exception.SpiAssertionExceptionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Deprecated
@RestController
@RequestMapping(value = UrlSettings.COLLECTION_STATIONS, produces = {
    "application/json"
})
public class StationsParameterController extends BaseController implements ResourceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StationsParameterController.class);

    private final ParameterService<StationOutput> parameterService;

    private final CountingMetadataService counter;

    private final ProceduresParameterController parameterControllerWithHref;

    public StationsParameterController(CountingMetadataService counter,
                                       ParameterService<StationOutput> service,
                                       ProceduresParameterController proceduresController) {
        ParameterService<StationOutput> transformingService = new TransformingStationOutputService(service);
        this.parameterService = new LocaleAwareSortService<>(new SpiAssertionExceptionAdapter<>(transformingService));
        this.parameterControllerWithHref = proceduresController;
        this.counter = counter;
    }
    
    @Override
    public String getCollectionName() {
        return UrlSettings.COLLECTION_STATIONS;
    }

    @Override
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView getCollection(HttpServletResponse response,
                                      @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                        required = false) String locale,
                                      @RequestParam(required = false) MultiValueMap<String, String> query) {
        IoParameters map = createParameters(query, locale).respectBackwardsCompatibility();
        OutputCollection< ? > result;

        if (map.isExpanded()) {
            Stopwatch stopwatch = Stopwatch.startStopwatch();
            result = parameterService.getExpandedParameters(map);
            logRequestTime(stopwatch);
        } else {
            Stopwatch stopwatch = Stopwatch.startStopwatch();
            result = parameterService.getCondensedParameters(map);
            logRequestTime(stopwatch);
        }

        // XXX refactor (is redundant here)
        if (map.containsParameter("limit") || map.containsParameter("offset")) {
            Integer elementcount = this.counter.getStationCount();
            if (elementcount != -1) {
                OffsetBasedPagination obp = new OffsetBasedPagination(map.getOffset(), map.getLimit());
                Paginated paginated = new Paginated(obp, elementcount.longValue());
                String collectionHref = createCollectionUrl(getCollectionName());
                PageLinkUtil.addPagingHeaders(collectionHref, response, paginated);
            }
        }
        return new ModelAndView().addObject(result.getItems());
    }

    protected MultiValueMap<String, String> addHrefBase(MultiValueMap<String, String> query) {
        List<String> value = Collections.singletonList(getExternalUrl());
        query.put(Parameters.HREF_BASE, value);
        return query;
    }

    @Override
    @RequestMapping(value = "/{item}", method = RequestMethod.GET)
    public ModelAndView getItem(@PathVariable("item") String procedureId,
                                @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                    required = false) String locale,
                                @RequestParam(required = false) MultiValueMap<String, String> query) {
        IoParameters parameters = createParameters(query, locale);

        // TODO check parameters and throw BAD_REQUEST if invalid
        Stopwatch stopwatch = Stopwatch.startStopwatch();
        Object result = parameterService.getParameter(procedureId, parameters);
        logRequestTime(stopwatch);

        if (result == null) {
            throw new ResourceNotFoundException("Found no station with given id.");
        }

        return new ModelAndView().addObject(result);
    }


    private void logRequestTime(Stopwatch stopwatch) {
        LOGGER.debug("Processing request took {} seconds.", stopwatch.stopInSeconds());
    }

}
