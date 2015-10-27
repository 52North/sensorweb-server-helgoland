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
package org.n52.io.extension.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.n52.io.extension.MetadataExtension;
import org.n52.io.request.IoParameters;
import org.n52.io.response.v1.TimeseriesMetadataOutput;
import org.n52.sensorweb.spi.ResultTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultTimeExtension implements MetadataExtension<TimeseriesMetadataOutput> {

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
    public Object getExtras(TimeseriesMetadataOutput output, IoParameters parameters) {
        if (parameters.getOther("request") != null && parameters.getOther("request").equals(EXTENSION_NAME)) {
            return resultTimeService.getResultTimeList(parameters, output.getId());
        }
        return null;
    }

    @Override
    public void addExtensionTo(TimeseriesMetadataOutput output) {
        if (enabled(output.getParameters().getService().getId())) {
            output.addExtra(EXTENSION_NAME);
        }
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }
    
    private boolean enabled(String serviceId) {
        return config.getServices().contains(serviceId);
    }

    public ResultTimeService getResultTimeService() {
        return resultTimeService;
    }

    public void setResultTimeService(ResultTimeService resultTimeService) {
        this.resultTimeService = resultTimeService;
    }

}
