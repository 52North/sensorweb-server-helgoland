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
package org.n52.io.quantity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.n52.io.Constants;
import org.n52.io.IoFactory;
import org.n52.io.IoHandler;
import org.n52.io.IoProcessChain;
import org.n52.io.csv.quantity.QuantityCsvIoHandler;
import org.n52.io.format.ResultTimeFormatter;
import org.n52.io.format.quantity.FormatterFactory;
import org.n52.io.generalize.quantity.GeneralizingQuantityService;
import org.n52.io.img.quantity.ChartIoHandler;
import org.n52.io.img.quantity.MultipleChartsRenderer;
import org.n52.io.report.quantity.PDFReportGenerator;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.quantity.QuantityDatasetOutput;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.series.spi.srv.DataService;

public final class QuantityIoFactory extends IoFactory<QuantityDatasetOutput, QuantityValue> {

    private static final List<Constants.MimeType> SUPPORTED_MIMETYPES = Arrays.asList(
            new Constants.MimeType[] {
                Constants.MimeType.TEXT_CSV,
                Constants.MimeType.IMAGE_PNG,
                Constants.MimeType.APPLICATION_ZIP,
                Constants.MimeType.APPLICATION_PDF,
            });

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
            public DataCollection<?> getProcessedData() {
                return getParameters().shallClassifyByResultTimes()
                        ? new ResultTimeFormatter<Data<QuantityValue>>().format(getData())
                        : createFormatter().create().format(getData());
            }

            private FormatterFactory createFormatter() {
                return FormatterFactory.createFormatterFactory(getParameters());
            }
        };
    }

    @Override
    public boolean isAbleToCreateHandlerFor(String outputMimeType) {
        return Constants.MimeType.isKnownMimeType(outputMimeType)
                && supportsMimeType(Constants.MimeType.toInstance(outputMimeType));
    }

    @Override
    public Set<String> getSupportedMimeTypes() {
        HashSet<String> mimeTypes = new HashSet<>();
        for (Constants.MimeType supportedMimeType : SUPPORTED_MIMETYPES) {
            mimeTypes.add(supportedMimeType.getMimeType());
        }
        return mimeTypes;
    }

    private static boolean supportsMimeType(Constants.MimeType mimeType) {
        return SUPPORTED_MIMETYPES.contains(mimeType);
    }

    @Override
    public IoHandler<Data<QuantityValue>> createHandler(String outputMimeType) {
        IoParameters parameters = getParameters();
        Constants.MimeType mimeType = Constants.MimeType.toInstance(outputMimeType);
        if (mimeType == Constants.MimeType.IMAGE_PNG) {
            return createMultiChartRenderer(mimeType);
        } else if (mimeType == Constants.MimeType.APPLICATION_PDF) {
            ChartIoHandler imgRenderer = createMultiChartRenderer(mimeType);
            PDFReportGenerator reportGenerator = new PDFReportGenerator(
                    parameters,
                    createProcessChain(),
                    imgRenderer);
            reportGenerator.setBaseURI(getBasePath());
            return reportGenerator;
        } else if (mimeType == Constants.MimeType.TEXT_CSV || mimeType == Constants.MimeType.APPLICATION_ZIP) {
            QuantityCsvIoHandler handler = new QuantityCsvIoHandler(
                    parameters,
                    createProcessChain(),
                    getMetadatas());
            handler.setTokenSeparator(parameters.getOther(Parameters.TOKEN_SEPARATOR));

            boolean zipOutput = parameters.getAsBoolean(Parameters.ZIP, false);
            handler.setZipOutput(zipOutput || mimeType == Constants.MimeType.APPLICATION_ZIP);
            boolean byteOderMark = Boolean.parseBoolean(parameters.getOther(Parameters.BOM));
            handler.setIncludeByteOrderMark(byteOderMark);
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
