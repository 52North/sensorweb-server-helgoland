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
package org.n52.io.dwd;

import static org.n52.io.MimeType.APPLICATION_PDF;
import static org.n52.io.MimeType.IMAGE_PNG;
import static org.n52.io.MimeType.TEXT_CSV;

import java.net.URI;

import org.n52.io.IoHandler;
import org.n52.io.MimeType;
import org.n52.io.measurement.MeasurementIoFactory;
import org.n52.io.measurement.img.MultipleChartsRenderer;
import org.n52.io.measurement.img.MeasurementRenderingContext;
import org.n52.io.measurement.report.PDFReportGenerator;
import org.n52.io.request.IoParameters;
import org.n52.io.response.series.MeasurementData;
import org.n52.io.response.series.text.TextObservationData;
import org.n52.io.series.csv.CsvIoHandler;

public class DwdAlertObservationIoFactory {
    private MimeType mimeType = IMAGE_PNG;

    private final IoParameters config;

    private URI servletContextRoot;

    private DwdAlertObservationIoFactory(IoParameters parameters) {
        this.config = parameters;
    }

    /**
     * @return An {@link DwdAlertObservationIoFactory} instance with default values set. Configure
     * factory by passing an {@link IoParameters} instance. After creating the
     * factory an apropriately configured {@link IoHandler} is returned when
     * calling {@link #createIOHandler(DwdAlertObservationRenderingContext)}.
     */
    public static DwdAlertObservationIoFactory create() {
        return createWith(null);
    }

    public static DwdAlertObservationIoFactory createWith(IoParameters parameters) {
        if (parameters == null) {
            parameters = IoParameters.createDefaults();
        }
        return new DwdAlertObservationIoFactory(parameters);
    }

    /**
     * @param mimeType the MIME-Type of the image to be rendered (default is
     * {@link MimeType#IMAGE_PNG}).
     * @return this instance for parameter chaining.
     */
    public DwdAlertObservationIoFactory forMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public DwdAlertObservationIoFactory withServletContextRoot(URI servletContextRoot) {
        this.servletContextRoot = servletContextRoot;
        return this;
    }

    public IoHandler<TextObservationData> createIOHandler(DwdAlertObservationRenderingContext context) {

        if (mimeType == APPLICATION_PDF) {
            return null;
        } else if (mimeType == IMAGE_PNG) {

            return null;
        } else if (mimeType == TEXT_CSV) {
            CsvIoHandler<TextObservationData> handler = new CsvIoHandler<TextObservationData>(context, config.getLocale());
            handler.setTokenSeparator(config.getOther("tokenSeparator"));

            boolean byteOderMark = Boolean.parseBoolean(config.getOther("bom"));
            boolean zipOutput = Boolean.parseBoolean(config.getOther("zip"));
            handler.setIncludeByteOrderMark(byteOderMark);
            handler.setZipOutput(zipOutput);
            return handler;
        }

        String msg = "The requested media type '" + mimeType.getMimeType() + "' is not supported.";
        IllegalArgumentException exception = new IllegalArgumentException(msg);
        throw exception;
    }

}
