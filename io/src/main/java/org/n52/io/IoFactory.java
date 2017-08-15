/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
import java.util.List;
import java.util.Set;

import org.n52.io.format.ResultTimeFormatter;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestParameterSet;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.series.spi.srv.DataService;
import org.n52.series.spi.srv.ParameterService;

public abstract class IoFactory<D extends Data<V>, P extends DatasetOutput<V, ? >, V extends AbstractValue< ? >> {

    private DataService<D> dataService;

    private ParameterService<P> datasetService;

    private RequestSimpleParameterSet simpleRequest;

    private RequestStyledParameterSet styledRequest;

    private URI basePath;

    public IoFactory<D, P, V> withSimpleRequest(RequestSimpleParameterSet request) {
        this.simpleRequest = request;
        return this;
    }

    public IoFactory<D, P, V> withStyledRequest(RequestStyledParameterSet request) {
        this.styledRequest = request;
        return this;
    }

    public IoFactory<D, P, V> withServletContextRoot(URI contextRoot) {
        this.basePath = contextRoot;
        return this;
    }

    public IoFactory<D, P, V> withDataService(DataService<D> service) {
        this.dataService = service;
        return this;
    }

    public IoFactory<D, P, V> withDatasetService(ParameterService<P> service) {
        this.datasetService = service;
        return this;
    }

    public IoFactory<D, P, V> withBasePath(URI path) {
        this.basePath = path;
        return this;
    }

    public IoHandler<D> createHandler(String outputMimeType) {
        String msg = "The requested media type '" + outputMimeType + "' is not supported.";
        IllegalArgumentException exception = new IllegalArgumentException(msg);
        throw exception;
    }

    public IoProcessChain<D> createProcessChain() {
        return new IoProcessChain<D>() {

            @Override
            public DataCollection<D> getData() {
                return getDataService().getData(getRequestParameters());
            }

            @Override
            public DataCollection< ? > getProcessedData() {
                return getParameters().shallClassifyByResultTimes()
                        ? new ResultTimeFormatter<D>().format(getData())
                        // empty chain
                        : getData();
            }
        };
    }

    public abstract boolean isAbleToCreateHandlerFor(String outputMimeType);

    public abstract Set<String> getSupportedMimeTypes();

    protected IoStyleContext createContext() {
        if (datasetService == null || styledRequest == null) {
            return IoStyleContext.createEmpty();
        }
        return IoStyleContext.createContextWith(styledRequest, getMetadatas());
    }

    protected List<P> getMetadatas() {
        String[] seriesIds = simpleRequest != null
                ? simpleRequest.getDatasets()
                : styledRequest.getDatasets();
        return datasetService.getParameters(seriesIds, getParameters())
                             .getItems();
    }

    protected IoParameters getParameters() {
        return simpleRequest != null
                ? IoParameters.createFromQuery(simpleRequest)
                : IoParameters.createFromQuery(styledRequest);
    }

    protected DataService<D> getDataService() {
        return dataService;
    }

    public RequestParameterSet getRequestParameters() {
        return simpleRequest == null
                ? styledRequest
                : simpleRequest;
    }

    public URI getBasePath() {
        return basePath;
    }

}
