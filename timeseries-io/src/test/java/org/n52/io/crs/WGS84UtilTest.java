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

import static java.lang.Math.PI;
import static java.lang.Math.toRadians;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;
import static org.n52.io.crs.WGS84Util.EARTH_MEAN_RADIUS;
import static org.n52.io.crs.WGS84Util.EPSG_4326;
import static org.n52.io.crs.WGS84Util.getLatitudeDelta;
import static org.n52.io.crs.WGS84Util.getLatitutesCircleRadius;
import static org.n52.io.crs.WGS84Util.getLongitudeDelta;
import static org.n52.io.crs.WGS84Util.shortestDistanceBetween;

import org.junit.Before;
import org.junit.Test;
import org.n52.io.crs.CRSUtils;
import org.n52.io.crs.WGS84Util;
import org.opengis.referencing.FactoryException;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class WGS84UtilTest {

    private static final double ERROR_DELTA = 0.01;

    private GeometryFactory factory;

    private CRSUtils helper;

    @Before
    public void
            setUp()
    {
        helper = CRSUtils.createEpsgForcedXYAxisOrder();
        factory = helper.createGeometryFactory(EPSG_4326);
    }

    @Test
    public void
    shouldCalculateShortestDistanceFromStatueOfLibertyToEiffelTower() throws FactoryException 
    {
        Point statueOfLiberty = createXYOrderedWgs84Point(-74.0444, 40.6892);
        Point tourDeEiffel = createXYOrderedWgs84Point(2.2945, 48.8583);
        assertThat(shortestDistanceBetween(statueOfLiberty, tourDeEiffel), closeTo(5837.0, 0.5));
    }

    private Point createXYOrderedWgs84Point(double lon, double lat) throws FactoryException {
        return factory.createPoint(helper.createCoordinate(EPSG_4326, lon, lat));
    }
    
    @Test
    public void
            shouldGetLongitudeDelta() throws FactoryException
    {
        assertThat(getLongitudeDelta(toRadians(0), 2 * PI * EARTH_MEAN_RADIUS), closeTo(0, ERROR_DELTA));
        assertThat(getLongitudeDelta(toRadians(0), PI * EARTH_MEAN_RADIUS), closeTo(180, ERROR_DELTA));
    }

    @Test
    public void
            shouldGetLatitudeDelta() throws FactoryException
    {
        assertThat(getLatitudeDelta(0d), closeTo(0, ERROR_DELTA));
    }

    @Test
    public void
            shouldReturnNearZeroDistanceAtPoles() throws FactoryException
    {
        assertThat(getLatitutesCircleRadius(toRadians(90)), closeTo(0.0, ERROR_DELTA));
        assertThat(getLatitutesCircleRadius(toRadians(-90d)), closeTo(0.0, ERROR_DELTA));
        assertThat(getLatitutesCircleRadius(toRadians(270d)), closeTo(0.0, ERROR_DELTA));
    }
    
    @Test public void
    shouldNormlizeLatitudesBiggerThan90Degrees()
    {
        double toNormalize = 91.4;
        double normalized = WGS84Util.normalizeLatitude(toNormalize);
        assertThat(normalized, closeTo(88.6, ERROR_DELTA));
    }
    

    @Test public void
    shouldNormlizeLatitudesSmallerThanMinus90Degrees()
    {
        double toNormalize = -91.4;
        double normalized = WGS84Util.normalizeLatitude(toNormalize);
        assertThat(normalized, closeTo(-88.6, ERROR_DELTA));
    }
    
    @Test public void
    shouldNotChangeZeroLatitudeDegrees()
    {
        double toNormalize = 0;
        double normalized = WGS84Util.normalizeLatitude(toNormalize);
        assertThat(normalized, closeTo(0, ERROR_DELTA));
    }
    
    @Test public void
    shouldNormlizeLongitudesBiggerThan180Degrees()
    {
        double toNormalize = 182.3;
        double normalized = WGS84Util.normalizeLongitude(toNormalize);
        assertThat(normalized, closeTo(-177.7, ERROR_DELTA));
    }
    

    @Test public void
    shouldNormlizeLongitudesSmallerThan180Degrees()
    {
        double toNormalize = -182.3;
        double normalized = WGS84Util.normalizeLongitude(toNormalize);
        assertThat(normalized, closeTo(177.7, ERROR_DELTA));
    }
    
    @Test public void
    shouldNotChangeZeroLongitudeDegrees()
    {
        double toNormalize = 0;
        double normalized = WGS84Util.normalizeLongitude(toNormalize);
        assertThat(normalized, closeTo(0, ERROR_DELTA));
    }

    
//    @Test
//    public void
//            shouldReturnMajorAxisDistanceAtEquator() throws FactoryException
//    {
//        assertThat(getLatitutesCircleRadius(0d), closeTo(MEAN_RADIUS, DELTA));
//    }
//    
//    @Test
//    public void
//    shouldNormalizeLatitude()
//    {
//        assertThat(normalizeLatitude(-95d), closeTo(-85d, DELTA));
//    }

}
