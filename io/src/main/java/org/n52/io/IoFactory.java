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
package org.n52.io;

import java.net.URI;

import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.v1.ext.DatasetOutput;
import org.n52.series.spi.srv.DataService;
import org.n52.series.spi.srv.ParameterService;

public abstract class IoFactory<T extends Data> {

    private DataService<T> dataService;

    private ParameterService<? extends DatasetOutput> datasetService;

    private RequestSimpleParameterSet simpleRequest;

    private RequestStyledParameterSet styledRequest;

    private URI basePath;

    public IoFactory withSimpleRequest(RequestSimpleParameterSet request) {
        this.simpleRequest = request;
        return this;
    }

    public IoFactory withStyledRequest(RequestStyledParameterSet request) {
        this.styledRequest = request;
        return this;
    }

    public IoFactory withServletContextRoot(URI servletContextRoot) {
        this.basePath = servletContextRoot;
        return this;
    }

    public IoFactory withDataService(DataService<T> dataService) {
        this.dataService = dataService;
        return this;
    }

    public IoFactory withDatasetService(ParameterService<? extends DatasetOutput> datasetService) {
        this.datasetService = datasetService;
        return this;
    }

    public IoFactory withBasePath(URI basePath) {
        this.basePath = basePath;
        return this;
    }

    public IoHandler<T> createHandler(String outputMimeType) {
        String msg = "The requested media type '" + outputMimeType + "' is not supported.";
        IllegalArgumentException exception = new IllegalArgumentException(msg);
        throw exception;
    }

    public IoProcessChain createProcessChain() {
        return new IoProcessChain() {
            @Override
            public DataCollection getData() {
                return getDataService().getData(getSimpleRequest());
            }
            @Override
            public DataCollection<? extends Data> getProcessedData() {
                return getData(); // empty chain
            }
        };
    }

    public abstract boolean isAbleToCreateHandlerFor(String outputMimeType);

    protected IoStyleContext createContext() {
        if (datasetService == null || styledRequest == null) {
            return IoStyleContext.createEmpty();
        }
        OutputCollection<? extends DatasetOutput> metadatas = getMetadatas(styledRequest.getSeriesIds());
        return IoStyleContext.createContextWith(styledRequest, metadatas.getItems());
    }

    private OutputCollection<? extends DatasetOutput> getMetadatas(String[] seriesIds) {
        return datasetService.getParameters(seriesIds, getParameters());
    }

    protected IoParameters getParameters() {
        return simpleRequest != null
                ? IoParameters.createFromQuery(simpleRequest)
                : IoParameters.createFromQuery(styledRequest);
    }

    protected DataService<T> getDataService() {
        return dataService;
    }

    public RequestSimpleParameterSet getSimpleRequest() {
        return simpleRequest;
    }

    public URI getBasePath() {
        return basePath;
    }

}
