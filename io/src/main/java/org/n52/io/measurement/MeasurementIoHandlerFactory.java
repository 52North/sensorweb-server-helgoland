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

import java.net.URI;
import org.n52.io.IoHandler;
import org.n52.io.IoHandlerFactory;

import org.n52.io.MimeType;
import org.n52.io.measurement.img.MultipleChartsRenderer;
import org.n52.io.measurement.report.PDFReportGenerator;
import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.measurement.MeasurementData;
import org.n52.io.measurement.csv.MeasurementCsvIoHandler;

public final class MeasurementIoHandlerFactory implements IoHandlerFactory {

    private IoParameters parameters;

    private URI servletContextRoot;

    private IoContext context;

    public MeasurementIoHandlerFactory() {
        this.parameters = IoParameters.createDefaults();
        this.context = new IoContext();
    }

    public MeasurementIoHandlerFactory withParameters(IoParameters parameters) {
        this.parameters = parameters;
        return this;
    }

    public MeasurementIoHandlerFactory withServletContextRoot(URI servletContextRoot) {
        this.servletContextRoot = servletContextRoot;
        return this;
    }

    public MeasurementIoHandlerFactory withContext(IoContext context) {
        this.context = context;
        return this;
    }

    @Override
    public boolean isAbleToCreateHandlerFor(String outputMimeType) {
        return MimeType.isKnownMimeType(outputMimeType)
                && supportsMimeType(MimeType.toInstance(outputMimeType));
    }

    private static boolean supportsMimeType(MimeType mimeType) {
        return mimeType == MimeType.TEXT_CSV
                || mimeType == MimeType.IMAGE_PNG
                || mimeType == mimeType.APPLICATION_PDF;
    }

    @Override
    public IoHandler<MeasurementData> createHandler(String outputMimeType) {
        MimeType mimeType = MimeType.toInstance(outputMimeType);
        if (mimeType == APPLICATION_PDF) {
            MultipleChartsRenderer imgRenderer = createMultiChartRenderer(mimeType);
            PDFReportGenerator reportGenerator = new PDFReportGenerator(imgRenderer, parameters.getLocale());
            reportGenerator.setBaseURI(servletContextRoot);
            return reportGenerator;
        } else if (mimeType == IMAGE_PNG) {
            return createMultiChartRenderer(mimeType);
        } else if (mimeType == TEXT_CSV) {
            MeasurementCsvIoHandler<MeasurementData> handler = new MeasurementCsvIoHandler(context, parameters.getLocale());
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
        MultipleChartsRenderer chartRenderer = new MultipleChartsRenderer(context, parameters.getLocale());
        chartRenderer.setDrawLegend(parameters.isLegend());
        chartRenderer.setShowGrid(parameters.isGrid());
        chartRenderer.setMimeType(mimeType);
        return chartRenderer;
    }

}
