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
package org.n52.series.db.da;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.n52.web.exception.ResourceNotFoundException;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.n52.series.db.beans.ServiceInfo;

public class DataRepositoryFactoryTest {

    private DataRepositoryFactory factory;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws URISyntaxException {
        File config = getConfigFile("dataset-factory.properties");
        factory = new DataRepositoryFactory(config);
    }

    @Test
    public void when_createdWithNoConfig_useDefaultConfig() {
        DataRepositoryFactory m = new DataRepositoryFactory();
        assertFalse(m.isKnownEntry("text"));
        assertFalse(m.isKnownEntry("count"));
        assertTrue(m.createRepository("measurement").getClass() == MeasurementDataRepository.class);
    }

    @Test
    public void when_havingInvalidEntry_then_throwException() throws URISyntaxException {
        thrown.expect(ResourceNotFoundException.class);
        thrown.expectMessage(is("No datasets available for 'invalid'."));
        File configFile = getConfigFile("/files/dataset-factory_with-invalid-entries.properties");
        new DataRepositoryFactory(configFile).createRepository("invalid");
    }

    @Test
    public void when_mapToText_then_returnTextDataRepository() {
        assertTrue(factory.createRepository("text").getClass() == TextDataRepository.class);
    }

    @Test
    public void when_mapToText_then_returnCountDataRepository() {
        assertTrue(factory.createRepository("count").getClass() == CountDataRepository.class);
    }

    @Test
    public void when_mapToText_then_returnMeasurementDataRepository() {
        assertTrue(factory.createRepository("measurement").getClass() == MeasurementDataRepository.class);
    }

    @Test
    public void when_instanceCreated_then_nextTimeFromCache() {
        DataRepository instance = factory.createRepository("measurement");
        Assert.assertTrue(factory.hasCacheEntry("measurement"));
        Assert.assertTrue(instance == factory.createRepository("measurement"));
    }
    
    @Test
    public void when_serviceInfoAvailable_then_instanceHasServiceInfo() {
        factory.setServiceInfo(new ServiceInfo());
        DataRepository instance = factory.createRepository("measurement");
        Assert.assertNotNull(instance.getServiceInfo());
    }

    @Test
    public void when_mapToInvalid_then_throwException() {
        thrown.expect(ResourceNotFoundException.class);
        thrown.expectMessage(is("No datasets available for 'invalid'."));
        factory.createRepository("invalid");
    }

    private File getConfigFile(String name) throws URISyntaxException {
        Path root = Paths.get(getClass().getResource("/files").toURI());
        return root.resolve(name).toFile();
    }

}
