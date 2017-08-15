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
package org.n52.io.quantity;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.n52.io.IoFactory;
import org.n52.io.IoHandler;
import org.n52.io.IoProcessChain;
import org.n52.io.MimeType;
import org.n52.io.csv.quantity.QuantityCsvIoHandler;
import org.n52.io.format.quantity.FormatterFactory;
import org.n52.io.generalize.quantity.GeneralizingQuantityService;
import org.n52.io.img.quantity.ChartIoHandler;
import org.n52.io.img.quantity.MultipleChartsRenderer;
import org.n52.io.report.quantity.PDFReportGenerator;
import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.quantity.QuantityData;
import org.n52.io.response.dataset.quantity.QuantityDatasetOutput;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.series.spi.srv.DataService;

public final class QuantityIoFactory extends IoFactory<QuantityData, QuantityDatasetOutput, QuantityValue> {

    private static final List<MimeType> SUPPORTED_MIMETYPES = Arrays.asList(
            new MimeType[] {
                MimeType.TEXT_CSV,
                MimeType.IMAGE_PNG,
                MimeType.APPLICATION_ZIP,
                MimeType.APPLICATION_PDF,
            });

    @Override
    public IoProcessChain<QuantityData> createProcessChain() {
        return new IoProcessChain<QuantityData>() {
            @Override
            public DataCollection<QuantityData> getData() {
                final boolean generalize = getParameters().isGeneralize();
                DataService<QuantityData> dataService = generalize
                        ? new GeneralizingQuantityService(getDataService())
                        : getDataService();
                return dataService.getData(getRequestParameters());
            }

            @Override
            public DataCollection<?> getProcessedData() {
                String format = getParameters().getFormat();
                return FormatterFactory.createFormatterFactory(format).create().format(getData());
            }
        };
    }

    @Override
    public boolean isAbleToCreateHandlerFor(String outputMimeType) {
        return MimeType.isKnownMimeType(outputMimeType)
                && supportsMimeType(MimeType.toInstance(outputMimeType));
    }

    @Override
    public Set<String> getSupportedMimeTypes() {
        HashSet<String> mimeTypes = new HashSet<>();
        for (MimeType supportedMimeType : SUPPORTED_MIMETYPES) {
            mimeTypes.add(supportedMimeType.getMimeType());
        }
        return mimeTypes;
    }

    private static boolean supportsMimeType(MimeType mimeType) {
        return SUPPORTED_MIMETYPES.contains(mimeType);
    }

    @Override
    public IoHandler<QuantityData> createHandler(String outputMimeType) {
        IoParameters parameters = getParameters();
        MimeType mimeType = MimeType.toInstance(outputMimeType);
        if (mimeType == MimeType.IMAGE_PNG) {
            return createMultiChartRenderer(mimeType);
        } else if (mimeType == MimeType.APPLICATION_PDF) {
            ChartIoHandler imgRenderer = createMultiChartRenderer(mimeType);
            PDFReportGenerator reportGenerator = new PDFReportGenerator(
                    getRequestParameters(),
                    createProcessChain(),
                    imgRenderer);
            reportGenerator.setBaseURI(getBasePath());
            return reportGenerator;
        } else if (mimeType == MimeType.TEXT_CSV || mimeType == MimeType.APPLICATION_ZIP) {
            QuantityCsvIoHandler handler = new QuantityCsvIoHandler(
                    getRequestParameters(),
                    createProcessChain(),
                    getMetadatas());
            handler.setTokenSeparator(parameters.getOther("tokenSeparator"));

            boolean zipOutput = parameters.getAsBoolean(MimeType.APPLICATION_ZIP.name());
            handler.setZipOutput(zipOutput || mimeType == MimeType.APPLICATION_ZIP);
            boolean byteOderMark = Boolean.parseBoolean(parameters.getOther("bom"));
            handler.setIncludeByteOrderMark(byteOderMark);
            return handler;
        }

        String msg = "The requested media type '" + outputMimeType + "' is not supported.";
        IllegalArgumentException exception = new IllegalArgumentException(msg);
        throw exception;
    }

    private MultipleChartsRenderer createMultiChartRenderer(MimeType mimeType) {
        MultipleChartsRenderer chartRenderer = new MultipleChartsRenderer(
                getRequestParameters(),
                createProcessChain(),
                createContext());

        chartRenderer.setMimeType(mimeType);
        return chartRenderer;
    }

}
