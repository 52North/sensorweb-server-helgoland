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
package org.n52.sensorweb.spi.v1;

import org.n52.io.geojson.GeojsonFeature;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.io.request.IoParameters;
import org.n52.io.response.v1.StationOutput;

/**
 * Composes a {@link ParameterService} for {@link GeojsonFeature}s to transform geometries to requested spatial
 * reference system.
 */
public class TransformingGeojsonOutputService extends TransformationService implements ParameterService<GeojsonFeature> {

    private final ParameterService<GeojsonFeature> composedService;

    public TransformingGeojsonOutputService(ParameterService<GeojsonFeature> toCompose) {
        this.composedService = toCompose;
    }

    @Override
    public StationOutput[] getExpandedParameters(IoParameters query) {
        GeojsonFeature[] features = composedService.getExpandedParameters(query);
        return transformStations(query, features);
    }

    @Override
    public GeojsonFeature[] getCondensedParameters(IoParameters query) {
        GeojsonFeature[] features = composedService.getCondensedParameters(query);
        return transformStations(query, features);
    }

    @Override
    public GeojsonFeature[] getParameters(String[] items) {
        GeojsonFeature[] features = composedService.getParameters(items);
        return transformStations(IoParameters.createDefaults(), features);
    }

    @Override
    public GeojsonFeature[] getParameters(String[] items, IoParameters query) {
        GeojsonFeature[] features = composedService.getParameters(items, query);
        return transformStations(query, features);
    }

    @Override
    public GeojsonFeature getParameter(String item) {
        GeojsonFeature feature = composedService.getParameter(item);
        transformInline(feature, IoParameters.createDefaults());
        return feature;
    }

    @Override
    public GeojsonFeature getParameter(String item, IoParameters query) {
        GeojsonFeature feature = composedService.getParameter(item, query);
        transformInline(feature, query);
        return feature;
    }

}
