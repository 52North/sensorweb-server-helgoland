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

package org.n52.io.measurement;

import static org.n52.io.MimeType.APPLICATION_PDF;
import static org.n52.io.MimeType.IMAGE_PNG;
import static org.n52.io.MimeType.TEXT_CSV;
import static org.n52.series.spi.srv.GeneralizingMeasurementDataService.composeDataService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.n52.io.IoFactory;
import org.n52.io.IoHandler;
import org.n52.io.IoProcessChain;
import org.n52.io.MimeType;
import org.n52.io.measurement.csv.MeasurementCsvIoHandler;
import org.n52.io.measurement.format.FormatterFactory;
import org.n52.io.measurement.img.ChartIoHandler;
import org.n52.io.measurement.img.MultipleChartsRenderer;
import org.n52.io.measurement.report.PDFReportGenerator;
import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.measurement.MeasurementData;
import org.n52.io.response.dataset.measurement.MeasurementDatasetOutput;
import org.n52.io.response.dataset.measurement.MeasurementValue;
import org.n52.series.spi.srv.DataService;

public final class MeasurementIoFactory extends IoFactory<MeasurementData, MeasurementDatasetOutput, MeasurementValue> {

    private static final List<MimeType> SUPPORTED_MIMETYPES = Arrays.asList(new MimeType[] {
                                                                                            MimeType.TEXT_CSV,
                                                                                            MimeType.IMAGE_PNG,
                                                                                            MimeType.APPLICATION_ZIP,
                                                                                            MimeType.APPLICATION_PDF
    });

    @Override
    public IoProcessChain<MeasurementData> createProcessChain() {
        return new IoProcessChain<MeasurementData>() {
            @Override
            public DataCollection<MeasurementData> getData() {
                final boolean generalize = getParameters().isGeneralize();
                DataService<MeasurementData> dataService = generalize
                    ? composeDataService(getDataService())
                    : getDataService();
                return dataService.getData(getSimpleRequest());
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
    public IoHandler<MeasurementData> createHandler(String outputMimeType) {
        IoParameters parameters = getParameters();
        MimeType mimeType = MimeType.toInstance(outputMimeType);
        if (mimeType == IMAGE_PNG) {
            return createMultiChartRenderer(mimeType);
        }
        else if (mimeType == APPLICATION_PDF) {
            ChartIoHandler imgRenderer = createMultiChartRenderer(mimeType);
            PDFReportGenerator reportGenerator = new PDFReportGenerator(
                                                                        getSimpleRequest(),
                                                                        createProcessChain(),
                                                                        imgRenderer);
            reportGenerator.setBaseURI(getBasePath());
            return reportGenerator;
        }
        else if (mimeType == TEXT_CSV || mimeType == MimeType.APPLICATION_ZIP) {
            MeasurementCsvIoHandler handler = new MeasurementCsvIoHandler(
                                                                          getSimpleRequest(),
                                                                          createProcessChain(),
                                                                          createContext());
            handler.setTokenSeparator(parameters.getOther("tokenSeparator"));

            boolean byteOderMark = Boolean.parseBoolean(parameters.getOther("bom"));
            boolean zipOutput = Boolean.parseBoolean(parameters.getOther("zip"));
            handler.setIncludeByteOrderMark(byteOderMark);
            handler.setZipOutput(zipOutput);
            return handler;
        }

        String msg = "The requested media type '" + outputMimeType + "' is not supported.";
        IllegalArgumentException exception = new IllegalArgumentException(msg);
        throw exception;
    }

    private MultipleChartsRenderer createMultiChartRenderer(MimeType mimeType) {
        MultipleChartsRenderer chartRenderer = new MultipleChartsRenderer(
                                                                          getSimpleRequest(),
                                                                          createProcessChain(),
                                                                          createContext());

        chartRenderer.setDrawLegend(getParameters().isLegend());
        chartRenderer.setShowGrid(getParameters().isGrid());
        chartRenderer.setMimeType(mimeType);
        return chartRenderer;
    }

}
