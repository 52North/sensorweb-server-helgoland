/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.series.db.beans;

import org.n52.io.crs.CRSUtils;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
/**
 * @author henning
 *
 */
public class GeometryEntity {

    private static final Logger LOGGER  = LoggerFactory.getLogger(GeometryEntity.class);

    private final CRSUtils crsUtils = CRSUtils.createEpsgForcedXYAxisOrder();

    private Geometry geometry;

    private Double lon;

    private Double lat;

    private Double alt;

    public boolean isSetGeometry() {
        return geometry != null && !geometry.isEmpty();
    }

    /**
     * Returns the {@link Geometry}. Expects that a geometry with a valid SRID is available. Otherwise use
     * {@link #getGeometry(String)} to obtain a geometry with spatial reference.
     *
     * @return the geometry
     */
    public Geometry getGeometry() {
        return getGeometry(null);
    }

    public GeometryEntity setGeometry(Geometry geometry) {
        this.geometry = geometry;
        return this;
    }

    /**
     * Returns the {@link Geometry} or creates a {@link Geometry} with the given srid in case of geometry has
     * been set via lat/lon.
     *
     * @param srid
     *        the spatial reference
     * @return the geometry or a created geometry (with given srid)
     */
    public Geometry getGeometry(String srid) {
        Geometry g =  isSetLonLat()
            ? crsUtils.createPoint(lon, lat, alt, srid)
            : geometry;
        try {
            return g != null && srid != null
                    ? crsUtils.transformOuterToInner(g, srid)
                    : g;
        } catch (FactoryException | TransformException e) {
            LOGGER.warn("Invalid srid '{}'. Could not transform geometry.", e);
            return g;
        }
    }

    public boolean isSetLonLat() {
        return lon != null && lat != null;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getAlt() {
        return alt;
    }

    public void setAlt(Double alt) {
        this.alt = alt;
    }

    public boolean isEmpty() {
        return !isSetGeometry() && !isSetLonLat();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [");
        sb.append(" latitude: ").append(getLat());
        sb.append(", longitude: ").append(getLon());
        return sb.append(" ]").toString();
    }

}
