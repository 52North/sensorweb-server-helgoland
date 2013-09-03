/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.io.crs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.n52.io.crs.CRSUtils.EPSG_4326;
import static org.n52.io.geojson.GeojsonPoint.createWithCoordinates;

import org.junit.Before;
import org.junit.Test;
import org.n52.io.geojson.GeojsonCrs;
import org.n52.io.geojson.GeojsonPoint;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

public class CRSUtilsTest {

    private CRSUtils referenceHelper;

    private BoundingBox bbox;

    @Before
    public void setUp() throws Exception {
        referenceHelper = CRSUtils.createEpsgStrictAxisOrder();
        EastingNorthing ll = new EastingNorthing(6.4, 51.9, EPSG_4326);
        EastingNorthing ur = new EastingNorthing(8.9, 53.4, EPSG_4326);
        bbox = new BoundingBox(ll, ur);
    }

    @Test
    public void testIsStationContainedByBBox() throws NoSuchAuthorityCodeException,
            FactoryException,
            TransformException {
        GeojsonPoint stationWithin = getStationWithinBBox();
        GeojsonPoint stationOutside = getStationOutsideBBox();
        assertTrue(referenceHelper.isContainedByBBox(bbox, stationWithin));
        assertFalse(referenceHelper.isContainedByBBox(bbox, stationOutside));
    }

    private GeojsonPoint getStationWithinBBox() {
        // TODO make random station within bbox
        // TODO add different epsg codes!
        GeojsonPoint point = createWithCoordinates(new Double[]{7.0, 52.0});
        point.setCrs(GeojsonCrs.createNamedCRS(EPSG_4326));
        return point;
    }

    private GeojsonPoint getStationOutsideBBox() {
        // TODO make random station within bbox
        // TODO add different epsg codes!
        GeojsonPoint point = createWithCoordinates(new Double[]{10.4, 52.0});
        point.setCrs(GeojsonCrs.createNamedCRS(EPSG_4326));
        return point;
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
