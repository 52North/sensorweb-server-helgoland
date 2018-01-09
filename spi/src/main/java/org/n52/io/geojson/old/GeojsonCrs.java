/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.io.geojson.old;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class GeojsonCrs extends GeojsonObject {

    private static final long serialVersionUID = 5964748458745655509L;

    private static final String PROPERTY_NAME = "name";

    private static final String TYPE_NAME = PROPERTY_NAME;

    private Map<String, String> properties;

    private String type = TYPE_NAME;

    GeojsonCrs() {
        this.properties = new HashMap<>();
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    void setType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    @JsonIgnore
    public String getName() {
        return properties.get(PROPERTY_NAME);
    }

    public static GeojsonCrs createNamedCRS(String name) {
        if (name == null) {
            throw new NullPointerException("Argument 'name' must not be null.");
        }
        GeojsonCrs namedCrs = new GeojsonCrs();
        namedCrs.addProperty(PROPERTY_NAME, name);
        namedCrs.setType(TYPE_NAME);
        return namedCrs;
    }

}
