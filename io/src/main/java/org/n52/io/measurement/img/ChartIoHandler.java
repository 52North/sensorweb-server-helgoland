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

import static java.awt.Color.BLACK;
import static java.awt.Color.LIGHT_GRAY;
import static java.awt.Color.WHITE;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static javax.imageio.ImageIO.write;
import static org.jfree.chart.ChartFactory.createTimeSeriesChart;
import static org.n52.io.measurement.img.BarRenderer.BAR_CHART_TYPE;
import static org.n52.io.measurement.img.ChartIoHandler.LabelConstants.COLOR;
import static org.n52.io.measurement.img.ChartIoHandler.LabelConstants.FONT_LABEL;
import static org.n52.io.measurement.img.ChartIoHandler.LabelConstants.FONT_LABEL_SMALL;
import static org.n52.io.measurement.img.LineRenderer.LINE_CHART_TYPE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.n52.io.IntervalWithTimeZone;
import org.n52.io.IoHandler;
import org.n52.io.IoParseException;
import org.n52.io.IoProcessChain;
import org.n52.io.IoStyleContext;
import org.n52.io.MimeType;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.request.StyleProperties;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.SeriesParameters;
import org.n52.io.response.dataset.measurement.MeasurementData;

public abstract class ChartIoHandler extends IoHandler<MeasurementData> {

    private final IoStyleContext context;

    private boolean showTooltips;

    private MimeType mimeType;

    private JFreeChart chart;

    private XYPlot xyPlot;

    public ChartIoHandler(RequestSimpleParameterSet request,
            IoProcessChain<MeasurementData> processChain,
            IoStyleContext context) {
        super(request, processChain);
        this.context = context;
    }

    public abstract void generateOutput(DataCollection<MeasurementData> data) throws IoParseException;

    @Override
    public void encodeAndWriteTo(DataCollection<MeasurementData> data, OutputStream stream) throws IoParseException {
        try {
            generateOutput(data);
            JPEGImageWriteParam p = new JPEGImageWriteParam(null);
            p.setCompressionMode(JPEGImageWriteParam.MODE_DEFAULT);
            write(drawChartToImage(), mimeType.getFormatName(), stream);
        } catch (IOException e) {
            throw new IoParseException("Could not write image to output stream.", e);
        }
    }

    private BufferedImage drawChartToImage() {
        int width = getChartStyleDefinitions().getWidth();
        int height = getChartStyleDefinitions().getHeight();
        BufferedImage chartImage = new BufferedImage(width, height, TYPE_INT_RGB);
        Graphics2D chartGraphics = chartImage.createGraphics();
        chartGraphics.fillRect(0, 0, width, height);
        chartGraphics.setColor(WHITE);

        chart.setTextAntiAlias(true);
        chart.setAntiAlias(true);
        if (chart.getLegend() != null) {
            chart.getLegend().setFrame(BlockBorder.NONE);
        }
        chart.draw(chartGraphics, new Rectangle2D.Float(0, 0, width, height));
        return chartImage;
    }

    public XYPlot getXYPlot() {
        if (xyPlot == null) {
            xyPlot = createChart(getRenderingContext());
        }
        return xyPlot;
    }

    public IoStyleContext getRenderingContext() {
        return context;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isShowTooltips() {
        return showTooltips;
    }

    public void setShowTooltips(boolean showTooltips) {
        this.showTooltips = showTooltips;
    }

    private XYPlot createChart(IoStyleContext context) {
        DateTime end = getTimespan() != null
                ? DateTime.parse(getTimespan().split("/")[1])
                : new DateTime();
        //DateTime end = DateTime.parse(getTimespan().split("/")[1]);
        String zoneName = end.getZone().getShortName(end.getMillis(), i18n.getLocale());
        zoneName = "+00:00".equalsIgnoreCase(zoneName) ? "UTC" : zoneName;

        StringBuilder domainAxisLabel = new StringBuilder(i18n.get("msg.io.chart.time"));
        domainAxisLabel.append(" (").append(zoneName).append(")");
        boolean showLegend = getChartStyleDefinitions().isLegend();
        chart = createTimeSeriesChart(null,
                domainAxisLabel.toString(),
                i18n.get("msg.io.chart.value"),
                null,
                showLegend,
                showTooltips,
                true);
        return createPlotArea(chart);
    }

    private XYPlot createPlotArea(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(WHITE);
        plot.setDomainGridlinePaint(LIGHT_GRAY);
        plot.setRangeGridlinePaint(LIGHT_GRAY);
        plot.setAxisOffset(new RectangleInsets(2.0, 2.0, 2.0, 2.0));
        showCrosshairsOnAxes(plot);
        configureDomainAxis(plot);
        showGridlinesOnChart(plot);
        configureTimeAxis(plot);
        configureTitle(chart);
        addNotice(chart);
        return plot;
    }

    private void addNotice(JFreeChart chart) {
        TextTitle notice = new TextTitle();
        String msg = i18n.get("msg.io.chart.notice");
        if (msg != null && !msg.isEmpty()) {
            notice.setText(msg);
            notice.setPaint(BLACK);
            notice.setFont(FONT_LABEL_SMALL);
            notice.setPosition(RectangleEdge.BOTTOM);
            notice.setHorizontalAlignment(HorizontalAlignment.RIGHT);
            notice.setVerticalAlignment(VerticalAlignment.BOTTOM);
            notice.setPadding(new RectangleInsets(0, 0, 20, 20));
            chart.addSubtitle(notice);
        }
    }

    private void configureDomainAxis(XYPlot xyPlot) {
        ValueAxis domainAxis = xyPlot.getDomainAxis();
        domainAxis.setTickLabelFont(FONT_LABEL);
        domainAxis.setLabelFont(FONT_LABEL);
        domainAxis.setTickLabelPaint(COLOR);
        domainAxis.setLabelPaint(COLOR);
    }

    private void showCrosshairsOnAxes(XYPlot xyPlot) {
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
    }

    private void showGridlinesOnChart(XYPlot xyPlot) {
        boolean showGrid = getChartStyleDefinitions().isGrid();
        xyPlot.setDomainGridlinesVisible(showGrid);
        xyPlot.setRangeGridlinesVisible(showGrid);
    }

    private void configureTimeAxis(XYPlot xyPlot) {
        DateAxis timeAxis = (DateAxis) xyPlot.getDomainAxis();
        timeAxis.setRange(getStartTime(getTimespan()), getEndTime(getTimespan()));

        String timeformat = "yyyy-MM-dd, HH:mm";
        if (getChartStyleDefinitions().containsParameter("timeformat")) {
            timeformat = getChartStyleDefinitions().getAsString("timeformat");
        }
        DateFormat requestTimeFormat = new SimpleDateFormat(timeformat, i18n.getLocale());
        requestTimeFormat.setTimeZone(getTimezone().toTimeZone());
        timeAxis.setDateFormatOverride(requestTimeFormat);
        timeAxis.setTimeZone(getTimezone().toTimeZone());
    }

    private String getTimespan() {
        return getChartStyleDefinitions().getTimespan();
    }

    private DateTimeZone getTimezone() {
        return new IntervalWithTimeZone(getTimespan()).getTimezone();
    }

    public ValueAxis createRangeAxis(DatasetOutput metadata) {
        NumberAxis axis = new NumberAxis(createRangeLabel(metadata));
        axis.setTickLabelFont(FONT_LABEL);
        axis.setLabelFont(FONT_LABEL);
        axis.setTickLabelPaint(COLOR);
        axis.setLabelPaint(COLOR);
        return axis;
    }

    protected String createRangeLabel(DatasetOutput timeseriesMetadata) {
        SeriesParameters parameters = timeseriesMetadata.getSeriesParameters();
        ParameterOutput phenomenon = parameters.getPhenomenon();
        StringBuilder uom = new StringBuilder();
        uom.append(phenomenon.getLabel());
        String uomLabel = timeseriesMetadata.getUom();
        if (uomLabel != null && !uomLabel.isEmpty()) {
            uom.append(" [").append(uomLabel).append("]");
        }
        return uom.toString();
    }

    private void configureTitle(JFreeChart chart) {
        if (getChartStyleDefinitions().containsParameter("title")) {
            chart.setTitle(getChartStyleDefinitions().getAsString("title"));
        }
    }

    protected List<? extends DatasetOutput> getMetadataOutputs() {
        return context.getSeriesMetadatas();
    }

    protected StyleProperties getTimeseriesStyleFor(String timeseriesId) {
        return getChartStyleDefinitions().getStyleOptions(timeseriesId);
    }

    protected StyleProperties getTimeseriesStyleFor(String timeseriesId, String referenceValueSeriesId) {
        return getChartStyleDefinitions().getReferenceSeriesStyleOptions(timeseriesId, referenceValueSeriesId);
    }

    protected RequestStyledParameterSet getChartStyleDefinitions() {
        return context.getChartStyleDefinitions();
    }

    protected boolean isLineStyle(StyleProperties properties) {
        return isLineStyleDefault(properties) || LINE_CHART_TYPE.equals(properties.getChartType());
    }

    protected boolean isBarStyle(StyleProperties properties) {
        return !isLineStyleDefault(properties) && BAR_CHART_TYPE.equals(properties.getChartType());
    }

    private boolean isLineStyleDefault(StyleProperties properties) {
        return properties == null;
    }

    protected Date getStartTime(String timespan) {
        Interval interval = Interval.parse(timespan);
        return interval.getStart().toDate();
    }

    protected Date getEndTime(String timespan) {
        Interval interval = Interval.parse(timespan);
        return interval.getEnd().toDate();
    }

    static class LabelConstants {

        static final Color COLOR = BLACK;
        static final int FONT_SIZE = 12;
        static final int FONT_SIZE_SMALL = 9;
        static final int FONT_SIZE_TICKS = 10;
        static final String LOGICAL_FONT = "Sans-serif";
        static final Font FONT_LABEL = new Font(LOGICAL_FONT, BOLD, FONT_SIZE);
        static final Font FONT_DOMAIN = new Font(LOGICAL_FONT, PLAIN, FONT_SIZE_TICKS);
        static final Font FONT_LABEL_SMALL = new Font(LOGICAL_FONT, PLAIN, FONT_SIZE_SMALL);
    }

}
