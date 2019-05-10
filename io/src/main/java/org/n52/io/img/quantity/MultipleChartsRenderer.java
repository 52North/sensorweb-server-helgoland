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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;

import org.n52.io.IoProcessChain;
import org.n52.io.IoStyleContext;
import org.n52.io.request.IoParameters;
import org.n52.io.request.StyleProperties;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetMetadata;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.ReferenceValueOutput;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.io.style.BarStyle;
import org.n52.io.style.LineStyle;
import org.n52.io.style.Style;

public class MultipleChartsRenderer extends ChartIoHandler {

    public MultipleChartsRenderer(IoParameters parameters,
                                  IoProcessChain<Data<QuantityValue>> processChain,
                                  IoStyleContext context) {
        super(parameters, processChain, context);
    }

    @Override
    public void writeDataToChart(DataCollection<Data<QuantityValue>> data) {
        Map<String, Data<QuantityValue>> allTimeseries = data.getAllSeries();
        List< ? extends DatasetOutput< ? >> timeseriesMetadatas = getMetadataOutputs();

        int rendererCount = timeseriesMetadatas.size();
        for (int rendererIndex = 0; rendererIndex < timeseriesMetadatas.size(); rendererIndex++) {

            /*
             * For each index put data and its renderer configured to a particular style. As each timeseries
             * may define its custom styling and different chart types we have to loop over all timeseries to
             * configure chart rendering.
             */
            DatasetOutput< ? > timeseriesMetadata = timeseriesMetadatas.get(rendererIndex);

            String timeseriesId = timeseriesMetadata.getId();
            StyleProperties style = getDatasetStyleFor(timeseriesId);
            Data<QuantityValue> timeseriesData = allTimeseries.get(timeseriesId);

            String chartId = createChartId(timeseriesMetadata);
            ChartIndexConfiguration configuration = new ChartIndexConfiguration(chartId, rendererIndex);
            configuration.setData(timeseriesData, timeseriesMetadata, style);
            configuration.setRenderer(createRenderer(style));

            if (timeseriesData.hasReferenceValues()) {
                int referenceIndex = rendererCount;

                /*
                 * Configure timeseries reference value renderers with the same metadata and add it at the end
                 * of the plot's renderer list.
                 */
                DatasetMetadata<Data<QuantityValue>> metadata = timeseriesData.getMetadata();
                Map<String, Data<QuantityValue>> referenceValues = metadata.getReferenceValues();
                for (Entry<String, Data<QuantityValue>> referencedTimeseries : referenceValues.entrySet()) {
                    String referenceTimeseriesId = referencedTimeseries.getKey();
                    ReferenceValueOutput< ? > referenceOutput = getReferenceValue(referenceTimeseriesId,
                                                                                  timeseriesMetadata);
                    String referenceChartId = createChartId(timeseriesMetadata, referenceOutput.getLabel());

                    Data<QuantityValue> referenceData = referenceValues.get(referenceTimeseriesId);
                    ChartIndexConfiguration referenceConfiguration = new ChartIndexConfiguration(referenceChartId,
                                                                                                 referenceIndex);
                    StyleProperties referenceStyle = getTimeseriesStyleFor(timeseriesId, referenceTimeseriesId);
                    referenceConfiguration.setReferenceData(referenceData, timeseriesMetadata, referenceStyle);
                    referenceConfiguration.setRenderer(createRenderer(referenceStyle));
                    referenceIndex++;
                }
            }
        }
    }

    private String createChartId(DatasetOutput< ? > metadata) {
        return createChartId(metadata, null);
    }

    private String createChartId(DatasetOutput< ? > metadata, String referenceId) {
        ParameterOutput feature = metadata.getDatasetParameters()
                                          .getFeature();
        StringBuilder timeseriesLabel = new StringBuilder();
        timeseriesLabel.append(feature.getLabel());
        if (referenceId != null) {
            timeseriesLabel.append(", ")
                           .append(referenceId);
        }
        timeseriesLabel.append(" (")
                       .append(createRangeLabel(metadata))
                       .append(")");
        return timeseriesLabel.toString();
    }

    private Renderer createRenderer(StyleProperties properties) {
        if (isBarStyle(properties)) {
            // configure bar chart renderer
            BarStyle barStyle = BarStyle.createBarStyle(properties);
            return BarRenderer.createBarRenderer(barStyle);
        }
        // configure line chart renderer
        LineStyle lineStyle = LineStyle.createLineStyle(properties);
        return LineRenderer.createStyledLineRenderer(lineStyle);
    }

    private ReferenceValueOutput< ? > getReferenceValue(String id, DatasetOutput< ? > metadata) {
        for (ReferenceValueOutput< ? > referenceOutput : metadata.getReferenceValues()) {
            if (referenceOutput.getReferenceValueId()
                               .equals(id)) {
                return referenceOutput;
            }
        }
        return null;
    }

    private class ChartIndexConfiguration {

        private int timeseriesIndex;

        private String chartId;

        ChartIndexConfiguration(String chartId, int index) {
            if (chartId == null) {
                throw new NullPointerException("ChartId must not be null.");
            }
            this.timeseriesIndex = index;
            this.chartId = chartId;
        }

        public void setRenderer(Renderer renderer) {
            getXYPlot().setRenderer(timeseriesIndex, renderer.getXYRenderer());
            // renderer.setColorForSeries(timeseriesIndex);
            renderer.setColorForSeries();
        }

        public void setData(Data<QuantityValue> data, DatasetOutput< ? > timeMetadata, StyleProperties style) {
            getXYPlot().setDataset(timeseriesIndex, createTimeseriesCollection(data, style));
            ValueAxis rangeAxis = createRangeAxis(timeMetadata);
            getXYPlot().setRangeAxis(timeseriesIndex, rangeAxis);
            getXYPlot().mapDatasetToRangeAxis(timeseriesIndex, timeseriesIndex);
        }

        public void setReferenceData(Data<QuantityValue> referenceData,
                                     DatasetOutput< ? > timeMetadata,
                                     StyleProperties style) {
            getXYPlot().setDataset(timeseriesIndex, createTimeseriesCollection(referenceData, style));
        }

        private TimeSeriesCollection createTimeseriesCollection(Data<QuantityValue> referenceData,
                                                                StyleProperties style) {
            TimeSeriesCollection timeseriesCollection = new TimeSeriesCollection();
            timeseriesCollection.addSeries(createDiscreteTimeseries(referenceData, style));
            timeseriesCollection.setGroup(new DatasetGroup(chartId));
            return timeseriesCollection;
        }

        private TimeSeries createDiscreteTimeseries(Data<QuantityValue> referenceData, StyleProperties style) {
            TimeSeries timeseries = new TimeSeries(chartId);
            if (hasValues(referenceData)) {
                if (isBarStyle(style)) {
                    QuantityValue timeseriesValue = referenceData.getValues()
                                                                 .get(0);
                    Date timeOfFirstValue = new Date(timeseriesValue.getTimestamp());
                    RegularTimePeriod timeinterval = determineTimeInterval(timeOfFirstValue, style);

                    BigDecimal intervalSum = BigDecimal.ZERO;
                    for (QuantityValue value : referenceData.getValues()) {
                        if (isValueInInterval(value, timeinterval)) {
                            intervalSum = intervalSum.add(value.getValue());
                        } else {
                            timeseries.add(timeinterval, intervalSum);
                            timeinterval = determineTimeInterval(new Date(value.getTimestamp()), style);
                            intervalSum = value.getValue();
                        }
                    }
                } else if (isLineStyle(style)) {
                    for (QuantityValue value : referenceData.getValues()) {
                        Second second = new Second(new Date(value.getTimestamp()));
                        timeseries.addOrUpdate(second, value.getValue());
                    }
                }
            }
            return timeseries;
        }

        private boolean hasValues(Data<QuantityValue> timeseriesData) {
            return timeseriesData.getValues()
                                 .size() > 0;
        }

        private RegularTimePeriod determineTimeInterval(Date date, StyleProperties styleProperties) {
            if (styleProperties.getProperties()
                               .containsKey(Style.PARAMETER_INTERVAL)) {
                String interval = styleProperties.getProperties()
                                                 .get(Style.PARAMETER_INTERVAL);
                if (interval.equals(Style.VALUE_INTERVAL_BY_HOUR)) {
                    return new Hour(date);
                } else if (interval.equals(Style.VALUE_INTERVAL_BY_DAY)) {
                    return new Day(date);
                } else if (interval.equals(Style.VALUE_INTERVAL_BY_MONTH)) {
                    return new Month(date);
                }
            }
            return new Week(date);
        }

        /**
         * @param interval
         *        the interval to check.
         * @return <code>true</code> if timestamp is within the given interval, otherwise <code>false</code>
         *         is returned. If passed interval was <code>null</code> false will be returned.
         * @throws IllegalArgumentException
         *         if passed in value is <code>null</code>.
         */
        private boolean isValueInInterval(QuantityValue value, RegularTimePeriod interval) {
            if (value == null) {
                throw new IllegalArgumentException("TimeseriesValue must not be null.");
            }
            return interval == null
                    || interval.getStart()
                               .getTime() <= value.getTimestamp()
                            && value.getTimestamp() < interval.getEnd()
                                                              .getTime();
        }

    }

}
