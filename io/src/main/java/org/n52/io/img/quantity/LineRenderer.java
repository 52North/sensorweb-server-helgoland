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
package org.n52.io.img.quantity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.n52.io.style.LineStyle;

/**
 * Renders lines of different styles. Supported line styles are
 * <ul>
 * <li><code>solid</code></li>
 * <li><code>solidWithDots</code></li>
 * <li><code>dashed</code></li>
 * <li><code>dotted</code></li>
 * </ul>
 */
final class LineRenderer implements Renderer {

    static final String LINE_CHART_TYPE = "line";

    private final XYLineAndShapeRenderer lineRenderer;

    private final LineStyle style;

    private LineRenderer(LineStyle style) {
        this.style = style;
        final String lineType = style.getLineType();
        if (lineType.equals("dashed")) {
            this.lineRenderer = new XYLineAndShapeRenderer(true, false);
            this.lineRenderer.setSeriesStroke(0, getDashedLineDefinition(style));
        } else if (lineType.equals("dotted")) {
            this.lineRenderer = new XYLineAndShapeRenderer(false, true);
            lineRenderer.setBaseShape(getDotsDefinition(style));
        } else if (lineType.equals("solidWithDots")) {
            this.lineRenderer = new XYLineAndShapeRenderer(true, true);
            this.lineRenderer.setSeriesStroke(0, getSolidLineDefinition(style));
            this.lineRenderer.setSeriesShape(0, getDotsDefinition(style));
        } else {
            // solid is default
            this.lineRenderer = new XYLineAndShapeRenderer(true, false);
            this.lineRenderer.setSeriesStroke(0, getSolidLineDefinition(style));
        }
    }

    private Shape getDotsDefinition(LineStyle lineStyle) {
        int width = lineStyle.getDotWidth();
        return new Ellipse2D.Double(-width, -width, 2 * width, 2 * width);
    }

    private Stroke getSolidLineDefinition(LineStyle lineStyle) {
        return new BasicStroke(lineStyle.getLineWidth());
    }

    private BasicStroke getDashedLineDefinition(LineStyle lineStyle) {
        int width = lineStyle.getDashGapWidth();
        float[] dashSequence = new float[] {
            4.0f * width,
            4.0f * width
        };
        return new BasicStroke(width,
                               BasicStroke.CAP_ROUND,
                               BasicStroke.JOIN_ROUND,
                               1.0f,
                               dashSequence,
                               0.0f);
    }

    @Override
    public XYItemRenderer getXYRenderer() {
        return this.lineRenderer;
    }

    @Override
    public String getRendererType() {
        return LINE_CHART_TYPE;
    }

    @Override
    public void setColorForSeries() {
        // each renderer renders just one series
        lineRenderer.setSeriesPaint(0, Color.decode(style.getColor()));
    }

    public static LineRenderer createStyledLineRenderer(LineStyle style) {
        return new LineRenderer(style);
    }

}
