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
package org.n52.web.ctrl.v1.ext;

import org.n52.io.request.Parameters;
import org.n52.web.ctrl.ParameterController;
import static org.n52.web.ctrl.v1.ext.ExtUrlSettings.COLLECTION_PLATFORMS;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping(value = COLLECTION_PLATFORMS)
public class PlatformsParameterController extends ParameterController {

    @Override
    @RequestMapping(method = GET)
    public ModelAndView getCollection(@RequestParam(required = false) MultiValueMap<String, String> query) {

        // TODO mobile_remote
        // TODO stationary_remote

        query.add(Parameters.PLATFORMS_INCLUDE_ALL, "true");
        return super.getCollection(query);
    }

    @RequestMapping(method = GET, path = "/stationary_insitu")
    public ModelAndView getStationaryInsituPlatforms(@RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.PLATFORMS_INCLUDE_INSITU, "true");
        query.add(Parameters.PLATFORMS_INCLUDE_STATIONARY, "true");
        return super.getCollection(query);
    }

    @RequestMapping(method = GET, path = "/stationary_insitu/{id}")
    public ModelAndView getStationaryInsituPlatform(@PathVariable("id") String id,
            @RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.PLATFORMS_INCLUDE_INSITU, "true");
        query.add(Parameters.PLATFORMS_INCLUDE_STATIONARY, "true");
        return super.getItem("stationary_insitu/" + id, query);
    }

    @RequestMapping(method = GET, path = "/mobile_insitu")
    public ModelAndView getMobileInsituPlatforms(@RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.PLATFORMS_INCLUDE_INSITU, "true");
        query.add(Parameters.PLATFORMS_INCLUDE_MOBILE, "true");
        return super.getCollection(query);
    }

    @RequestMapping(method = GET, path = "/mobile_insitu/{id}")
    public ModelAndView getMobileInsituPlatform(@PathVariable("id") String id,
            @RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.PLATFORMS_INCLUDE_INSITU, "true");
        query.add(Parameters.PLATFORMS_INCLUDE_MOBILE, "true");
        return super.getItem("mobile_insitu/" + id, query);
    }

}
