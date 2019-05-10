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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.n52.io.crs.CRSUtils.DEFAULT_CRS;
import static org.n52.io.geojson.old.GeojsonPoint.createWithCoordinates;

import org.junit.Before;
import org.junit.Test;
import org.n52.io.geojson.old.GeojsonPoint;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import com.vividsolutions.jts.geom.Point;

public class CRSUtilsTest {

    private CRSUtils referenceHelper;

    private BoundingBox bbox;

    @Before
    public void setUp() throws Exception {
        referenceHelper = CRSUtils.createEpsgStrictAxisOrder();
        Point ll = referenceHelper.createPoint(6.4, 51.9, DEFAULT_CRS);
        Point ur = referenceHelper.createPoint(8.9, 53.4, DEFAULT_CRS);
//        EastingNorthing ll = new EastingNorthing(6.4, 51.9, DEFAULT_CRS);
//        EastingNorthing ur = new EastingNorthing(8.9, 53.4, DEFAULT_CRS);
        bbox = new BoundingBox(ll, ur, DEFAULT_CRS);
    }

    @Test
    public void shouldIndicateLatLonOrder() throws FactoryException {
        referenceHelper = CRSUtils.createEpsgStrictAxisOrder();
        assertThat(referenceHelper.isLatLonAxesOrder("EPSG:4326"), is(true));
    }

    @Test
    public void shouldIndicateLonLatOrder() throws FactoryException {
        referenceHelper = CRSUtils.createEpsgForcedXYAxisOrder();
        assertThat(referenceHelper.isLatLonAxesOrder("EPSG:4326"), is(false));
    }

    @Test
    public void testIsStationContainedByBBox() throws NoSuchAuthorityCodeException,
            FactoryException,
            TransformException {
        GeojsonPoint stationWithin = getStationWithinBBox();
        GeojsonPoint stationOutside = getStationOutsideBBox();
        assertTrue(bbox.contains(referenceHelper.convertToPointFrom(stationWithin)));
        assertFalse(bbox.contains(referenceHelper.convertToPointFrom(stationOutside)));
    }

    private GeojsonPoint getStationWithinBBox() {
        // TODO make random station within bbox
        // TODO add different epsg codes!
        return createWithCoordinates(new Double[]{7.0, 52.0});
    }

    private GeojsonPoint getStationOutsideBBox() {
        // TODO make random station within bbox
        // TODO add different epsg codes!
        return createWithCoordinates(new Double[]{10.4, 52.0});
    }

    @Test
    public void testGetSrsIdFromEPSG() {
        assertValidCodeFromEpsg(4326, "4326");
        assertValidCodeFromEpsg(4326, "EPSG:4326");
        assertValidCodeFromEpsg(4326, "epsg:4326");
        assertValidCodeFromEpsg(4326, "epsg::4326");
        assertValidCodeFromEpsg(4326834, "ePsG:4326834");
        assertValidCodeFromEpsg(4326, "ogc:def:ref:epsg:4.7:4326");
    }

    private void assertValidCodeFromEpsg(int expected, String code) {
        assertEquals("Unexpected EPSG code!", expected, referenceHelper.getSrsIdFromEPSG(code));
    }

    @Test
    public void testExtractSRSCode() {
        String smallCaseUrn = "urn:ogc:def:crs:epsg::4326";
        String capitalCaseUrn = "URN:OGC:DEF:CRS:EPSG:3.5:4326";
        String mixedCaseUrn = "UrN:OfC:dEf:crs:EPSG::4323426";
        String capitalEpsgLink = "http://www.opengis.net/def/crs/EPSG/0/4324336";
        String smallCaseEpsgLink = "http://www.opengis.net/def/crs/epsg/0/4326";
        assertValidEpsgShortCut("EPSG:4326", smallCaseUrn);
        assertValidEpsgShortCut("EPSG:4326", capitalCaseUrn);
        assertValidEpsgShortCut("EPSG:4323426", mixedCaseUrn);
        assertValidEpsgShortCut("EPSG:4324336", capitalEpsgLink);
        assertValidEpsgShortCut("EPSG:4326", smallCaseEpsgLink);
    }

    private void assertValidEpsgShortCut(String expected, String epsgCode) {
        assertEquals("Unexpected EPSG string!", expected, referenceHelper.extractSRSCode(epsgCode));
    }

    // TODO add tests for creating coordinates
    // TODO add tests for transform coordinates
}
