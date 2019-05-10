/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.io;

import static org.hamcrest.CoreMatchers.is;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DatasetFactoryTest {

    private ConfigTypedFactory<Collection> factory;

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Before
    public void setUp() throws URISyntaxException {
        File config = getConfigFile("dataset-collection-factory.properties");
        factory = createCollectionFactory(config);
    }

    @Test
    public void when_created_then_hasMappings() throws DatasetFactoryException {
        Assert.assertTrue(factory.create("arraylist").getClass() == ArrayList.class);
    }

    @Test
    public void when_created_then_initHaveBeenCalled() throws DatasetFactoryException {
        Assert.assertThat(factory.create("arraylist").isEmpty(), Matchers.is(false));
    }

    @Test
    public void when_createdWithNullConfig_then_configureWithFallback() {
        ConfigTypedFactory<Collection> f = createCollectionFactory(null);
        Assert.assertTrue(f.isKnown("hashmap"));
    }

    @Test
    public void when_havingInvalidEntry_then_throwException() throws URISyntaxException, DatasetFactoryException {
        thrown.expect(DatasetFactoryException.class);
        thrown.expectMessage(is("No datasets available for 'invalid'."));
        File configFile = getConfigFile("dataset-collection-factory-invalid-entry.properties");
        new DefaultIoFactory(configFile).create("invalid");
    }

    @Test
    public void when_creatingOfInvalidType_then_throwException() throws DatasetFactoryException {
        thrown.expect(DatasetFactoryException.class);
        thrown.expectMessage(is("No datasets available for 'invalid'."));
        factory.create("invalid");
    }

    private File getConfigFile(String name) throws URISyntaxException {
        Path root = Paths.get(getClass().getResource("/").toURI());
        return root.resolve(name).toFile();
    }

    private ConfigTypedFactory<Collection> createCollectionFactory(File config) {
        return new ConfigTypedFactory<Collection>(config) {
            @Override
            protected String getFallbackConfigResource() {
                return "/dataset-collection-factory-fallback.properties";
            }

            @Override
            protected Collection initInstance(Collection instance) {
                instance.add(new Object());
                return instance;
            }

            @Override
            protected Class<Collection> getTargetType() {
                return Collection.class;
            }
        };
    }

}
