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

import static org.n52.io.crs.CRSUtils.DEFAULT_CRS;
import static org.n52.io.geojson.GeojsonCrs.createNamedCRS;

import java.io.Serializable;

import org.n52.io.geojson.GeojsonCrs;
import org.n52.io.geojson.GeojsonPoint;

class EastingNorthing implements Serializable {

    private static final long serialVersionUID = 4080241800833286545L;

    private double easting;
    private double northing;
    private double altitude;
    private GeojsonCrs crs;

    @SuppressWarnings("unused")
    private EastingNorthing() {
        // client requires class to be default instantiable
    }

    public EastingNorthing(GeojsonPoint point) {
        this(point.getCoordinates(), point.getCrs());
    }

    /**
     * @param easting
     *        the east value.
     * @param northing
     *        the north value.
     * @param srs
     *        the CRS name, e.g. 'EPSG:25832'
     */
    public EastingNorthing(Double easting, Double northing, String srs) {
        this(new Double[] {easting, northing}, createNamedCRS(srs));
    }

    /**
     * @param coordinates
     *        the coordinates in EPSG:4326
     */
    public EastingNorthing(Double[] coordinates) {
        this(coordinates, createNamedCRS(DEFAULT_CRS));
    }

    public EastingNorthing(Double[] coordinates, GeojsonCrs crs) {
        if (coordinates == null) {
            throw new NullPointerException("Coordinates must not null.");
        }
        if (coordinates.length != 2 && coordinates.length != 3) {
            throw new IllegalArgumentException("Coordinates must be either 2- or 3-dimensional.");
        }
        this.crs = crs == null ? createNamedCRS(DEFAULT_CRS) : crs;
        easting = coordinates[0];
        northing = coordinates[1];
        if (coordinates.length == 3) {
            altitude = coordinates[2];
        }
    }

    public double getEasting() {
        return easting;
    }

    public double getNorthing() {
        return northing;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public String getCrsDefinition() {
        return crs.getName();
    }

    public GeojsonCrs getCrs() {
        return crs;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(altitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(easting);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(northing);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if ( ! (obj instanceof EastingNorthing)) {
            return false;
        }
        EastingNorthing other = (EastingNorthing) obj;
        if (Double.doubleToLongBits(altitude) != Double.doubleToLongBits(other.altitude)) {
            return false;
        }
        if (Double.doubleToLongBits(easting) != Double.doubleToLongBits(other.easting)) {
            return false;
        }
        if (Double.doubleToLongBits(northing) != Double.doubleToLongBits(other.northing)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName()).append(" [ ");
        sb.append("Easting: ").append(easting).append(", ");
        sb.append("Northing: ").append(northing).append(", ");
        sb.append("Altitude: ").append(altitude).append(" ]");
        return sb.toString();
    }
}
