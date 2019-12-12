/**
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.io.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.n52.io.v1.data.TimeseriesData;
import org.n52.io.v1.data.TimeseriesDataCollection;
import org.n52.io.v1.data.TimeseriesDataMetadata;
import org.n52.io.v1.data.TimeseriesValue;

public class FlotFormatter implements TimeseriesDataFormatter<FlotDataCollection> {

    @Override
    public TimeseriesDataCollection<?> format(TvpDataCollection toFormat) {
        FlotDataCollection flotDataCollection = new FlotDataCollection();
        for (String timeseriesId : toFormat.getAllTimeseries().keySet()) {
            TimeseriesData seriesToFormat = toFormat.getTimeseries(timeseriesId);
            FlotSeries series = createFlotSeries(seriesToFormat);
            flotDataCollection.addNewTimeseries(timeseriesId, series);
        }
        return flotDataCollection;
    }

    private FlotSeries createFlotSeries(TimeseriesData seriesToFormat) {
        FlotSeries flotSeries = new FlotSeries();
        flotSeries.setValues(formatValues(seriesToFormat));
        if (seriesToFormat.hasMetadata()) {
            formatMetadata(seriesToFormat, flotSeries);
        }
        return flotSeries;
    }

    private void formatMetadata(TimeseriesData seriesToFormat, FlotSeries flotSeries) {
        TimeseriesDataMetadata metadata = seriesToFormat.getMetadata();
        if (metadata.hasReferenceValues()) {
            Map<String, TimeseriesData> referenceValues = metadata.getReferenceValues();
            for (String referenceValueId : referenceValues.keySet()) {
                TimeseriesData referenceValueData = metadata.getReferenceValues().get(referenceValueId);
                flotSeries.addReferenceValues(referenceValueId, formatSeries(referenceValueData));
            }
        }
        flotSeries.setValueBeforeTimespan(formatValue(metadata.getValueBeforeTimespan()));
        flotSeries.setValueAfterTimespan(formatValue(metadata.getValueAfterTimespan()));
    }

    private FlotSeries formatSeries(TimeseriesData timeseries) {
        FlotSeries series = new FlotSeries();
        series.setValues(formatValues(timeseries));
        if (timeseries.hasMetadata()) {
            series.setValueBeforeTimespan(formatValue(timeseries.getMetadata().getValueBeforeTimespan()));
            series.setValueAfterTimespan(formatValue(timeseries.getMetadata().getValueAfterTimespan()));
        }
        return series;
    }

    private List<Number[]> formatValues(TimeseriesData timeseries) {
        List<Number[]> series = new ArrayList<>();
        for (TimeseriesValue currentValue : timeseries.getValues()) {
            series.add(formatValue(currentValue));
        }
        return series;
    }

    private Number[] formatValue(TimeseriesValue currentValue) {
        if (currentValue == null) {
            return null;
        }
        Double value = currentValue.getValue();
        Long timestamp = currentValue.getTimestamp();
        return new Number[] { timestamp, value };
    }

}
