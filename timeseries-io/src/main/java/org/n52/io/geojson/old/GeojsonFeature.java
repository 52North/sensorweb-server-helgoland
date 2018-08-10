/**
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.io.geojson.old;

import java.util.HashMap;
import java.util.Map;

public class GeojsonFeature extends GeojsonObject {

    private static final long serialVersionUID = 863297394860249486L;

    private static final String GEOJSON_TYPE_FEATURE = "Feature";
    
    protected Map<String, Object> properties = null;
    
    private GeojsonPoint geometry; // XXX should be GeojsonGeometry, but generics are different here 
    
    public String getType() {
        return GEOJSON_TYPE_FEATURE;
    }
    
    public GeojsonPoint getGeometry() {
        return geometry;
    }

    public void setGeometry(GeojsonPoint geometry) {
        this.geometry = geometry;
    }
    
    public void addProperty(String property, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        properties.put(property, value);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
}
