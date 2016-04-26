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
package org.n52.io.response.v2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.n52.io.geojson.GeoJSONObject;
import org.n52.io.geojson.GeoJSONSerializer;
import org.n52.io.response.AbstractOutput;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vividsolutions.jts.geom.Geometry;


@JsonSerialize(using = GeoJSONSerializer.class, as = GeoJSONObject.class)
public class FeatureOutput extends AbstractOutput {

    private final Map<String, Object> members = new HashMap<>();

    private final String featureType;

    private Geometry geometry;

    public FeatureOutput(String featureType) {
        this.featureType = featureType;
    }

    public FeatureOutput(String featureType, Geometry geometry) {
        this(featureType);
        this.geometry = geometry;
    }

    public String getFeatureType() {
        return featureType;
    }

    public void addProperty(String key, Object value) {
        this.members.put(key, value);
    }

    public void removeProperty(String key) {
        this.members.remove(key);
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(members);
    }

    public void setProperties(Map<String, Object> properties) {
        this.members.putAll(properties);
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public boolean isSetGeometry() {
        return getGeometry() != null && !getGeometry().isEmpty();
    }


}
