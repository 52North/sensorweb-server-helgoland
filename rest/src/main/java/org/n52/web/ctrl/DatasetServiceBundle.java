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
package org.n52.web.ctrl;

import java.net.URI;

import org.n52.io.IoHandler;
import org.n52.io.MimeType;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestParameterSet;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.dataset.DataCollection;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.web.exception.WebExceptionAdapter;
import org.n52.sensorweb.spi.DataService;

public abstract class DatasetServiceBundle {

    private ParameterService metadataService;

    private DataService dataService;

    public ParameterService getMetadataService() {
        return metadataService;
    }

    public void setMetadataService(ParameterService seriesMetadataService) {
        this.metadataService = new WebExceptionAdapter(seriesMetadataService);
    }

    public DataService getDataService() {
        return dataService;
    }

    public void setDataService(DataService seriesDataService) {
        this.dataService = seriesDataService;
    }

    public abstract IoHandler getIoHandler(RequestStyledParameterSet parameters, IoParameters map, MimeType mimeType, URI uri);

    public abstract IoHandler getIoHandler(RequestParameterSet parameters, IoParameters map, MimeType mimeType, URI uri);

    public abstract IoHandler getIoHandler(RequestStyledParameterSet parameters, IoParameters map);

    public abstract IoHandler getIoHandler(RequestSimpleParameterSet parameters, IoParameters map);

    public abstract DataCollection<?> format(DataCollection seriesData, String format);

    public abstract DataCollection getSeriesData(RequestSimpleParameterSet parameters);
}
