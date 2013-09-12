/**
 * ﻿Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.io;

import static org.n52.io.MimeType.APPLICATION_PDF;
import static org.n52.io.MimeType.IMAGE_PNG;

import org.n52.io.img.MultipleChartsRenderer;
import org.n52.io.img.RenderingContext;
import org.n52.io.report.PDFReportGenerator;
import org.n52.io.report.ReportGenerator;

public class IoFactory {

    private MimeType mimeType = IMAGE_PNG;
    
    private IoParameters config;

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

    public IoHandler createIOHandler(RenderingContext context) {

        if (mimeType == APPLICATION_PDF) {
            MultipleChartsRenderer imgRenderer = createMultiChartRenderer(context);
            ReportGenerator reportGenerator = new PDFReportGenerator(imgRenderer, config.getLocale());

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
