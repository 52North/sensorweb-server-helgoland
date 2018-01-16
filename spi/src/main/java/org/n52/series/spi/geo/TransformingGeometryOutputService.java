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
package org.n52.series.spi.geo;

import org.n52.io.request.IoParameters;
import org.n52.io.response.GeometryInfo;
import org.n52.io.response.OutputCollection;
import org.n52.series.spi.srv.ParameterService;

import com.vividsolutions.jts.geom.Geometry;

// TODO consolidate
// -> TransformingGeometryOutputService
// -> TransformingPlatformOutputService
// -> TransformingStationOutputService
public class TransformingGeometryOutputService extends ParameterService<GeometryInfo> {

    private final ParameterService<GeometryInfo> composedService;

    private final TransformationService transformationService;

    public TransformingGeometryOutputService(ParameterService<GeometryInfo> toCompose) {
        this.composedService = toCompose;
        this.transformationService = new TransformationService();
    }

    @Override
    public OutputCollection<GeometryInfo> getExpandedParameters(IoParameters query) {
        return transform(query, composedService.getExpandedParameters(query));
    }

    @Override
    public OutputCollection<GeometryInfo> getCondensedParameters(IoParameters query) {
        return transform(query, composedService.getCondensedParameters(query));
    }

    @Override
    public OutputCollection<GeometryInfo> getParameters(String[] items, IoParameters query) {
        return transform(query, composedService.getParameters(items, query));
    }

    @Override
    public GeometryInfo getParameter(String item, IoParameters query) {
        return transform(composedService.getParameter(item, query), query);
    }

    @Override
    public boolean exists(String id, IoParameters parameters) {
        return composedService.exists(id, parameters);
    }

    private OutputCollection<GeometryInfo> transform(IoParameters query, OutputCollection<GeometryInfo> infos) {
        if (infos != null) {
            for (GeometryInfo info : infos) {
                transformInline(info, query);
            }
        }
        return infos;
    }

    private GeometryInfo transform(GeometryInfo info, IoParameters parameters) {
        transformInline(info, parameters);
        return info;
    }

    private void transformInline(GeometryInfo info, IoParameters parameters) {
        Geometry geometry = transformationService.transform(info.getGeometry(), parameters);
        info.setValue(GeometryInfo.GEOMETRY, geometry, parameters, info::setGeometry);
    }

}
