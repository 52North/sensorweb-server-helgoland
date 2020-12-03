/*
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.io.crs;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class GeotoolsJTSReferenceTester {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeotoolsJTSReferenceTester.class);

    @Before
    public void setUp() {

    }

    @Test
    public void shouldBlah() throws Exception {
        CRSUtils forcedXYOrder = CRSUtils.createEpsgForcedXYAxisOrder();
        GeometryFactory xyFactory = forcedXYOrder.createGeometryFactory("EPSG:4326");
        Point forcedXYPoint = xyFactory.createPoint(new Coordinate(7.4, 52.3));
        LOGGER.info("EPSG:4326 as JTS point (forced XY): {}", forcedXYPoint);
        LOGGER.info("Transformed to EPSG:25832: {}", forcedXYOrder.transform(forcedXYPoint, "EPSG:4326", "EPSG:25832"));

        CRSUtils respectEpsgOrder = CRSUtils.createEpsgStrictAxisOrder();
        GeometryFactory strictFactory = respectEpsgOrder.createGeometryFactory("EPSG:4326");
        Point strictPoint = strictFactory.createPoint(new Coordinate(52.3, 7.4));
        LOGGER.info("EPSG:4326 as JTS point (strict EPSG order): {}", strictPoint);
        LOGGER.info("Transformed to EPSG:25832: {}", respectEpsgOrder.transform(strictPoint, "EPSG:4326", "EPSG:25832"));
    }

    @Test
    public void shouldCreateCRS84() throws Exception {
        CRSUtils respectEpsgOrder = CRSUtils.createEpsgStrictAxisOrder();
        GeometryFactory strictFactory = respectEpsgOrder.createGeometryFactory("EPSG:4326");
        Point strictPoint = strictFactory.createPoint(new Coordinate(52.3, 7.4));
        LOGGER.info("EPSG:4326 as JTS point (strict EPSG order): {}", strictPoint);
        LOGGER.info("Transformed to CRS:84: {}", respectEpsgOrder.transform(strictPoint, "EPSG:4326", "CRS:84"));
    }

}
