/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.io.response.v2;

import org.n52.io.geojson.GeoJSONGeometrySerializer;
import org.n52.io.response.TimeseriesValue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vividsolutions.jts.geom.Geometry;

public class SeriesValue extends TimeseriesValue {

    private static final long serialVersionUID = 4735920096822181576L;
    
    private Geometry geometry;
    
    public SeriesValue() {
        // for serialization
    }
    
    public SeriesValue(long timestamp, Double value) {
        super(timestamp, value);
    }
    
    public SeriesValue(long timestamp, Double value, Geometry geometry) {
        super(timestamp, value);
        this.setGeometry(geometry);
    }

    @JsonSerialize(using = GeoJSONGeometrySerializer.class)
    public Geometry getGeometry() {
        return geometry;
    }

    @JsonIgnore
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
    
    @JsonIgnore
    public boolean isSetGeometry() {
        return geometry != null && !geometry.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString().replace(" ]", ""));
        sb.append(", ");
        if (isSetGeometry()) {
            sb.append("geometry: ").append(geometry);
        }
        return sb.append(" ]").toString();
    }
}
