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
package org.n52.io.measurement.img;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.n52.io.IoStyleContext;
import org.n52.io.MimeType;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.measurement.MeasurementData;

public class ChartRendererTest {

    private static final String VALID_ISO8601_RELATIVE_START = "PT6H/2013-08-13TZ";

    private static final String VALID_ISO8601_ABSOLUTE_START = "2013-07-13TZ/2013-08-13TZ";

    private static final String VALID_ISO8601_DAYLIGHT_SAVING_SWITCH = "2013-10-28T02:00:00+02:00/2013-10-28T02:00:00+01:00";

    private MyChartRenderer chartRenderer;

    @Before
    public void
            setUp() {
        this.chartRenderer = new MyChartRenderer(IoStyleContext.createEmpty());
    }

    @Test
    public void
            shouldParseBeginFromIso8601PeriodWithRelativeStart() {
        Date start = chartRenderer.getStartTime(VALID_ISO8601_RELATIVE_START);
        assertThat(start, is(DateTime.parse("2013-08-13TZ").minusHours(6).toDate()));
    }

    @Test
    public void
            shouldParseBeginFromIso8601PeriodWithAbsoluteStart() {
        Date start = chartRenderer.getStartTime(VALID_ISO8601_ABSOLUTE_START);
        assertThat(start, is(DateTime.parse("2013-08-13TZ").minusMonths(1).toDate()));
    }

    @Test
    public void
            shouldParseBeginAndEndFromIso8601PeriodContainingDaylightSavingTimezoneSwith() {
        Date start = chartRenderer.getStartTime(VALID_ISO8601_DAYLIGHT_SAVING_SWITCH);
        Date end = chartRenderer.getEndTime(VALID_ISO8601_DAYLIGHT_SAVING_SWITCH);
        assertThat(start, is(DateTime.parse("2013-10-28T00:00:00Z").toDate()));
        assertThat(end, is(DateTime.parse("2013-10-28T01:00:00Z").toDate()));
    }

    @Test
    public void
            shouldHaveCETTimezoneIncludedInDomainAxisLabel() {
        IoStyleContext context = IoStyleContext.createEmpty();
        context.getChartStyleDefinitions().setTimespan(VALID_ISO8601_DAYLIGHT_SAVING_SWITCH);
        this.chartRenderer = new MyChartRenderer(context);
        String label = chartRenderer.getXYPlot().getDomainAxis().getLabel();
        assertThat(label, is("Time (+01:00)"));
    }

    @Test
    public void
            shouldHandleEmptyTimespanWhenIncludingTimezoneInDomainAxisLabel() {
        IoStyleContext context = IoStyleContext.createEmpty();
        context.getChartStyleDefinitions().setTimespan(null);
        this.chartRenderer = new MyChartRenderer(context);
        String label = chartRenderer.getXYPlot().getDomainAxis().getLabel();
        //assertThat(label, is("Time (+01:00)"));
    }

    @Test
    public void
            shouldHaveUTCTimezoneIncludedInDomainAxisLabel() {
        IoStyleContext context = IoStyleContext.createEmpty();
        context.getChartStyleDefinitions().setTimespan(VALID_ISO8601_ABSOLUTE_START);
        this.chartRenderer = new MyChartRenderer(context);
        String label = chartRenderer.getXYPlot().getDomainAxis().getLabel();
        ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime(VALID_ISO8601_ABSOLUTE_START.split("/")[1]);
        assertThat(label, is("Time (UTC)"));
    }

    static class MyChartRenderer extends ChartIoHandler {

        public MyChartRenderer(IoStyleContext context) {
            super(new RequestSimpleParameterSet(), null, context);
        }

        public MyChartRenderer() {
            super(new RequestSimpleParameterSet(), null, null);
        }

        @Override
        public void setMimeType(MimeType mimetype) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeDataToChart(DataCollection<MeasurementData> data) {
            throw new UnsupportedOperationException();
        }

    }
}
