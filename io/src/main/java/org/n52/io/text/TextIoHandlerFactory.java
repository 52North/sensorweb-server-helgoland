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
package org.n52.io.text;


import java.net.URI;
import org.n52.io.IoHandler;
import org.n52.io.IoHandlerFactory;

import org.n52.io.MimeType;
import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.text.TextObservationData;
import org.n52.io.measurement.csv.MeasurementCsvIoHandler;

public class TextIoHandlerFactory implements IoHandlerFactory {

    private IoParameters parameters;

    private URI servletContextRoot;

    private TextObservationRenderingContext context;

    public TextIoHandlerFactory() {
        this.parameters = IoParameters.createDefaults();
        this.context = new TextObservationRenderingContext();
    }

    public TextIoHandlerFactory withParameters(IoParameters parameters) {
        this.parameters = parameters;
        return this;
    }

    public TextIoHandlerFactory withServletContextRoot(URI servletContextRoot) {
        this.servletContextRoot = servletContextRoot;
        return this;
    }

    public TextIoHandlerFactory withContext(TextObservationRenderingContext context) {
        this.context = context;
        return this;
    }

    @Override
    public boolean isAbleToCreateHandlerFor(String outputMimeType) {
        return MimeType.isKnownMimeType(outputMimeType)
            && MimeType.toInstance(outputMimeType) == MimeType.TEXT_CSV;
    }

    @Override
    public IoHandler<TextObservationData> createHandler(String outputMimeType) {
        if (MimeType.toInstance(outputMimeType) == MimeType.TEXT_CSV) {
            MeasurementCsvIoHandler<TextObservationData> handler = new MeasurementCsvIoHandler<>(context, parameters.getLocale());
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

}
