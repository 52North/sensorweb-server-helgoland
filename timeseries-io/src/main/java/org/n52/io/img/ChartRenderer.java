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

import static java.awt.Color.BLACK;
import static java.awt.Color.LIGHT_GRAY;
import static java.awt.Color.WHITE;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static javax.imageio.ImageIO.write;
import static org.jfree.chart.ChartFactory.createTimeSeriesChart;
import static org.n52.io.I18N.getDefaultLocalizer;
import static org.n52.io.I18N.getMessageLocalizer;
import static org.n52.io.img.BarRenderer.BAR_CHART_TYPE;
import static org.n52.io.img.ChartRenderer.LabelConstants.COLOR;
import static org.n52.io.img.ChartRenderer.LabelConstants.FONT_DOMAIN;
import static org.n52.io.img.ChartRenderer.LabelConstants.FONT_LABEL;
import static org.n52.io.img.LineRenderer.LINE_CHART_TYPE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleInsets;
import org.joda.time.Interval;
import org.n52.io.I18N;
import org.n52.io.IOHandler;
import org.n52.io.MimeType;
import org.n52.io.TimeseriesIOException;
import org.n52.io.format.TvpDataCollection;
import org.n52.io.v1.data.DesignedParameterSet;
import org.n52.io.v1.data.PhenomenonOutput;
import org.n52.io.v1.data.StyleProperties;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.n52.io.v1.data.TimeseriesOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ChartRenderer implements IOHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChartRenderer.class);

    protected I18N i18n = getDefaultLocalizer();

    private RenderingContext context;

    private boolean showTooltips;

    private MimeType mimeType;

    private boolean drawLegend;
    
    private boolean generalize;

    private boolean showGrid;
    
    private JFreeChart chart;

    private XYPlot xyPlot;

    public ChartRenderer(RenderingContext context, String locale) {
        if (locale != null) {
            i18n = getMessageLocalizer(locale);
        }
        this.context = context;
    }

    public abstract void generateOutput(TvpDataCollection data) throws TimeseriesIOException;

    public void encodeAndWriteTo(OutputStream stream) throws TimeseriesIOException {
        try {
            JPEGImageWriteParam p = new JPEGImageWriteParam(null);
            p.setCompressionMode(JPEGImageWriteParam.MODE_DEFAULT);
        	write(drawChartToImage(), mimeType.getFormatName(), stream);
        }
        catch (IOException e) {
            throw new TimeseriesIOException("Could not write image to output stream.", e);
        }
        finally {
            try {
                stream.flush();
                stream.close();
            }
            catch (IOException e) {
                LOGGER.debug("Stream already flushed and closed.", e);
            }
        }
    }

    private BufferedImage drawChartToImage() {
        int width = getChartStyleDefinitions().getWidth();
        int height = getChartStyleDefinitions().getHeight();
        BufferedImage chartImage = new BufferedImage(width, height, TYPE_INT_RGB);
        Graphics2D chartGraphics = chartImage.createGraphics();
        chartGraphics.fillRect(0, 0, width, height);
        chartGraphics.setColor(WHITE);
        chart.draw(chartGraphics, new Rectangle2D.Float(0, 0, width, height));
        return chartImage;
    }
    
    public XYPlot getXYPlot() {
        if (xyPlot == null) {
            this.xyPlot = createChart(context);
        }
        return xyPlot;
    }

    public RenderingContext getRenderingContext() {
        return context;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    public void setMimeType(MimeType mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isDrawLegend() {
        return drawLegend;
    }

    public void setDrawLegend(boolean drawLegend) {
        this.drawLegend = drawLegend;
    }
    
    public boolean isGeneralize() {
        return generalize;
    }
    
    public void setGeneralize(boolean generalize) {
        this.generalize = generalize;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public boolean isShowTooltips() {
        return showTooltips;
    }

    public void setShowTooltips(boolean showTooltips) {
        this.showTooltips = showTooltips;
    }

    private XYPlot createChart(RenderingContext context) {
        this.chart = createTimeSeriesChart(null,
                                           i18n.get("time"),
                                           i18n.get("value"),
                                           null,
                                           drawLegend,
                                           showTooltips,
                                           true);
        return createPlotArea(chart);
    }

    private XYPlot createPlotArea(JFreeChart chart) {
        XYPlot xyPlot = chart.getXYPlot();
        xyPlot.setBackgroundPaint(WHITE);
        xyPlot.setDomainGridlinePaint(LIGHT_GRAY);
        xyPlot.setRangeGridlinePaint(LIGHT_GRAY);
        xyPlot.setAxisOffset(new RectangleInsets(2.0, 2.0, 2.0, 2.0));
        showCrosshairsOnAxes(xyPlot);
        showGridlinesOnChart(xyPlot);
        configureDomainAxis(xyPlot);
        configureTimeAxis(xyPlot);
        return xyPlot;
    }

    private void configureDomainAxis(XYPlot xyPlot) {
        ValueAxis domainAxis = xyPlot.getDomainAxis();
        domainAxis.setTickLabelFont(FONT_DOMAIN);
        domainAxis.setLabelFont(FONT_LABEL);
        domainAxis.setTickLabelPaint(COLOR);
        domainAxis.setLabelPaint(COLOR);
    }

    private void showCrosshairsOnAxes(XYPlot xyPlot) {
        xyPlot.setDomainCrosshairVisible(true);
        xyPlot.setRangeCrosshairVisible(true);
    }

    private void showGridlinesOnChart(XYPlot xyPlot) {
        xyPlot.setDomainGridlinesVisible(showGrid);
        xyPlot.setRangeGridlinesVisible(showGrid);
    }

    private void configureTimeAxis(XYPlot xyPlot) {
        String timespan = getChartStyleDefinitions().getTimespan();
        DateAxis timeAxis = (DateAxis) xyPlot.getDomainAxis();
        timeAxis.setRange(getStartTime(timespan), getEndTime(timespan));
        timeAxis.setDateFormatOverride(new SimpleDateFormat());
    }

    public void configureRangeAxis(TimeseriesMetadataOutput timeseries, int seriesIndex) {
        ValueAxis rangeAxis = xyPlot.getRangeAxisForDataset(seriesIndex);
        rangeAxis.setLabel(createRangeLabel(timeseries));
        rangeAxis.setTickLabelFont(FONT_LABEL);
        rangeAxis.setLabelFont(FONT_LABEL);
        rangeAxis.setTickLabelPaint(COLOR);
        rangeAxis.setLabelPaint(COLOR);
    }

    private String createRangeLabel(TimeseriesMetadataOutput timeseriesMetadata) {
        TimeseriesOutput parameters = timeseriesMetadata.getParameters();
        PhenomenonOutput phenomenon = parameters.getPhenomenon();
        StringBuilder uom = new StringBuilder();
        uom.append(phenomenon.getLabel());
        String uomLabel = timeseriesMetadata.getUom();
        if (uomLabel != null && !uomLabel.isEmpty()) {
            uom.append(" (").append(uomLabel).append(")");
        }
        return uom.toString();
    }

    protected TimeseriesMetadataOutput[] getTimeseriesMetadataOutputs() {
        return context.getTimeseriesMetadatas();
    }

    protected StyleProperties getTimeseriesStyleFor(String timeseriesId) {
        return getChartStyleDefinitions().getStyleOptions(timeseriesId);
    }

    protected DesignedParameterSet getChartStyleDefinitions() {
        return context.getChartStyleDefinitions();
    }

    protected boolean isLineStyle(StyleProperties properties) {
        return isLineStyleDefault(properties) || LINE_CHART_TYPE.equals(properties.getChartType());
    }

    protected boolean isBarStyle(StyleProperties properties) {
        return !isLineStyleDefault(properties) || BAR_CHART_TYPE.equals(properties.getChartType());
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
        static final String ARIAL = "Arial";
        static final int FONT_SIZE = 12;
        static final int FONT_SIZE_TICK = 10;
        static final Font FONT_LABEL = new Font(ARIAL, BOLD, FONT_SIZE);
        static final Font FONT_DOMAIN = new Font(ARIAL, PLAIN, FONT_SIZE_TICK);
    }

}
