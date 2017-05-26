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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.n52.io.request.IoParameters;

import org.n52.io.request.Parameters;
import org.n52.io.request.QueryParameters;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.pagination.OffsetBasedPagination;
import org.n52.io.response.pagination.Paginated;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.da.EntityCounter;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.spi.srv.RawFormats;
import org.n52.web.common.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private EntityCounter counter;

    @Override
    @RequestMapping(path = "")
    public ModelAndView getCollection(HttpServletResponse response,
                                      @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE) String locale,
                                      @RequestParam MultiValueMap<String, String> query) {
        query = addHrefBase(query);
        IoParameters queryMap = QueryParameters.createFromQuery(query);
        if (queryMap.containsParameter("limit") || queryMap.containsParameter("offset")){
            try {
                OffsetBasedPagination impl = new OffsetBasedPagination(queryMap.getOffset(), queryMap.getLimit());
                Paginated<T> paginated = new Paginated(impl, this.getElementCount(queryMap));
                this.addPagingHeaders(this.getCollectionPath(queryMap.getHrefBase()), response, paginated);
            } catch (DataAccessException ex) {
                //TODO(specki): Better Solution?
                // Stop Paging

            }
        }
    return super.getCollection(null, locale, query);
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
        String externalUrl = getExternalUrl();
        String hrefBase = RequestUtils.resolveQueryLessRequestUrl(externalUrl);
        query.put(Parameters.HREF_BASE, Collections.singletonList(hrefBase));
        return query;
    }

    protected abstract int getElementCount(IoParameters queryMap) throws DataAccessException;

    protected EntityCounter getEntityCounter(){
        return counter;
    }

    private HttpServletResponse addPagingHeaders(String href, HttpServletResponse response, Paginated paginated ){

        if (paginated.getCurrent().isPresent()) {
            response.addHeader("Link:","<" + href + "?" + paginated.getCurrent().get().toString() +"> rel=\"self\"");
        }
        if (paginated.getFirst().isPresent()) {
            response.addHeader("Link:","<" + href + "?" + paginated.getFirst().get().toString() +"> rel=\"first\"");
        }
        if (paginated.getLast().isPresent()) {
            response.addHeader("Link:","<" + href + "?" + paginated.getLast().get().toString() +"> rel=\"last\"");
        }
        if (paginated.getNext().isPresent()) {
            response.addHeader("Link:","<" + href + "?" + paginated.getNext().get().toString() +"> rel=\"next\"");
        }
        if (paginated.getPrevious().isPresent()) {
            response.addHeader("Link:","<" + href + "?" + paginated.getPrevious().get().toString() +"> rel=\"previous\"");
        }
        return response;
    }
}
