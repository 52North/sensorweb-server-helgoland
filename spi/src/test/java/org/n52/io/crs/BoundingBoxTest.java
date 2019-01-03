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
package org.n52.io.crs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class BoundingBoxTest {

    @Test
    public void testContainingGeometryIsContained() throws ParseException {
        Point ll = (Point) createGeometry("POINT (0 0)");
        Point ur = (Point) createGeometry("POINT (10 10)");
        BoundingBox bbox = new BoundingBox(ll, ur, "EPSG:4326");

        Geometry overlappingPolygon = createGeometry("POLYGON ((0 1, 9 0, 3 3, 0 1))");
        assertTrue("geometry is not contained by bbox", bbox.contains(overlappingPolygon));
    }

    @Test
    public void testOverlappingGeometryIsNotContained() throws ParseException {
        Point ll = (Point) createGeometry("POINT (0 0)");
        Point ur = (Point) createGeometry("POINT (10 10)");
        BoundingBox bbox = new BoundingBox(ll, ur, "EPSG:4326");

        Geometry overlappingPolygon = createGeometry("POLYGON ((0 1, 11 0, 3 3, 0 1))");
        assertFalse("geometry is contained by bbox", bbox.contains(overlappingPolygon));
    }

    private Geometry createGeometry(String wkt) throws ParseException {
        WKTReader wktReader = new WKTReader(new GeometryFactory());
         return wktReader.read(wkt);
    }

}
