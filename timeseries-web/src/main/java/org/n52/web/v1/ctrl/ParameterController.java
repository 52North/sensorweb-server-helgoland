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

import static org.n52.io.QueryParameters.createFromQuery;
import static org.n52.web.v1.ctrl.Stopwatch.startStopwatch;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.n52.io.IoParameters;
import org.n52.io.v1.data.ParameterOutput;
import org.n52.web.BaseController;
import org.n52.web.ResourceNotFoundException;
import org.n52.web.v1.srv.LocaleAwareSortService;
import org.n52.web.v1.srv.ParameterService;
import org.n52.web.v1.srv.ServiceParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping(produces = {"application/json"})
public abstract class ParameterController extends BaseController implements RestfulUrls {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterController.class);
    
    private ServiceParameterService serviceParameterService;
    
    private ParameterService<ParameterOutput> parameterService;

    @RequestMapping(method = GET)
    public ModelAndView getCollection(@RequestParam(required=false) MultiValueMap<String, String> query) {
        IoParameters queryMap = createFromQuery(query);

        if (queryMap.isExpanded()) {
            Stopwatch stopwatch = startStopwatch();
            ParameterOutput[] result = parameterService.getExpandedParameters(queryMap);
            LOGGER.debug("Processing request took {} seconds.", stopwatch.stopInSeconds());

            // TODO add paging

            return new ModelAndView().addObject(result);
        }
        else {
            ParameterOutput[] results = parameterService.getCondensedParameters(queryMap);

            // TODO add paging

            return new ModelAndView().addObject(results);
        }
    }

    @RequestMapping(value = "/{item}", method = GET)
    public ModelAndView getItem(@PathVariable("item") String id,
                                @RequestParam(required = false) MultiValueMap<String, String> query) {
        IoParameters queryMap = createFromQuery(query);
        ParameterOutput parameter = parameterService.getParameter(id, queryMap);

        if (parameter == null) {
            throw new ResourceNotFoundException("Found no parameter for id '" + id + "'.");
        }

        return new ModelAndView().addObject(parameter);
    }

    public ServiceParameterService getServiceParameterService() {
        return serviceParameterService;
    }

    public void setServiceParameterService(ServiceParameterService serviceParameterService) {
        this.serviceParameterService = serviceParameterService;
    }

    public ParameterService<ParameterOutput> getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService<ParameterOutput> parameterService) {
        this.parameterService = new LocaleAwareSortService<ParameterOutput>(parameterService);
    }

}
