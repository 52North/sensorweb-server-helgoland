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
package org.n52.io.extension.resulttime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.io.request.IoParameters;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.extension.MetadataExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ResultTimeExtension extends MetadataExtension<DatasetOutput> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ResultTimeExtension.class);

    private static final String CONFIG_FILE = "/config-extension-resultTime.json";

    private static final String EXTENSION_NAME = "resultTimes";

    private final List<String> enabledServices = readEnabledServices();

    private ResultTimeService service;

    private List<String> readEnabledServices() {
        try (InputStream taskConfig = getClass().getResourceAsStream(CONFIG_FILE);) {
            ObjectMapper om = new ObjectMapper();
            return Arrays.asList(om.readValue(taskConfig, String[].class));
        } catch (IOException e) {
            LOGGER.error("Could not load {}. Using empty config.", CONFIG_FILE, e);
            return Collections.emptyList();
        }
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public void addExtraMetadataFieldNames(DatasetOutput output) {
        final ParameterOutput service = output.getSeriesParameters().getService();
        if (isAvailableFor(service.getId())) {
            output.addExtra(EXTENSION_NAME);
        }
    }

    private boolean isAvailableFor(String serviceId) {
        return enabledServices.contains(serviceId);
    }

    @Override
    public Map<String, Object> getExtras(DatasetOutput output, IoParameters parameters) {
        return wrapSingleIntoMap(getResultTimes(parameters, output));
    }

    private Set<String> getResultTimes(IoParameters parameters, DatasetOutput output) {
        return service.getResultTimeList(parameters, output.getId());
    }

    public ResultTimeService getService() {
        return service;
    }

    public void setService(ResultTimeService resultTimeService) {
        this.service = resultTimeService;
    }

}
