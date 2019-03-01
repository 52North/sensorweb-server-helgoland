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
package org.n52.io.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.locationtech.jts.geom.Point;
import org.n52.io.crs.BoundingBox;
import org.n52.io.crs.CRSUtils;
import org.n52.io.crs.WGS84Util;
import org.opengis.referencing.FactoryException;

/**
 * Represents the surrounding area based on a center and a radius. All
 * coordinate calculations are based on a EPSG:4326, lon-lat ordered reference
 * frame.
 */
@JsonSerialize(using = VicinitySerializer.class)
@JsonDeserialize(using = VicinityDeserializer.class)
public class Vicinity {

    /**
     * The coordinate reference system. Defaults to
     * {@link CRSUtils#DEFAULT_CRS}.
     */
    private String crs = CRSUtils.DEFAULT_CRS;

    private Point center;

    private double radius;

    Vicinity() {
        // for serialization
    }

    /**
     * @param center the center point.
     * @param radius the distance around the center
     */
    public Vicinity(Point center, Number radius) {
        try {
            this.radius = radius.doubleValue();
            this.center = center;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse radius.");
        }
    }

    /**
     * @return calculates bounding box within WGS84 and strict EPSG axes order
     * context.
     */
    public BoundingBox calculateBounds() {
        return calculateBounds(CRSUtils.createEpsgStrictAxisOrder());
    }

    /**
     * Calculates bounding box with the given CRS context.
     *
     * @param crsUtils the reference context.
     * @return a bounding rectangle.
     * @throws IllegalStateException if invalid crs was set.
     */
    public BoundingBox calculateBounds(CRSUtils crsUtils) {
        double latInRad = Math.toRadians(center.getY());
        final double latitudeDelta = WGS84Util.getLatitudeDelta(radius);
        final double longitudeDelta = WGS84Util.getLongitudeDelta(latInRad, radius);
        double llEasting = WGS84Util.normalizeLongitude(center.getX() - longitudeDelta);
        double llNorthing = WGS84Util.normalizeLatitude(center.getY() - latitudeDelta);
        double urEasting = WGS84Util.normalizeLongitude(center.getX() + longitudeDelta);
        double urNorthing = WGS84Util.normalizeLatitude(center.getY() + latitudeDelta);
        try {
            if (crsUtils.isLatLonAxesOrder(crs)) {
                Point ll = crsUtils.createPoint(llNorthing, llEasting, crs);
                Point ur = crsUtils.createPoint(urNorthing, urEasting, crs);
                return new BoundingBox(ll, ur, crs);
            }
            Point ll = crsUtils.createPoint(llEasting, llNorthing, crs);
            Point ur = crsUtils.createPoint(urEasting, urNorthing, crs);
            return new BoundingBox(ll, ur, crs);
        } catch (FactoryException e) {
            throw new IllegalStateException("Illegal CRS parameter: " + crs, e);
        }
    }

    /**
     * @param crs sets the coordinate reference system, e.g. 'EPSG:25832'
     */
    public void setCrs(String crs) {
        if (crs != null) {
            this.crs = crs;
        }
    }

    public String getCrs() {
        return crs;
    }

    public void setRadius(Number radius) {
        this.radius = radius.doubleValue();
    }

    public double getRadius() {
        return radius;
    }

    /**
     * @param center the center coordinates.
     */
    public void setCenter(Point center) {
        this.center = center;
    }

    public Point getCenter() {
        return center;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [ ");
        sb.append("Center: ").append(center).append(", ");
        sb.append("Radius: ").append(radius).append(" km");
        return sb.append(" ]").toString();
    }

}
