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

import java.io.Serializable;
import org.n52.io.geojson.old.GeojsonCrs;
import org.n52.io.geojson.old.GeojsonPoint;

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

    EastingNorthing(GeojsonPoint point) {
        this(point.getCoordinates(), point.getCrs());
    }

    /**
     * @param easting the east value.
     * @param northing the north value.
     * @param srs the CRS name, e.g. 'EPSG:25832'
     */
    EastingNorthing(Double easting, Double northing, String srs) {
        this(new Double[]{easting, northing}, GeojsonCrs.createNamedCRS(srs));
    }

    /**
     * @param coordinates the coordinates in EPSG:4326
     */
    EastingNorthing(Double[] coordinates) {
        this(coordinates, GeojsonCrs.createNamedCRS(CRSUtils.DEFAULT_CRS));
    }

    EastingNorthing(Double[] coordinates, GeojsonCrs crs) {
        if (coordinates == null) {
            throw new NullPointerException("Coordinates must not null.");
        }
        if (coordinates.length != 2 && coordinates.length != 3) {
            throw new IllegalArgumentException("Coordinates must be either 2- or 3-dimensional.");
        }
        this.crs = crs == null
                ? GeojsonCrs.createNamedCRS(CRSUtils.DEFAULT_CRS)
                : crs;
        easting = coordinates[0];
        northing = coordinates[1];
        if (coordinates.length == 3) {
            altitude = coordinates[2];
        }
    }

    double getEasting() {
        return easting;
    }

    double getNorthing() {
        return northing;
    }

    double getAltitude() {
        return altitude;
    }

    void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    String getCrsDefinition() {
        return crs.getName();
    }

    GeojsonCrs getCrs() {
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
        if (!(obj instanceof EastingNorthing)) {
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
