/**
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
import static org.n52.io.IoParameters.createDefaults;
import static org.n52.io.img.RenderingContext.createContextForSingleTimeseries;
import static org.n52.io.img.RenderingContext.createEmpty;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.n52.io.IoParameters;
import org.n52.io.MimeType;
import org.n52.io.format.TvpDataCollection;
import org.n52.io.v1.data.CategoryOutput;
import org.n52.io.v1.data.FeatureOutput;
import org.n52.io.v1.data.OfferingOutput;
import org.n52.io.v1.data.ParameterOutput;
import org.n52.io.v1.data.PhenomenonOutput;
import org.n52.io.v1.data.ProcedureOutput;
import org.n52.io.v1.data.ServiceOutput;
import org.n52.io.v1.data.StationOutput;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.n52.io.v1.data.TimeseriesOutput;

public class ChartRendererTest {

    private static final String VALID_ISO8601_RELATIVE_START = "PT6H/2013-08-13TZ";

    private static final String VALID_ISO8601_ABSOLUTE_START = "2013-07-13TZ/2013-08-13TZ";

    private static final String VALID_ISO8601_DAYLIGHT_SAVING_SWITCH = "2013-10-28T02:00:00+02:00/2013-10-28T02:00:00+01:00";

    @Test
    public void shouldParseBeginFromIso8601PeriodWithRelativeStart() {
        MyChartRenderer chartRenderer = new MyChartRenderer(createEmpty());
        Date start = chartRenderer.getStartTime(VALID_ISO8601_RELATIVE_START);
        assertThat(start, is(DateTime.parse("2013-08-13TZ")
                                     .minusHours(6)
                                     .toDate()));
    }

    @Test
    public void shouldParseBeginFromIso8601PeriodWithAbsoluteStart() {
        MyChartRenderer chartRenderer = new MyChartRenderer(createEmpty());
        Date start = chartRenderer.getStartTime(VALID_ISO8601_ABSOLUTE_START);
        assertThat(start, is(DateTime.parse("2013-08-13TZ")
                                     .minusMonths(1)
                                     .toDate()));
    }

    @Test
    public void shouldParseBeginAndEndFromIso8601PeriodContainingDaylightSavingTimezoneSwith() {
        MyChartRenderer chartRenderer = new MyChartRenderer(createEmpty());
        Date start = chartRenderer.getStartTime(VALID_ISO8601_DAYLIGHT_SAVING_SWITCH);
        Date end = chartRenderer.getEndTime(VALID_ISO8601_DAYLIGHT_SAVING_SWITCH);
        assertThat(start, is(DateTime.parse("2013-10-28T00:00:00Z")
                                     .toDate()));
        assertThat(end, is(DateTime.parse("2013-10-28T01:00:00Z")
                                   .toDate()));
    }

    @Test
    public void shouldPrintDefaultOutputTimezoneInDomainAxisLabel() {
        IoParameters config = IoParameters.createDefaults();
        RenderingContext context = RenderingContext.create(config);
        String label = new MyChartRenderer(context).getXYPlot()
                                                   .getDomainAxis()
                                                   .getLabel();
        assertThat(label, is("Time (UTC)"));
    }

    @Test
    public void shouldPrintExplicitlySetOutputTimezoneInDomainAxisLabel() {
        IoParameters config = IoParameters.createDefaults()
                                          .removeAllOf("outputTimezone")
                                          .extendWith("timespan", "P4D/2012-08-12TZ")
                                          .extendWith("outputTimezone", "America/Los_Angeles");
        RenderingContext context = RenderingContext.create(config);
        String label = new MyChartRenderer(context).getXYPlot()
                                                   .getDomainAxis()
                                                   .getLabel();
        assertThat(label, is("Time (PDT)"));
    }

    @Test
    public void shouldHaveUTCTimezoneIncludedInDomainAxisLabel() {
        RenderingContext context = RenderingContext.createEmpty();
        context.getChartStyleDefinitions()
               .setTimespan(VALID_ISO8601_ABSOLUTE_START);
        MyChartRenderer chartRenderer = new MyChartRenderer(context);
        String label = chartRenderer.getXYPlot()
                                    .getDomainAxis()
                                    .getLabel();
        ISODateTimeFormat.dateTimeParser()
                         .withOffsetParsed()
                         .parseDateTime(VALID_ISO8601_ABSOLUTE_START.split("/")[1]);
        assertThat(label, is("Time (UTC)"));
    }

    @Test
    public void shouldFormatTitleTemplateWhenPrerenderingTriggerIsActive() {
        TimeseriesMetadataOutput metadata = new TimeseriesMetadataOutput();
        TimeseriesOutput timeseriesOutput = new TimeseriesOutput();
        timeseriesOutput.setCategory(createParameter(new CategoryOutput(), "cat_1", "category"));
        timeseriesOutput.setFeature(createParameter(new FeatureOutput(), "feat_1", "feature"));
        timeseriesOutput.setOffering(createParameter(new OfferingOutput(), "off_1", "offering"));
        timeseriesOutput.setPhenomenon(createParameter(new PhenomenonOutput(), "phen_1", "phenomenon"));
        timeseriesOutput.setProcedure(createParameter(new ProcedureOutput(), "proc_1", "procedure"));
        timeseriesOutput.setService(createParameter(new ServiceOutput(), "ser_1", "service"));
        metadata.setParameters(timeseriesOutput);
        metadata.setId("timeseries");
        metadata.setUom("");

        StationOutput stationOutput = new StationOutput();
        stationOutput.addProperty("id", "sta_1");
        stationOutput.addProperty("label", "station");
        metadata.setStation(stationOutput);

        // build expected title
        StringBuilder expected = new StringBuilder();
        expected.append(metadata.getStation()
                                .getProperties()
                                .get("label"));
        expected.append(" ")
                .append(timeseriesOutput.getPhenomenon()
                                        .getLabel());
        expected.append(" ")
                .append(timeseriesOutput.getProcedure()
                                        .getLabel());
        // expected.append(" ").append(timeseriesOutput.getCategory().getLabel());
        expected.append(" (4 opted-out)");
        expected.append(" ")
                .append(timeseriesOutput.getOffering()
                                        .getLabel());
        expected.append(" ")
                .append(timeseriesOutput.getFeature()
                                        .getLabel());
        expected.append(" ")
                .append(timeseriesOutput.getService()
                                        .getLabel());
        expected.append(" ")
                .append(metadata.getUom());

        IoParameters ioConfig = createDefaults().extendWith("rendering_trigger", "prerendering");
        RenderingContext context = createContextForSingleTimeseries(metadata, ioConfig);
        MyChartRenderer chartRenderer = new MyChartRenderer(context);
        // String template = "%1$s %2$s %3$s %4$s %5$s %6$s %7$s %8$s";
        String template = "%1$s %2$s %3$s (4 opted-out) %5$s %6$s %7$s %8$s";
        String actual = chartRenderer.formatTitle(metadata, template);

        assertThat(actual, is(expected.toString()));
    }

    private <T extends ParameterOutput> T createParameter(T output, String id, String label) {
        output.setId(id);
        output.setLabel(label);
        return output;
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
        public void generateOutput(TvpDataCollection data) {
            throw new UnsupportedOperationException();
        }

    }
}
