/**
 * ﻿Copyright (C) 2012
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

public class IOFactory {

    private String locale = "en";

    private boolean tooltips = false;

    private boolean drawLegend = false;
    
    private boolean showGrid = true;

    private MimeType mimeType = IMAGE_PNG;

    private IOFactory() {
        // use static constructor
    }

    /**
     * @return An {@link IOFactory} instance with default values set. Configure factory by chaining parameter
     *         methods. After configuring the factory a <code>create*</code> method creates appropriate IO
     *         generators.
     */
    public static IOFactory create() {
        return new IOFactory();
    }

    /**
     * @param locale
     *        the locale (default is <code>en</code>).
     * @return this instance for parameter chaining.
     */
    public IOFactory withLocale(String locale) {
        this.locale = locale;
        return this;
    }

    /**
     * @param tooltips
     *        <code>true</code> if tooltips shall be shown (default is <code>false</code>).
     * @return this instance for parameter chaining.
     */
    public IOFactory showTooltips(boolean tooltips) {
        this.tooltips = tooltips;
        return this;
    }

    /**
     * @param showGrid
     *        <code>true</code> if grid shall be shown (default is <code>true</code>).
     * @return this instance for parameter chaining.
     */
    public IOFactory showGrid(boolean showGrid) {
        this.showGrid = showGrid;
        return this;
    }
    
    /**
     * @param drawLegend
     *        <code>true</code> if a legend shall be drawn (default is <code>false</code>).
     * @return this instance for parameter chaining.
     */
    public IOFactory withLegend(boolean drawLegend) {
        this.drawLegend = drawLegend;
        return this;
    }

    /**
     * @param mimeType
     *        the MIME-Type of the image to be rendered (default is {@link MimeType#IMAGE_PNG}).
     * @return this instance for parameter chaining.
     */
    public IOFactory forMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public IOHandler createIOHandler(RenderingContext context) {

        if (mimeType == APPLICATION_PDF) {
            MultipleChartsRenderer imgRenderer = createMultiChartRenderer(context);
            ReportGenerator reportGenerator = new PDFReportGenerator(imgRenderer, locale);

            // TODO

            return reportGenerator;
        } else if (mimeType == IMAGE_PNG) {

            /*
             * Depending on the parameters set, we can choose at this point which ChartRenderer might be the best
             * for doing the work.
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
        MultipleChartsRenderer chartRenderer = new MultipleChartsRenderer(context, locale);
        chartRenderer.setShowTooltips(tooltips);
        chartRenderer.setDrawLegend(drawLegend);
        chartRenderer.setMimeType(mimeType);
        chartRenderer.setShowGrid(showGrid);
        return chartRenderer;
    }

}
