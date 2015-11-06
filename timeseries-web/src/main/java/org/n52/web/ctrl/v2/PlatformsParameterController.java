/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.web.ctrl.v2;

import org.n52.io.geojson.GeoJSONFeature;
import static org.n52.io.request.QueryParameters.createFromQuery;
import static org.n52.web.ctrl.v2.RestfulUrls.COLLECTION_PLATFORMS;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.v2.PlatformOutput;
import org.n52.io.response.v2.PlatformOutputCollection;
import org.n52.web.exception.ResourceNotFoundException;
import org.n52.sensorweb.spi.LocaleAwareSortService;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.sensorweb.spi.v2.TransformingGeometryOutputService;
import org.n52.web.exception.WebExceptionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping(value = COLLECTION_PLATFORMS, produces = {"application/json"})
public class PlatformsParameterController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformsParameterController.class);

    private ParameterService<PlatformOutput> parameterService;

    @RequestMapping(method = GET)
    public ModelAndView getCollection(@RequestParam(required = false) MultiValueMap<String, String> query) {
        IoParameters map = createFromQuery(query);

        if (map.isExpanded()) {
            OutputCollection<?> result = parameterService.getExpandedParameters(map);

            // TODO add paging

            return new ModelAndView().addObject(result);
        }
        else {
            OutputCollection<?> result = parameterService.getCondensedParameters(map);

            // TODO add paging

            return new ModelAndView().addObject(result);
        }
    }

    @RequestMapping(value = "/{item}", method = GET)
    public ModelAndView getItem(@PathVariable("item") String procedureId,
                                @RequestParam(required = false) MultiValueMap<String, String> query) {
        IoParameters map = createFromQuery(query);

        // TODO check parameters and throw BAD_REQUEST if invalid

        Object result = parameterService.getParameter(procedureId, map);

        if (result == null) {
            throw new ResourceNotFoundException("Found no platform with given id.");
        }

        return new ModelAndView().addObject(result);
    }

    public ParameterService<PlatformOutput> getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService<PlatformOutput> geojsonOutputService) {
        // TODO via config
//        ParameterService<PlatformOutput> service = new TransformingGeometryOutputService(geojsonOutputService);
        ParameterService<PlatformOutput> service = geojsonOutputService;
        this.parameterService = new LocaleAwareSortService<>(new WebExceptionAdapter<>(service));
    }

}
