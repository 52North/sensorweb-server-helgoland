/**
 * ﻿Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.io.img;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.n52.io.MimeType;
import org.n52.io.format.TvpDataCollection;


public class ChartRendererTest {
    
    private static final String VALID_ISO8601_RELATIVE_START = "PT6H/2013-08-13TZ";
    
    private static final String VALID_ISO8601_ABSOLUTE_START = "2013-07-13TZ/2013-08-13TZ";
    
    private MyChartRenderer chartRenderer;

    @Before public void
    setUp() {
        this.chartRenderer = new MyChartRenderer(RenderingContext.createEmpty());
    }
    
    
    @Test public void
    shouldParseBeginFromIso8601PeriodWithRelativeStart() {
        Date start = chartRenderer.getStartTime(VALID_ISO8601_RELATIVE_START);
        assertThat(start, is(DateTime.parse("2013-08-13TZ").minusHours(6).toDate()));
    }
    
    @Test public void
    shouldParseBeginFromIso8601PeriodWithAbsoluteStart() {
        Date start = chartRenderer.getStartTime(VALID_ISO8601_ABSOLUTE_START);
        assertThat(start, is(DateTime.parse("2013-08-13TZ").minusMonths(1).toDate()));
    }

    static class MyChartRenderer extends ChartRenderer {

        public MyChartRenderer(RenderingContext context) {
            super(context, null);
        }

        public MyChartRenderer() {
            super(null, null);
        }

        @Override
        public void setMimeType(MimeType mimetype) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setShowTooltips(boolean tooltips) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDrawLegend(boolean drawLegend) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void generateOutput(TvpDataCollection data) {
            throw new UnsupportedOperationException();
        }

    }
}
