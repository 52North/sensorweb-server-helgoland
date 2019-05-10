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

import com.vividsolutions.jts.geom.Point;

/**
 * Some utility methods for WGS84 points. Note that all calculations are being
 * made by assuming a spherical shape of the earth (not elliptic). The
 * {@link WGS84Util#EARTH_MEAN_RADIUS} is used as radius.
 */
public class WGS84Util {

    public static final String EPSG_4326 = "EPSG:4326";

    /**
     * The mean radius of WGS84 ellipse.
     */
    protected static final double EARTH_MEAN_RADIUS = 6371.000;

    /**
     * Calculates the shortest distance between two points on a great circle.
     *
     * @param a a point.
     * @param b another point.
     * @return the shortest distance between point A and point B.
     */
    public static double shortestDistanceBetween(Point a, Point b) {
        double aXinRad = Math.toRadians(a.getX());
        double aYinRad = Math.toRadians(a.getY());
        double bXinRad = Math.toRadians(b.getX());
        double bYinRad = Math.toRadians(b.getY());
        double aProd = Math.sin(aYinRad) * Math.sin(bYinRad);
        double bProd = Math.cos(aYinRad) * Math.cos(bYinRad) * Math.cos(aXinRad - bXinRad);
        return Math.acos(aProd + bProd) * EARTH_MEAN_RADIUS;
    }

    /**
     * Calculates the longitude delta for a given distance.
     *
     * @param latitude the latitude in radians to calculate the distance delta
     * from.
     * @param distance the distance in kilometer.
     * @return the longitude delta in degrees.
     */
    public static double getLongitudeDelta(double latitude, double distance) {
        return Math.toDegrees(distance / getLatitutesCircleRadius(latitude)) % 360;
    }

    /**
     * Calculates the latitude delta from a point from a given distance.
     *
     * @param distance the distance in kilometer.
     * @return the latitude delta in degrees.
     */
    public static double getLatitudeDelta(double distance) {
        return Math.toDegrees(distance / EARTH_MEAN_RADIUS) % 180;
    }

    /**
     * @param latitude in degrees in radians.
     * @return the length of the latitude radius.
     */
    static double getLatitutesCircleRadius(double latitude) {
        return EARTH_MEAN_RADIUS * Math.sin(Math.PI / 2 - latitude);
    }

    /**
     * Normalizes given longitude to bounds [-180.0,180.0].
     *
     * @param longitude in degrees.
     * @return the normalized longitude.
     */
    public static double normalizeLongitude(double longitude) {
        double asRad = Math.toRadians(longitude);
        if (asRad > Math.PI) {
            return Math.toDegrees(-2 * Math.PI + asRad) % 180;
        } else if (-Math.PI > asRad) {
            return Math.toDegrees(2 * Math.PI + asRad) % 180;
        }
        return longitude;
    }

    /**
     * Normalizes given latitude to bounds [-90.0,90.0].
     *
     * @param latitude in degrees.
     * @return the normalized latitude.
     */
    public static double normalizeLatitude(double latitude) {
        double asRad = Math.toRadians(latitude);
        if (asRad > Math.PI / 2) {
            return Math.toDegrees(2 * Math.PI - asRad) % 90;
        } else if (-Math.PI / 2 > asRad) {
            return Math.toDegrees(-2 * Math.PI - asRad) % 90;
        }
        return latitude;
    }

}
