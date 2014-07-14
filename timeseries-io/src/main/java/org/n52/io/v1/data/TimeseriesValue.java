/**
 * Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.io.v1.data;

import java.io.Serializable;
import org.n52.io.geojson.GeojsonPoint;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class TimeseriesValue implements Comparable<TimeseriesValue>, Serializable {

    private static final long serialVersionUID = -7292181682632614697L;

    private Long timestamp;

    private Double value;
    
    private GeojsonPoint geomvalue;

    public TimeseriesValue() {
        // for serialization
    }

    public TimeseriesValue(long timestamp, Double value) {
        this.timestamp = timestamp;
        this.value = value;
    }
    
    public TimeseriesValue(long timestamp, GeojsonPoint geomvalue) {
        this.timestamp = timestamp;
        this.geomvalue = geomvalue;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
    
    public void setGeomvalue(GeojsonPoint geomvalue){
    	this.geomvalue = geomvalue;
    }
    
    public GeojsonPoint getGeomvalue(){
    	return geomvalue;
    }
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TimeseriesValue [ ");
        sb.append("timestamp: ").append(timestamp).append(", ");
        sb.append("value: ").append(value);
        return sb.append(" ]").toString();
    }

    @Override
    public int compareTo(TimeseriesValue o) {
        return getTimestamp().compareTo(o.getTimestamp());
    }
}