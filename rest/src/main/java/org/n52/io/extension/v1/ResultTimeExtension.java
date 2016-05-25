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
package org.n52.io.extension.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import org.n52.io.response.ext.MetadataExtension;
import org.n52.io.response.v1.ext.SeriesMetadataOutput;
import org.n52.io.request.IoParameters;
import org.n52.io.response.ParameterOutput;
import org.n52.sensorweb.spi.ResultTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultTimeExtension extends MetadataExtension<SeriesMetadataOutput> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResultTimeExtension.class);

    private static final String CONFIG_FILE = "/config-extension-resultTime.json";

    private static final String EXTENSION_NAME = "resultTime";

    private final ResultTimeExtensionConfig config = readConfig();

    private ResultTimeService resultTimeService;

    private ResultTimeExtensionConfig readConfig() {
        try (InputStream taskConfig = getClass().getResourceAsStream(CONFIG_FILE);) {
            ObjectMapper om = new ObjectMapper();
            return om.readValue(taskConfig, ResultTimeExtensionConfig.class);
        } catch (IOException e) {
            LOGGER.error("Could not load {}. Using empty config.", CONFIG_FILE, e);
            return new ResultTimeExtensionConfig();
        }
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public void addExtraMetadataFieldNames(SeriesMetadataOutput output) {
        final ParameterOutput service = output.getSeriesParameters().getService();
        if (isAvailableFor(service.getId())) {
            output.addExtra(EXTENSION_NAME);
        }
    }

    private boolean isAvailableFor(String serviceId) {
        return config.getServices().contains(serviceId);
    }

    @Override
    public Map<String, Object> getExtras(SeriesMetadataOutput output, IoParameters parameters) {
        if ( hasExtrasToReturn(output, parameters)) {
            return wrapSingleIntoMap(getResultTimes(parameters, output));
        }
        return Collections.<String, Object>emptyMap();
    }

    private ArrayList<String> getResultTimes(IoParameters parameters, SeriesMetadataOutput output) {
        return resultTimeService.getResultTimeList(parameters, output.getId());
    }

    private boolean hasExtrasToReturn(SeriesMetadataOutput output, IoParameters parameters) {
        return super.hasExtrasToReturn(output, parameters)
                && hasResultTimeRequestParameter(parameters);
    }

    private static boolean hasResultTimeRequestParameter(IoParameters parameters) {
        return parameters.containsParameter("request")
                && parameters.getOther("request").equalsIgnoreCase(EXTENSION_NAME);
    }

    public ResultTimeService getResultTimeService() {
        return resultTimeService;
    }

    public void setResultTimeService(ResultTimeService resultTimeService) {
        this.resultTimeService = resultTimeService;
    }

}
