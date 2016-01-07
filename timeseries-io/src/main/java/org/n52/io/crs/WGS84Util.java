/**
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.io.crs;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import com.vividsolutions.jts.geom.Point;

/**
 * Some utility methods for WGS84 points. Note that all calculations are being made by assuming a spherical
 * shape of the earth (not elliptic). The {@link WGS84Util#EARTH_MEAN_RADIUS} is used as radius.
 */
public class WGS84Util {

    public static final String EPSG_4326 = "EPSG:4326";

    /**
     * The mean radius of WGS84 ellipse.
     */
    static double EARTH_MEAN_RADIUS = 6371.000;

    /**
     * Calculates the shortest distance between two points on a great circle.
     * 
     * @param a
     *        a point.
     * @param b
     *        another point.
     * @return the shortest distance between point A and point B.
     */
    public static double shortestDistanceBetween(Point a, Point b) {
        double aXinRad = toRadians(a.getX());
        double aYinRad = toRadians(a.getY());
        double bXinRad = toRadians(b.getX());
        double bYinRad = toRadians(b.getY());
        return acos(sin(aYinRad) * sin(bYinRad) + cos(aYinRad) * cos(bYinRad) * cos(aXinRad - bXinRad))
                * EARTH_MEAN_RADIUS;
    }

    /**
     * Calculates the longitude delta for a given distance.
     * 
     * @param latitude
     *        the latitude in radians to calculate the distance delta from.
     * @param distance
     *        the distance in kilometer.
     * @return the longitude delta in degrees.
     */
    public static double getLongitudeDelta(double latitude, double distance) {
        return toDegrees(distance / getLatitutesCircleRadius(latitude)) % 360;
    }

    /**
     * Calculates the latitude delta from a point from a given distance.
     * 
     * @param distance
     *        the distance in kilometer.
     * @return the latitude delta in degrees.
     */
    public static double getLatitudeDelta(double distance) {
        return toDegrees(distance / EARTH_MEAN_RADIUS) % 180;
    }

    /**
     * @param latitude
     *        in degrees in radians.
     * @return the length of the latitude radius.
     */
    static double getLatitutesCircleRadius(double latitude) {
        return EARTH_MEAN_RADIUS * sin(PI / 2 - latitude);
    }

    /**
     * Normalizes given longitude to bounds [-180.0,180.0].
     * 
     * @param longitude
     *        in degrees.
     * @return the normalized longitude.
     */
    public static double normalizeLongitude(double longitude) {
        double asRad = toRadians(longitude);
        if (asRad > PI) {
            return toDegrees( -2 * PI + asRad) % 180;
        }
        else if ( -PI > asRad) {
            return toDegrees(2 * PI + asRad) % 180;
        }
        return longitude;
    }

    /**
     * Normalizes given latitude to bounds [-90.0,90.0].
     * 
     * @param latitude
     *        in degrees.
     * @return the normalized latitude.
     */
    public static double normalizeLatitude(double latitude) {
        double asRad = toRadians(latitude);
        if (asRad > PI / 2) {
            return toDegrees(2 * PI - asRad) % 90;
        }
        else if ( -PI / 2 > asRad) {
            return toDegrees( -2 * PI - asRad) % 90;
        }
        return latitude;
    }

}
