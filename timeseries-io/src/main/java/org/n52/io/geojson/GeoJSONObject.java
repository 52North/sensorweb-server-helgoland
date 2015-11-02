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
package org.n52.io.geojson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 2.0
 */
public class GeoJSONObject {
    
    public enum GeoJSONType {
        Point,
        MultiPoint,
        LineString,
        MultiLineString,
        Polygon,
        MultiPolygon,
        GeometryCollection,
        Feature,
        FeatureCollection;
    	
    }
    
    public static final String LABEL = "label";
    
    private GeoJSONType type;
    
//    private String crs; // TODO
    
//    private double[] bbox; // TODO
    
    @JsonSerialize(keyAs = String.class, contentAs = Object.class)
    private Map<String, Object> members = new HashMap<>();
    
    public GeoJSONObject(String type) {
        this.type = GeoJSONType.valueOf(type);
    }
    
    public void addProperty(String key, Object value) {
        this.members.put(key, value);
    }
    
    public void removeProperty(String key) {
        this.members.remove(key);
    }
    
    @JsonIgnore
    public Object getProperty(String key) {
        return this.members.get(key);
    }
    
    public boolean hasProperty(String key) {
        return this.members.containsKey(key);
    }

    public Map<String, Object> getMembers() {
        return Collections.unmodifiableMap(members);
    }

    public void setMembers(Map<String, Object> members) {
        this.members = members;
    }
    
    public void setType(String type) {
        this.type = GeoJSONType.valueOf(type);
    }
    
    public String getType() {
        return this.type.name();
    }
    
}
