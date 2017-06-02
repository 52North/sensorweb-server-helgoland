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

import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.n52.io.request.IoParameters;

import org.n52.io.request.Parameters;
import org.n52.io.request.QueryParameters;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.pagination.OffsetBasedPagination;
import org.n52.io.response.pagination.Paginated;
import org.n52.series.spi.srv.CountingMetadataService;
import org.n52.series.spi.srv.RawFormats;
import org.n52.web.common.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping(method = RequestMethod.GET, produces = {
    "application/json"
})
public abstract class ParameterRequestMappingAdapter<T extends ParameterOutput> extends ParameterController<T> {

    @Autowired
    @Qualifier("metadataService")
    private CountingMetadataService counter;

    @Override
    @RequestMapping(path = "")
    public ModelAndView getCollection(HttpServletResponse response,
                                      @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE) String locale,
                                      @RequestParam MultiValueMap<String, String> query) {
        String lim = "limit";
        String off = "offset";
        IoParameters queryMap = QueryParameters.createFromQuery(query);
        queryMap = IoParameters.ensureBackwardsCompatibility(queryMap);
        if (queryMap.containsParameter(lim) || queryMap.containsParameter(off)) {
            Integer elementcount = this.getElementCount(queryMap.removeAllOf(lim).removeAllOf(off));
            if (elementcount != -1) {
                OffsetBasedPagination obp = new OffsetBasedPagination(queryMap.getOffset(), queryMap.getLimit());
                Paginated<T> paginated = new Paginated(obp, elementcount.longValue());
                this.addPagingHeaders(this.getCollectionPath(this.getHrefBase()), response, paginated);
            }
        }
        return super.getCollection(null, locale, addHrefBase(query));
    }

    @Override
    @RequestMapping(value = "/{item}")
    public ModelAndView getItem(@PathVariable("item") String id,
                                @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE) String locale,
                                @RequestParam MultiValueMap<String, String> query) {
        RequestUtils.overrideQueryLocaleWhenSet(locale, query);
        return super.getItem(id, locale, addHrefBase(query));
    }

    @Override
    @RequestMapping(value = "/{item}", params = {
        RawFormats.RAW_FORMAT
    })
    public void getRawData(HttpServletResponse response,
                           @PathVariable("item") String id,
                           @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE) String locale,
                           @RequestParam MultiValueMap<String, String> query) {
        super.getRawData(response, id, locale, addHrefBase(query));
    }

    @Override
    @RequestMapping(value = "/{item}/extras")
    public Map<String, Object> getExtras(@PathVariable("item") String resourceId,
                                         @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE) String locale,
                                         @RequestParam(required = false) MultiValueMap<String, String> query) {
        RequestUtils.overrideQueryLocaleWhenSet(locale, query);
        return super.getExtras(resourceId, locale, addHrefBase(query));
    }

    protected MultiValueMap<String, String> addHrefBase(MultiValueMap<String, String> query) {
        query.put(Parameters.HREF_BASE, Collections.singletonList(getHrefBase()));
        return query;
    }

    private String getHrefBase() {
        return RequestUtils.resolveQueryLessRequestUrl(getExternalUrl());
    }

    protected abstract Integer getElementCount(IoParameters queryMap);

    protected CountingMetadataService getEntityCounter() {
        return counter;
    }

    private HttpServletResponse addPagingHeaders(String href, HttpServletResponse response, Paginated paginated) {
        String l = "Link:";
        if (paginated.getCurrent().isPresent()) {
            response.addHeader(l, "<" + href + "?" + paginated.getCurrent().get().toString() + "> rel=\"self\"");
        }
        if (paginated.getNext().isPresent()) {
            response.addHeader(l, "<" + href + "?" + paginated.getNext().get().toString() + "> rel=\"next\"");
        }
        if (paginated.getPrevious().isPresent()) {
            response.addHeader(l, "<" + href + "?" + paginated.getPrevious().get().toString() + "> rel=\"previous\"");
        }
        if (paginated.getFirst().isPresent()) {
            response.addHeader(l, "<" + href + "?" + paginated.getFirst().get().toString() + "> rel=\"first\"");
        }
        if (paginated.getLast().isPresent()) {
            response.addHeader(l, "<" + href + "?" + paginated.getLast().get().toString() + "> rel=\"last\"");
        }

        return response;
    }
}
