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

import org.n52.io.response.v1.PhenomenonOutput;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class ExtPhenomenaParameterController extends ExtParameterRequestMappingAdapter<PhenomenonOutput> {

    @RequestMapping(ExtUrlSettings.COLLECTION_CATEGORIES_DEFAULT)
    public ModelAndView getDefaultPhenomena(@RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.PURE_STATION_INSITU_CONCEPT, "true");
        return super.getCollection(query);
    }

    @RequestMapping(ExtUrlSettings.COLLECTION_PHENOMENA_ALL)
    public ModelAndView getAllPhenomena(@RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.INCLUDE_ALL, "true");
        return super.getCollection(query);
    }

    @RequestMapping(ExtUrlSettings.COLLECTION_PHENOMENA_INSITU)
    public ModelAndView getInsituPhenomena(@RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.FILTER_ON_INSITU, "true");
        return super.getCollection(query);
    }

    @RequestMapping(ExtUrlSettings.COLLECTION_PHENOMENA_REMOTE)
    public ModelAndView getRemotePhenomena(@RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.FILTER_ON_REMOTE, "true");
        return super.getCollection(query);
    }

    @RequestMapping(ExtUrlSettings.COLLECTION_PHENOMENA_MOBILE)
    public ModelAndView getMobilePhenomena(@RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.FILTER_ON_MOBILE, "true");
        return super.getCollection(query);
    }

    @RequestMapping(ExtUrlSettings.COLLECTION_PHENOMENA_STATIONARY)
    public ModelAndView getStationaryaPhenomena(@RequestParam(required = false) MultiValueMap<String, String> query) {
        query.add(Parameters.FILTER_ON_STATIONARY, "true");
        return super.getCollection(query);
    }

}
