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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.n52.io.request.Parameters;
import org.n52.io.response.ParameterOutput;
import org.n52.io.v1.data.RawFormats;
import org.n52.web.common.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping(method = GET, produces = {"application/json"})
public abstract class ParameterRequestMappingAdapter<T extends ParameterOutput> extends ParameterController<T>  {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterRequestMappingAdapter.class);

    @Override
    @RequestMapping(path = "")
    public ModelAndView getCollection(
            @RequestParam MultiValueMap<String, String> query) {
        return super.getCollection(addHrefBase(query));
    }

    @Override
    @RequestMapping(value = "/{item}")
    public ModelAndView getItem(@PathVariable("item") String id,
            @RequestParam MultiValueMap<String, String> query) {
        return super.getItem(id, addHrefBase(query));
    }

    @Override
    @RequestMapping(value = "/{item}", params = {RawFormats.RAW_FORMAT})
    public void getRawData(HttpServletResponse response,
            @PathVariable("item") String id,
            @RequestParam MultiValueMap<String, String> query) {
        super.getRawData(response, id, addHrefBase(query));
    }

    @Override
    @RequestMapping(value = "/{item}/extras")
    public Map<String, Object> getExtras(@PathVariable("item") String resourceId,
            @RequestParam(required = false) MultiValueMap<String, String> query) {
        return super.getExtras(resourceId, addHrefBase(query));
    }

    protected MultiValueMap<String, String> addHrefBase(MultiValueMap<String, String> query) {
        try {
            String externalUrl = getExternalUrl();
            String hrefBase = RequestUtils.resolveQueryLessRequestUrl(externalUrl);
            query.put(Parameters.HREF_BASE, Collections.singletonList(hrefBase));
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("could not resolve href base URL.", e);
        }
        return query;
    }

}
