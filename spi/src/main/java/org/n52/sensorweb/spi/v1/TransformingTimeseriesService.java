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
package org.n52.sensorweb.spi.v1;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.v1.SeriesMetadataV1Output;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.sensorweb.spi.RawDataService;

@Deprecated
public class TransformingTimeseriesService extends ParameterService<SeriesMetadataV1Output> {

    private final ParameterService<SeriesMetadataV1Output> composedService;

    private final TransformationService transformationService;

    public TransformingTimeseriesService(ParameterService<SeriesMetadataV1Output> toCompose) {
        this.composedService = toCompose;
        this.transformationService = new TransformationService();
    }

    @Override
    public OutputCollection<SeriesMetadataV1Output> getExpandedParameters(IoParameters query) {
        OutputCollection<SeriesMetadataV1Output> metadata = composedService.getExpandedParameters(query);
        return transformStations(query, metadata);
    }

    @Override
    public OutputCollection<SeriesMetadataV1Output> getCondensedParameters(IoParameters query) {
        OutputCollection<SeriesMetadataV1Output> metadata = composedService.getCondensedParameters(query);
        return transformStations(query, metadata);
    }

    @Override
    public OutputCollection<SeriesMetadataV1Output> getParameters(String[] items, IoParameters query) {
        return transformStations(query, composedService.getParameters(items, query));
    }

    @Override
    public SeriesMetadataV1Output getParameter(String timeseriesId, IoParameters query) {
        SeriesMetadataV1Output metadata = composedService.getParameter(timeseriesId, query);
        if (metadata != null) {
            transformationService.transformInline(metadata.getStation(), query);
        }
        return metadata;
    }

    private OutputCollection<SeriesMetadataV1Output> transformStations(IoParameters query, OutputCollection<SeriesMetadataV1Output> metadata) {
        for (SeriesMetadataV1Output timeseriesMetadata : metadata) {
            transformationService.transformInline(timeseriesMetadata.getStation(), query);
        }
        return metadata;
    }

    @Override
    public boolean exists(String id) {
        return composedService.exists(id);
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
