/*
 * Copyright (C) 2013-2022 52°North Spatial Information Research GmbH
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
package org.n52.web.ctrl;

import org.n52.io.I18N;
import org.n52.io.request.IoParameters;
import org.n52.io.response.GeometryOutput;
import org.n52.series.spi.geo.TransformingGeometryOutputService;
import org.n52.series.spi.srv.CountingMetadataService;
import org.n52.series.spi.srv.ParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = UrlSettings.COLLECTION_GEOMETRIES)
public class GeometriesController extends ParameterRequestMappingAdapter<GeometryOutput> {

    @Autowired
    public GeometriesController(CountingMetadataService counter, ParameterService<GeometryOutput> parameterService) {
        super(counter, new TransformingGeometryOutputService(parameterService));
    }

    @Override
    public String getCollectionName() {
        return UrlSettings.COLLECTION_GEOMETRIES;
    }

    @Override
    protected String getResource() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getDescription(I18N i18n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Long getSize(IoParameters parameters) {
        // TODO Auto-generateadsfDASFd method stub
        return null;
    }



}
