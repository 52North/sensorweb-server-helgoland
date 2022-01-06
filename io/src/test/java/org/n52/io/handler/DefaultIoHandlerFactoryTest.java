/*
 * Copyright (C) 2013-2022 52°North Spatial Information Research GmbH
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
package org.n52.io.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.quantity.QuantityDatasetOutput;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.io.type.quantity.QuantityIoFactory;
import org.n52.io.type.text.TextIoFactory;

public class DefaultIoHandlerFactoryTest {

    private ConfigTypedFactory<IoHandlerFactory<DatasetOutput<AbstractValue< ? >>, AbstractValue< ? >>> factory;

    @BeforeEach
    public void setUp() throws URISyntaxException {
        File config = getConfigFile("dataset-io-factory.properties");
        factory = new DefaultIoFactory<>(config);
    }

    @Test
    public void when_createdWithNoConfig_useDefaultConfig() throws DatasetFactoryException {
        ConfigTypedFactory<IoHandlerFactory<QuantityDatasetOutput, QuantityValue>> factory = new DefaultIoFactory<>();
        assertTrue(factory.isKnown("text"));
        assertTrue(factory.create(QuantityValue.TYPE).getClass() == QuantityIoFactory.class);
    }

    @Test
    public void when_mapToText_then_returnTextIoHandler() throws DatasetFactoryException {
        assertTrue(factory.create("text").getClass() == TextIoFactory.class);
    }

    @Test
    public void when_mapToText_then_returnMeasurementDataRepository() throws DatasetFactoryException {
        assertTrue(factory.create(QuantityValue.TYPE).getClass() == QuantityIoFactory.class);
    }

    private File getConfigFile(String name) throws URISyntaxException {
        Path root = Paths.get(getClass().getResource("/").toURI());
        return root.resolve(name).toFile();
    }

}
