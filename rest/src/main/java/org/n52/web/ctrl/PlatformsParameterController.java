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
package org.n52.web.ctrl;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Map;

import org.n52.io.request.Parameters;
import org.n52.io.response.v1.ext.PlatformOutput;
import org.n52.io.response.v1.ext.PlatformType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping(path = UrlSettings.COLLECTION_PLATFORMS, method = GET, produces = {"application/json"})
public class PlatformsParameterController extends ParameterRequestMappingAdapter<PlatformOutput> {

    @Override
    @RequestMapping(path = "")
    public ModelAndView getCollection(@RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.FILTER_PLATFORM_TYPES, "all");
        return super.getCollection(query);
    }

    /* ******************************************************************
                        REMOTE REQUEST MAPPINGS
     * ******************************************************************/

    // TODO remote

    /* ******************************************************************
                        STATIONARY REQUEST MAPPINGS
     * ******************************************************************/

    @RequestMapping(path = "/stationary")
    public ModelAndView getStationaryCollection(@RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.FILTER_PLATFORM_TYPES, "stationary");
        return super.getCollection(query);
    }

    @RequestMapping(path = "/stationary/insitu")
    public ModelAndView getStationaryInsituCollection(@RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.FILTER_PLATFORM_TYPES, "stationary");
        query.add(Parameters.FILTER_PLATFORM_TYPES, "insitu");
        return super.getCollection(query);
    }

    @RequestMapping(path = "/stationary/insitu/{id}")
    public ModelAndView getStationaryInsituItem(@PathVariable("id") String id,
            @RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.FILTER_PLATFORM_TYPES, "stationary");
        query.add(Parameters.FILTER_PLATFORM_TYPES, "insitu");
        return super.getItem(PlatformType.STATIONARY_INSITU.createId(id), query);
    }

    @RequestMapping(path = "/stationary/insitu/{id}/extras")
    public Map<String, Object> getStationaryInsituItemExtras(String id, MultiValueMap<String, String> query) {
        return super.getExtras(PlatformType.MOBILE_INSITU.createId(id), query);
    }

    /* ******************************************************************
                        MOBILE REQUEST MAPPINGS
     * ******************************************************************/

    @RequestMapping(path = "/mobile")
    public ModelAndView getMobileCollection(@RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.FILTER_PLATFORM_TYPES, "mobile");
        return super.getCollection(query);
    }

    @RequestMapping(method = GET, path = "/mobile/insitu")
    public ModelAndView getMobileInsituCollection(@RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.FILTER_PLATFORM_TYPES, "mobile");
        query.add(Parameters.FILTER_PLATFORM_TYPES, "insitu");
        return super.getCollection(query);
    }

    @RequestMapping(method = GET, path = "/mobile/insitu/{id}")
    public ModelAndView getMobileInsituItem(@PathVariable("id") String id,
            @RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.FILTER_PLATFORM_TYPES, "mobile");
        query.add(Parameters.FILTER_PLATFORM_TYPES, "insitu");
        return super.getItem(PlatformType.MOBILE_INSITU.createId(id), query);
    }

    @RequestMapping(method = GET, path = "/mobile/insitu/{id}/extras")
    public Map<String, Object> getMobileInsituItemExtras(String id, MultiValueMap<String, String> query) {
        return super.getExtras(PlatformType.MOBILE_INSITU.createId(id), query);
    }

}
