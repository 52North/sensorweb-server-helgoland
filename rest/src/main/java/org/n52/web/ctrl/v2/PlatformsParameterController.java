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
package org.n52.web.ctrl.v2;

import static org.n52.io.request.QueryParameters.createFromQuery;
import static org.n52.web.ctrl.v2.RestfulUrls.COLLECTION_PLATFORMS;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.v2.FeatureOutput;
import org.n52.io.response.v2.PlatformOutput;
import org.n52.io.response.v2.SeriesMetadataV2Output;
import org.n52.sensorweb.spi.LocaleAwareSortService;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.web.exception.ResourceNotFoundException;
import org.n52.web.exception.WebExceptionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
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

    private ParameterService<FeatureOutput> featureParameterService;

    private ParameterService<SeriesMetadataV2Output> seriesParameterService;

    @RequestMapping(method = GET)
    public ModelAndView getCollection(@RequestParam(required = false) MultiValueMap<String, String> query) {
        IoParameters map = createFromQuery(query);

        if (map.isExpanded()) {
            OutputCollection<?> result = parameterService.getExpandedParameters(map);

            // TODO add paging
            return new ModelAndView().addObject(result);
        } else {
            OutputCollection<?> result = parameterService.getCondensedParameters(map);

            // TODO add paging
            return new ModelAndView().addObject(result);
        }
    }

    @RequestMapping(value = "/{item}", method = GET)
    public ModelAndView getItem(@PathVariable("item") String platformId,
            @RequestParam(required = false) MultiValueMap<String, String> query) {
        IoParameters map = createFromQuery(query);

        // TODO check parameters and throw BAD_REQUEST if invalid
        Object result = parameterService.getParameter(platformId, map);

        if (result == null) {
            throw new ResourceNotFoundException("Found no platform with given id.");
        }

        return new ModelAndView().addObject(result);
    }

    @RequestMapping(value = "/{platformItem}/features", method = GET)
    public ModelAndView getFeatures(@PathVariable("platformItem") String platformId,
            @RequestParam(required = false) MultiValueMap<String, String> query) {
        // TODO check parameters and throw BAD_REQUEST if invalid
        query.add("platform", platformId);
        IoParameters map = createFromQuery(query);
        PlatformOutput platform = parameterService.getParameter(platformId, map);
        if (platform == null) {
            throw new ResourceNotFoundException("Found no platform for given platform id.");
        }

        Object result = null;
        if (map.isExpanded()) {
            result = featureParameterService.getExpandedParameters(map);
        } else {
            result = featureParameterService.getCondensedParameters(map);
        }

        if (result == null) {
            throw new ResourceNotFoundException("Found no feature for given platform id.");
        }

        return new ModelAndView().addObject(result);
    }

    @RequestMapping(value = "/{platformItem}/features/{featureItem}", method = GET)
    public ModelAndView getFeatureItem(@PathVariable("platformItem") String platformId, @PathVariable("featureItem") String featureId,
            @RequestParam(required = false) MultiValueMap<String, String> query) {

        // check parameters and throw BAD_REQUEST if invalid
        MultiValueMap<String, String> platformMap = new LinkedMultiValueMap<>();
        platformMap.add("feature", featureId);
        IoParameters platformQuery = createFromQuery(platformMap);
        PlatformOutput platform = parameterService.getParameter(platformId, platformQuery);
        if (platform == null) {
            throw new ResourceNotFoundException("Found no platform for given platform id.");
        }
        query.add("platform", platformId);
        IoParameters map = createFromQuery(query);
        Object result = featureParameterService.getParameter(featureId, map);

        if (result == null) {
            throw new ResourceNotFoundException("Found no feature with given id for given platfrom id.");
        }

        return new ModelAndView().addObject(result);
    }

    @RequestMapping(value = "/{platformItem}/series", method = GET)
    public ModelAndView getSeries(@PathVariable("platformItem") String platformId,
            @RequestParam(required = false) MultiValueMap<String, String> query) {
        // TODO check parameters and throw BAD_REQUEST if invalid
        query.add("platform", platformId);
        IoParameters map = createFromQuery(query);
        PlatformOutput platform = parameterService.getParameter(platformId, map);
        if (platform == null) {
            throw new ResourceNotFoundException("Found no platform for given platform id.");
        }

        Object result = null;
        if (map.isExpanded()) {
            result = seriesParameterService.getExpandedParameters(map);
        } else {
            result = seriesParameterService.getCondensedParameters(map);
        }

        if (result == null) {
            throw new ResourceNotFoundException("Found no series for given platform id.");
        }

        return new ModelAndView().addObject(result);
    }

    @RequestMapping(value = "/{platformItem}/series/{seriesItem}", method = GET)
    public ModelAndView getSeriesItem(@PathVariable("platformItem") String platformId, @PathVariable("seriesItem") String seriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query) {

        // check parameters and throw BAD_REQUEST if invalid
        MultiValueMap<String, String> platformMap = new LinkedMultiValueMap<>();
        platformMap.add("series", seriesId);
        IoParameters platformQuery = createFromQuery(platformMap);
        PlatformOutput platform = parameterService.getParameter(platformId, platformQuery);
        if (platform == null) {
            throw new ResourceNotFoundException("Found no platform for given platform id.");
        }
        query.add("platform", platformId);
        IoParameters map = createFromQuery(query);
        Object result = seriesParameterService.getParameter(seriesId, map);

        if (result == null) {
            throw new ResourceNotFoundException("Found no series with given id for given platfrom id.");
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

    public void setFeatureParameterService(ParameterService<FeatureOutput> featureParameterService) {
        this.featureParameterService = featureParameterService;
    }

    public void setSeriesParameterService(ParameterService<SeriesMetadataV2Output> seriesParameterService) {
        this.seriesParameterService = seriesParameterService;
    }

}
