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

import org.n52.sensorweb.spi.ParameterService;
import org.n52.io.request.IoParameters;
import org.n52.io.response.v1.StationOutput;

/**
 * Composes a {@link ParameterService} for {@link StationOutput}s to transform geometries to requested spatial
 * reference system.
 */
public class TransformingStationService extends TransformationService implements ParameterService<StationOutput> {

    private final ParameterService<StationOutput> composedService;

    public TransformingStationService(ParameterService<StationOutput> toCompose) {
        this.composedService = toCompose;
    }

    @Override
    public StationOutput[] getExpandedParameters(IoParameters query) {
        StationOutput[] stations = composedService.getExpandedParameters(query);
        return transformStations(query, stations);
    }

    @Override
    public StationOutput[] getCondensedParameters(IoParameters query) {
        StationOutput[] stations = composedService.getCondensedParameters(query);
        return transformStations(query, stations);
    }

    @Override
    public StationOutput[] getParameters(String[] items) {
        StationOutput[] stations = composedService.getParameters(items);
        return transformStations(IoParameters.createDefaults(), stations);
    }

    @Override
    public StationOutput[] getParameters(String[] items, IoParameters query) {
        StationOutput[] stations = composedService.getParameters(items, query);
        return transformStations(query, stations);
    }

    @Override
    public StationOutput getParameter(String item) {
        StationOutput station = composedService.getParameter(item);
        transformInline(station, IoParameters.createDefaults());
        return station;
    }

    @Override
    public StationOutput getParameter(String item, IoParameters query) {
        StationOutput station = composedService.getParameter(item, query);
        transformInline(station, query);
        return station;
    }

}
