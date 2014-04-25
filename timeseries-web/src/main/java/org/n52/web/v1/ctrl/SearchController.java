/**
 * Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.web.v1.ctrl;

import static org.n52.web.v1.ctrl.RestfulUrls.SEARCH;

import java.util.Collection;

import org.n52.web.BadRequestException;
import org.n52.web.BaseController;
import org.n52.web.v1.srv.SearchService;
import org.n52.sensorweb.v1.spi.search.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = SEARCH, produces = {"application/json"})
public class SearchController extends BaseController {

    private SearchService searchService;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView searchResources(@RequestParam String q,
                                        @RequestParam(defaultValue="en") String locale) {

        if (q == null) {
            throw new BadRequestException("Use parameter 'q' with search string to define your search term.");
        }

        Collection<SearchResult> result = searchService.searchResources(q, locale);
        return new ModelAndView().addObject(result);
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

}
