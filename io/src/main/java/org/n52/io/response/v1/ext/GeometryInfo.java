/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.io.response.v1.ext;

import java.util.Map;

import org.n52.io.response.ParameterOutput;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vividsolutions.jts.geom.Geometry;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class GeometryInfo extends ParameterOutput implements CondensedGeometryInfo {

    private String hrefBase;

    private Geometry geometry;

    private PlatformItemOutput platform;

    private final GeometryCategory geometyCategory;

    private Map<String, Object> properties;

    public GeometryInfo(GeometryCategory category) {
        this.geometyCategory = category;
    }

    @JsonIgnore
    @Override
    public String getLabel() {
        return super.getLabel();
    }

    @JsonIgnore
    @Override
    public String getDomainId() {
        return super.getDomainId();
    }

    @Override
    public PlatformItemOutput getPlatform() {
        return platform;
    }

    public void setPlatform(PlatformItemOutput platform) {
        this.platform = platform;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public void setHrefBase(String hrefBase) {
        this.hrefBase = hrefBase;
    }

    @JsonIgnore
    public String getHrefBase() {
        String suffix = getUrlIdSuffix();
        return hrefBase != null && hrefBase.endsWith(suffix)
                ? hrefBase.substring(0, hrefBase.lastIndexOf(suffix) - 1)
                : hrefBase;
    }

    private String getUrlIdSuffix() {
        return getType().getTypeName();
    }

    @JsonIgnore
    public GeometryCategory getType() {
        return geometyCategory;
    }

    public String getGeometyCategory() {
        return geometyCategory.getCategory();
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    public boolean hasProperty(String key) {
        return this.properties.containsKey(key);
    }

}
