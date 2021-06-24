/*
 * Copyright (C) 2013-2021 52°North Spatial Information Research GmbH
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
package org.n52.io.response;

import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.geom.Geometry;
import org.n52.io.geojson.FeatureOutputSerializer;
import org.n52.io.geojson.GeoJSONFeature;
import org.n52.io.geojson.GeoJSONObject;
import org.n52.io.response.dataset.DatasetParameters;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = FeatureOutputSerializer.class, as = GeoJSONObject.class)
public class FeatureOutput extends HierarchicalParameterOutput<FeatureOutput> implements GeoJSONFeature {

    public static final String COLLECTION_PATH = "features";
    public static final String PROPERTIES = "properties";
    public static final String GEOMETRY = "geometry";
    public static final String DATASETS = "datasets";
    public static final String PARAMETERS = "parameters";

    private OptionalOutput<Geometry> geometry;
    private OptionalOutput<Map<String, DatasetParameters>> datasets;
    private OptionalOutput<Map<String, Object>> parameters;

    @Override
    public String getCollectionName() {
        return COLLECTION_PATH;
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

    public Map<String, DatasetParameters> getDatasets() {
        return getIfSerialized(datasets);
    }

    public void setDatasets(OptionalOutput<Map<String, DatasetParameters>> datasets) {
        this.datasets = datasets;
    }

    public void setParameters(OptionalOutput< Map<String, Object>> parameters) {
        this.parameters = parameters;
    }

    public  Map<String, Object> getParameters() {
        return getIfSerialized(parameters);
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        nullSafePut("label", getLabel(), properties);
        nullSafePut("domainId", getDomainId(), properties);
        nullSafePut("href", getHref(), properties);
        nullSafePut(EXTRAS, getExtras(), properties);
        nullSafePut(DATASETS, getDatasets(), properties);
        nullSafePut(PARENTS, getParents(), properties);
        nullSafePut(CHILDREN, getChildren(), properties);
        nullSafePut(PARAMETERS, getParameters(), properties);
        return properties;
    }

    private void nullSafePut(String key, Object value, Map<String, Object> container) {
        if (value != null) {
            container.put(key, value);
        }
    }


}
