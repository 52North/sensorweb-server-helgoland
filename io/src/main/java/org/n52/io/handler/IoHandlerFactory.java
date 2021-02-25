/*
 * Copyright (C) 2013-2021 52°North Initiative for Geospatial Open Source
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
package org.n52.io.handler;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.n52.io.Constants;
import org.n52.io.IoStyleContext;
import org.n52.io.format.ResultTimeFormatter;
import org.n52.io.handler.simple.SimpleCsvIoHandler;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.series.spi.srv.DataService;
import org.n52.series.spi.srv.ParameterService;

public abstract class IoHandlerFactory<P extends DatasetOutput<V>, V extends AbstractValue< ? >> {

    private IoParameters parameters;

    private DataService<Data<V>> dataService;

    private ParameterService<P> datasetService;

    public IoHandlerFactory() {
        this.parameters = IoParameters.createDefaults();
    }

    public IoHandlerFactory<P, V> setParameters(IoParameters parameters) {
        this.parameters = parameters;
        return this;
    }

    public IoHandlerFactory<P, V> setDataService(DataService<Data<V>> dataService) {
        this.dataService = dataService;
        return this;
    }

    public IoHandlerFactory<P, V> setDatasetService(ParameterService<P> datasetService) {
        this.datasetService = datasetService;
        return this;
    }

    public IoHandler<Data<V>> createHandler(String outputMimeType) {
        Constants.MimeType mimeType = Constants.MimeType.toInstance(outputMimeType);
        if (isCsvOutput(mimeType)) {
            SimpleCsvIoHandler<V> handler = new SimpleCsvIoHandler<>(parameters,
                                                                     createProcessChain(),
                                                                     getMetadatas());

            boolean zipOutput = parameters.getAsBoolean(Parameters.ZIP, false);
            handler.setZipOutput(zipOutput || mimeType == Constants.MimeType.APPLICATION_ZIP);
            return handler;
        }

        String msg = "The requested media type '" + outputMimeType + "' is not supported.";
        IllegalArgumentException exception = new IllegalArgumentException(msg);
        throw exception;
    }

    public IoProcessChain<Data<V>> createProcessChain() {
        return new IoProcessChain<Data<V>>() {

            @Override
            public DataCollection<Data<V>> getData() {
                return getDataService().getData(parameters);
            }

            @Override
            public DataCollection< ? > getProcessedData() {
                return parameters.shallClassifyByResultTimes()
                        ? new ResultTimeFormatter<Data<V>>().format(getData())
                        // empty chain
                        : getData();
            }
        };
    }

    boolean isAbleToCreateHandlerFor(String outputMimeType) {
        return Constants.MimeType.isKnownMimeType(outputMimeType)
                && supportsMimeType(Constants.MimeType.toInstance(outputMimeType));
    }

    private boolean supportsMimeType(Constants.MimeType mimeType) {
        Set<String> supportedMimeTypes = getSupportedMimeTypes();
        return supportedMimeTypes.contains(mimeType.getMimeType());
    }

    public Set<String> getSupportedMimeTypes() {
        return Stream.of(Constants.MimeType.TEXT_CSV,
                         Constants.MimeType.APPLICATION_ZIP)
                     .map(Constants.MimeType::getMimeType)
                     .collect(Collectors.toSet());
    }

    protected IoStyleContext createContext() {
        if (datasetService == null || !parameters.hasStyles()) {
            return IoStyleContext.createEmpty();
        }
        return IoStyleContext.createContextWith(parameters, getMetadatas());
    }

    protected List<P> getMetadatas() {
        String[] datasetIds = parameters.getDatasets()
                                        .toArray(new String[0]);
        return datasetService.getParameters(datasetIds, parameters)
                             .getItems();
    }

    protected IoParameters getParameters() {
        return parameters;
    }

    protected DataService<Data<V>> getDataService() {
        return dataService;
    }

    protected boolean isCsvOutput(Constants.MimeType mimeType) {
        return mimeType == Constants.MimeType.TEXT_CSV || mimeType == Constants.MimeType.APPLICATION_ZIP;
    }

}
