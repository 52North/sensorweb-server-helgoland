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
package org.n52.series.dwd;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.n52.series.dwd.store.InMemoryAlertStore;

public class GeometryHarvesterTest {

    private static final String TEST_SHAPE_FILE = "geometries/DWD-PVW-Customer_VG2500_extract.shp";
    private InMemoryAlertStore store;
    
    @Before
    public void setUp() {
        store = new InMemoryAlertStore();
    }

    @Test
    public void when_notYetHarvested_then_emptyStore() throws IOException, URISyntaxException {
        ShapeFileHarvester harvester = new ShapeFileHarvester(getTestShapeFile(), store);
        assertTrue(harvester.loadGeometries().size() == 1);
    }
    
    @Test
    public void when_harvestingTestShape_then_muensterWarnCellHasGeometry() throws URISyntaxException {
        new ShapeFileHarvester(getTestShapeFile(), store).harvest();
        assertTrue(store.getWarnCell("105515000").getGeometry() != null);
        assertFalse(store.getWarnCell("105515000").getGeometry().isEmpty());
    }

    private File getTestShapeFile() throws URISyntaxException {
        Path root = Paths.get(getClass().getResource("/").toURI());
        return root.resolve(TEST_SHAPE_FILE).toFile();
    }

}
