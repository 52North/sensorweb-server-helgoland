/*
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.series.spi.geo;

import org.n52.io.geojson.GeoJSONFeature;
import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.dataset.StationOutput;
import org.n52.series.spi.srv.ParameterService;
import org.n52.series.spi.srv.RawDataService;

/**
 * Composes a {@link ParameterService} for {@link GeoJSONFeature}s to transform
 * geometries to requested spatial reference system.
 *
 * @deprecated since 2.0.0
 */
@Deprecated
// TODO consolidate
// -> TransformingGeometryOutputService
// -> TransformingPlatformOutputService
// -> TransformingStationOutputService
public class TransformingStationOutputService extends ParameterService<StationOutput> {

    private final ParameterService<StationOutput> composedService;

    private final TransformationService transformService;

    public TransformingStationOutputService(ParameterService<StationOutput> toCompose) {
        this.composedService = toCompose;
        this.transformService = new TransformationService();
    }

    @Override
    public OutputCollection<StationOutput> getExpandedParameters(IoParameters query) {
        OutputCollection<StationOutput> features = composedService.getExpandedParameters(query);
        return transformFeatures(query, features);
    }

    @Override
    public OutputCollection<StationOutput> getCondensedParameters(IoParameters query) {
        OutputCollection<StationOutput> features = composedService.getCondensedParameters(query);
        return transformFeatures(query, features);
    }

    @Override
    public OutputCollection<StationOutput> getParameters(String[] items, IoParameters query) {
        OutputCollection<StationOutput> features = composedService.getParameters(items, query);
        return transformFeatures(query, features);
    }

    @Override
    public StationOutput getParameter(String item, IoParameters query) {
        StationOutput feature = composedService.getParameter(item, query);
        transformService.transformInline(feature, query);
        return feature;
    }

    private OutputCollection<StationOutput> transformFeatures(IoParameters query,
            OutputCollection<StationOutput> features) {
        if (features != null) {
            for (StationOutput feature : features) {
                transformService.transformInline(feature, query);
            }
        }
        return features;
    }

    @Override
    public boolean exists(String id, IoParameters parameters) {
        return composedService.exists(id, parameters);
    }

    @Override
    public RawDataService getRawDataService() {
        return composedService.getRawDataService();
    }

    @Override
    public boolean supportsRawData() {
        return composedService.supportsRawData();
    }

}
