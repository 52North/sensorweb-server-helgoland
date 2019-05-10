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

import java.awt.Color;

import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.n52.io.style.BarStyle;

final class BarRenderer implements Renderer {

    static final String BAR_CHART_TYPE = "bar";

    private XYBarRenderer renderer;

    private BarStyle style;

    private BarRenderer(BarStyle style) {
        this.style = style;
        this.renderer = new XYBarRenderer(style.getBarMargin());
        StandardXYBarPainter barPainter = new StandardXYBarPainter();
        XYBarRenderer.setDefaultShadowsVisible(false);
        this.renderer.setBarPainter(barPainter);
    }

    @Override
    public String getRendererType() {
        return BAR_CHART_TYPE;
    }

    @Override
    public XYItemRenderer getXYRenderer() {
        return renderer;
    }

    @Override
    public void setColorForSeries() {
        // each renderer renders just one series
        this.renderer.setSeriesPaint(0, Color.decode(style.getColor()));
    }

    public static BarRenderer createBarRenderer(BarStyle barStyle) {
        return new BarRenderer(barStyle);
    }

}
