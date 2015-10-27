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
package org.n52.sensorweb.spi.v2;

import org.n52.io.geojson.GeoJSONFeature;
import org.n52.io.geojson.old.GeojsonFeature;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.io.request.IoParameters;
import org.n52.sensorweb.spi.TransformationService;

/**
 * Composes a {@link ParameterService} for {@link GeojsonFeature}s to transform geometries to requested spatial
 * reference system.
 */
public class TransformingGeometryOutputService extends TransformationService implements ParameterService<GeoJSONFeature> {

    private final ParameterService<GeoJSONFeature> composedService;

    public TransformingGeometryOutputService(ParameterService<GeoJSONFeature> toCompose) {
        this.composedService = toCompose;
    }

    @Override
    public GeoJSONFeature[] getExpandedParameters(IoParameters query) {
        GeoJSONFeature[] features = composedService.getExpandedParameters(query);
        return transformFeatures(query, features);
    }

    @Override
    public GeoJSONFeature[] getCondensedParameters(IoParameters query) {
        GeoJSONFeature[] features = composedService.getCondensedParameters(query);
        return transformFeatures(query, features);
    }

    @Override
    public GeoJSONFeature[] getParameters(String[] items) {
        GeoJSONFeature[] features = composedService.getParameters(items);
        return transformFeatures(IoParameters.createDefaults(), features);
    }

    @Override
    public GeoJSONFeature[] getParameters(String[] items, IoParameters query) {
        GeoJSONFeature[] features = composedService.getParameters(items, query);
        return transformFeatures(query, features);
    }

    @Override
    public GeoJSONFeature getParameter(String item) {
        GeoJSONFeature feature = composedService.getParameter(item);
        transformInline(feature.getGeometry(), IoParameters.createDefaults());
        return feature;
    }

    @Override
    public GeoJSONFeature getParameter(String item, IoParameters query) {
        GeoJSONFeature feature = composedService.getParameter(item, query);
        transformInline(feature.getGeometry(), query);
        return feature;
    }
    
    private GeoJSONFeature[] transformFeatures(IoParameters query, GeoJSONFeature[] features) {
        if (features != null) {
            for (GeoJSONFeature feature : features) {
                transformInline(feature.getGeometry(), query);
            }
        }
        return features;
    }

}
