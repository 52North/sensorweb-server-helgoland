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
package org.n52.io.response.dataset;

import java.util.HashMap;
import java.util.Map;

import org.n52.io.geojson.FeatureOutputSerializer;
import org.n52.io.geojson.GeoJSONFeature;
import org.n52.io.geojson.GeoJSONObject;
import org.n52.io.response.AbstractOutput;
import org.n52.io.response.OptionalOutput;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 * @deprecated since 2.0.0
 */
@Deprecated
@JsonSerialize(using = FeatureOutputSerializer.class, as = GeoJSONObject.class)
public class StationOutput extends AbstractOutput implements GeoJSONFeature {

    public static final String TIMESERIES = "timeseries";
    public static final String PROPERTIES = "properties";
    public static final String GEOMETRY = "geometry";

    private OptionalOutput<Map<String, DatasetParameters>> timeseries;

    private OptionalOutput<Geometry> geometry;

    public Map<String, DatasetParameters> getTimeseries() {
        return getIfSerialized(timeseries);
    }

    public void setTimeseries(OptionalOutput<Map<String, DatasetParameters>> timeseries) {
        this.timeseries = timeseries;
    }

    @Override
    public Geometry getGeometry() {
        return getIfSerialized(geometry);
    }

    @Override
    public void setGeometry(OptionalOutput<Geometry> geometry) {
        this.geometry = geometry;
    }

    @Override
    public boolean isSetGeometry() {
        return isSet(geometry) && geometry.isSerialize();
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        nullSafePut("label", getLabel(), properties);
        nullSafePut("domainId", getDomainId(), properties);
        nullSafePut("href", getHref(), properties);
        nullSafePut("rawFormats", getRawFormats(), properties);
        nullSafePut(TIMESERIES, getTimeseries(), properties);
        return properties;
    }

    private void nullSafePut(String key, Object value, Map<String, Object> container) {
        if (value != null) {
            container.put(key, value);
        }
    }

}
