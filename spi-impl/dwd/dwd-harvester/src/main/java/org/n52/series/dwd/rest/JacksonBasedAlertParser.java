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
package org.n52.series.dwd.rest;

import java.io.IOException;
import java.io.InputStream;

import org.n52.series.dwd.AlertParser;
import org.n52.series.dwd.ParseException;
import org.n52.series.dwd.store.AlertStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonBasedAlertParser implements AlertParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonBasedAlertParser.class);

    private final ObjectMapper objectMapper;

    public JacksonBasedAlertParser() {
        this(new ObjectMapper());
    }

    public JacksonBasedAlertParser(ObjectMapper om) {
        this.objectMapper = om;
    }

    ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public void parse(InputStream stream, AlertStore store) throws ParseException {
        try {
            AlertCollection c = objectMapper.readValue(stream, AlertCollection.class);
            store.updateCurrentAlerts(c);
        } catch (IOException e) {
            LOGGER.warn("Unable to parse from input stream!", e);
            throw new ParseException("Parsing input was not possible.", e);
        }
    }

}
