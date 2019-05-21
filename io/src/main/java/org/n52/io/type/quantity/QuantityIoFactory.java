/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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

package org.n52.io.type.quantity;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.n52.io.Constants;
import org.n52.io.format.ResultTimeFormatter;
import org.n52.io.handler.CsvIoHandler;
import org.n52.io.handler.IoHandler;
import org.n52.io.handler.IoHandlerFactory;
import org.n52.io.handler.IoProcessChain;
import org.n52.io.handler.SimpleCsvIoHandler;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.quantity.QuantityDatasetOutput;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.io.type.quantity.format.FormatterFactory;
import org.n52.io.type.quantity.generalize.GeneralizingQuantityService;
import org.n52.io.type.quantity.handler.img.ChartIoHandler;
import org.n52.io.type.quantity.handler.img.MultipleChartsRenderer;
import org.n52.io.type.quantity.handler.report.PDFReportGenerator;
import org.n52.series.spi.srv.DataService;

public final class QuantityIoFactory extends IoHandlerFactory<QuantityDatasetOutput, QuantityValue> {

    private static final Constants.MimeType[] MIME_TYPES = new Constants.MimeType[] {
        Constants.MimeType.TEXT_CSV,
        Constants.MimeType.IMAGE_PNG,
        Constants.MimeType.APPLICATION_ZIP,
        Constants.MimeType.APPLICATION_PDF,
    };

    @Override
    public Set<String> getSupportedMimeTypes() {
        return Stream.of(MIME_TYPES)
                     .map(Constants.MimeType::getMimeType)
                     .collect(Collectors.toSet());
    }


    @Override
    public IoProcessChain<Data<QuantityValue>> createProcessChain() {
        return new IoProcessChain<Data<QuantityValue>>() {
            @Override
            public DataCollection<Data<QuantityValue>> getData() {
                boolean generalize = getParameters().isGeneralize();
                DataService<Data<QuantityValue>> dataService = generalize
                        ? new GeneralizingQuantityService(getDataService())
                        : getDataService();
                return dataService.getData(getParameters());
            }

            @Override
            public DataCollection< ? > getProcessedData() {
                return getParameters().shallClassifyByResultTimes()
                        ? new ResultTimeFormatter<Data<QuantityValue>>().format(getData())
                        : createFormatter().create()
                                           .format(getData());
            }

            private FormatterFactory createFormatter() {
                return FormatterFactory.createFormatterFactory(getParameters());
            }
        };
    }

    @Override
    public IoHandler<Data<QuantityValue>> createHandler(String outputMimeType) {
        IoParameters parameters = getParameters();
        Constants.MimeType mimeType = Constants.MimeType.toInstance(outputMimeType);
        if (mimeType == Constants.MimeType.IMAGE_PNG) {
            return createMultiChartRenderer(mimeType);
        } else if (mimeType == Constants.MimeType.APPLICATION_PDF) {
            ChartIoHandler imgRenderer = createMultiChartRenderer(mimeType);
            return new PDFReportGenerator(parameters, createProcessChain(), imgRenderer);
        } else if (isCsvOutput(mimeType)) {
            CsvIoHandler<QuantityValue> handler = new SimpleCsvIoHandler<>(parameters,
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

    private MultipleChartsRenderer createMultiChartRenderer(Constants.MimeType mimeType) {
        MultipleChartsRenderer chartRenderer = new MultipleChartsRenderer(
                                                                          getParameters(),
                                                                          createProcessChain(),
                                                                          createContext());

        chartRenderer.setMimeType(mimeType);
        return chartRenderer;
    }

}
