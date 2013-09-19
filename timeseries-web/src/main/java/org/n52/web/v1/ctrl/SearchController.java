/**
 * ﻿Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.web.v1.ctrl;

import static org.n52.web.v1.ctrl.RestfulUrls.SEARCH;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.n52.io.v1.data.search.SearchResult;
import org.n52.web.BadRequestException;
import org.n52.web.BaseController;
import org.n52.web.v1.srv.SearchService;
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
    public ModelAndView searchResources(HttpServletResponse response, @RequestParam String q) {

        if (q == null) {
            throw new BadRequestException("Use parameter 'q' with search string to define your search term.");
        }

        Collection<SearchResult> resultedResources = searchService.searchResources(q);
        
        return new ModelAndView().addObject(resultedResources);
    }

    public SearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

}
