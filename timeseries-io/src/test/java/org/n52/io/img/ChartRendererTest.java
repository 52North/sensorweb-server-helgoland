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
