/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.web.ctrl.v1;

import org.n52.io.extension.MetadataExtension;
import org.n52.io.request.IoParameters;
import static org.n52.io.request.QueryParameters.createFromQuery;
import org.n52.io.response.ParameterOutput;
import org.n52.web.ctrl.ParameterController;
import static org.n52.web.ctrl.v1.RestfulUrls.COLLECTION_TIMESERIES;
import org.n52.web.exception.ResourceNotFoundException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping(value = COLLECTION_TIMESERIES)
public class TimeseriesMetadataController extends ParameterController {

    // resource controller for timeseries metadata
    
    @Override
    @RequestMapping(value = "/{item}", method = GET)
    public ModelAndView getItem(@PathVariable("item") String id,
                                @RequestParam(required = false) MultiValueMap<String, String> query) {
        IoParameters queryMap = createFromQuery(query);
        ParameterOutput parameter = addExtensionInfos(getParameterService().getParameter(id, queryMap));
        for (MetadataExtension<ParameterOutput> extension : getMetadataExtensions()) {
            // some extensions have to stay backward compatible
            // these add their infos directly to the metadata output
            extension.getExtras(parameter, queryMap);
        }

        if (parameter == null) {
            throw new ResourceNotFoundException("Found no parameter for id '" + id + "'.");
        }

        return new ModelAndView().addObject(parameter);
    }
}
