/**
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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

import static org.n52.web.v1.ctrl.RestfulUrls.COLLECTION_PROCEDURES;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.n52.io.IoParameters;
import org.n52.io.v1.data.RawFormats;
import org.n52.sensorweb.v1.spi.RawDataService;
import org.n52.web.BadRequestException;
import org.n52.web.InternalServerException;
import org.n52.web.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = COLLECTION_PROCEDURES)
public class ProceduresParameterController extends ParameterController {
	
    private final static Logger LOGGER = LoggerFactory.getLogger(ProceduresParameterController.class);

    @RequestMapping(value = "/{item}", method = GET, params = { RawFormats.RAW_FORMAT })
    public void getRawData(HttpServletResponse response, @PathVariable("item") String id,
            @RequestParam MultiValueMap<String, String> query) {
        if (getParameterService() instanceof RawDataService) {
            IoParameters queryMap = preProcess(query, response);
            InputStream inputStream = ((RawDataService) getParameterService()).getRawData(id, queryMap);
            if (inputStream == null) {
                throw new ResourceNotFoundException("Found no parameter for id '" + id + "'.");
            }
            try {
                IOUtils.copyLarge(inputStream, response.getOutputStream());
            } catch (IOException e) {
                throw new InternalServerException("Error while querying raw procedure data", e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        LOGGER.error("Error while closing InputStream", e);
                    }
                }
            }
        } else {
            throw new BadRequestException(
                    "Querying of raw procedure data is not supported by the underlying service!");
        }
    }

    @Override
    protected void addCacheHeader(IoParameters parameter, HttpServletResponse response) {
        if (parameter.hasCache()
                && parameter.getCache().has(getResourcePathFrom(COLLECTION_PROCEDURES))) {
            addCacheHeader(response, parameter.getCache()
                    .get(getResourcePathFrom(COLLECTION_PROCEDURES)).asLong(0));
        }
    }


}
