/**
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.io;

import java.net.URI;
import org.n52.io.request.IoParameters;
import static org.n52.io.MimeType.APPLICATION_PDF;
import static org.n52.io.MimeType.IMAGE_PNG;
import static org.n52.io.MimeType.TEXT_CSV;
import org.n52.io.csv.CsvIoHandler;
import org.n52.io.img.MultipleChartsRenderer;
import org.n52.io.img.RenderingContext;
import org.n52.io.report.PDFReportGenerator;
import org.n52.io.report.ReportGenerator;

public final class IoFactory {

    private MimeType mimeType = IMAGE_PNG;

    private final IoParameters config;

    private URI servletContextRoot;

    private IoFactory(IoParameters parameters) {
        this.config = parameters;
    }

    /**
     * @return An {@link IoFactory} instance with default values set. Configure factory by passing an
     *         {@link IoParameters} instance. After creating the factory an apropriately configured
     *         {@link IoHandler} is returned when calling {@link #createIOHandler(RenderingContext)}.
     */
    public static IoFactory create() {
        return createWith(null);
    }

    public static IoFactory createWith(IoParameters parameters) {
        if (parameters == null) {
            parameters = IoParameters.createDefaults();
        }
        return new IoFactory(parameters);
    }

    /**
     * @param mimeType
     *        the MIME-Type of the image to be rendered (default is {@link MimeType#IMAGE_PNG}).
     * @return this instance for parameter chaining.
     */
    public IoFactory forMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public IoFactory withServletContextRoot(URI servletContextRoot) {
        this.servletContextRoot = servletContextRoot;
        return this;
    }

    public IoHandler createIOHandler(RenderingContext context) {

        if (mimeType == APPLICATION_PDF) {
            MultipleChartsRenderer imgRenderer = createMultiChartRenderer(context);
            PDFReportGenerator reportGenerator = new PDFReportGenerator(imgRenderer, config.getLocale());
            reportGenerator.setBaseURI(servletContextRoot);
            
            // TODO

            return reportGenerator;
        }
        else if (mimeType == IMAGE_PNG) {

            /*
             * Depending on the parameters set, we can choose at this point which ChartRenderer might be the
             * best for doing the work.
             *
             * However, for now we only support a Default one ...
             */

            // TODO create an OverviewChartRenderer

            MultipleChartsRenderer chartRenderer = createMultiChartRenderer(context);

            // TODO do further settings?!

            return chartRenderer;
        } else if (mimeType == TEXT_CSV) {
            CsvIoHandler handler = new CsvIoHandler(context, config.getLocale());
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

    private MultipleChartsRenderer createMultiChartRenderer(RenderingContext context) {
        MultipleChartsRenderer chartRenderer = new MultipleChartsRenderer(context, config.getLocale());
        chartRenderer.setDrawLegend(config.isLegend());
        chartRenderer.setShowGrid(config.isGrid());
        chartRenderer.setMimeType(mimeType);
        return chartRenderer;
    }

}
