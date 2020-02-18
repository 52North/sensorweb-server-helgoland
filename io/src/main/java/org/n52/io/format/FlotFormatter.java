/*
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.io.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetMetadata;
import org.n52.io.response.dataset.NumericValue;

public class FlotFormatter<T extends NumericValue<?>> implements DataFormatter<Data<T>, FlotData> {

    @Override
    public FlotDataCollection format(DataCollection<Data<T>> toFormat) {
        FlotDataCollection flotDataCollection = new FlotDataCollection();
        for (String timeseriesId : toFormat.getAllSeries().keySet()) {
            Data<T> seriesToFormat = toFormat.getSeries(timeseriesId);
            FlotData series = createFlotSeries(seriesToFormat);
            flotDataCollection.addNewSeries(timeseriesId, series);
        }
        return flotDataCollection;
    }

    private FlotData createFlotSeries(Data<T> seriesToFormat) {
        FlotData flotSeries = new FlotData();
        flotSeries.setValues(formatValues(seriesToFormat));
        if (seriesToFormat.hasMetadata()) {
            formatMetadata(seriesToFormat, flotSeries);
        }
        return flotSeries;
    }

    private void formatMetadata(Data<T> seriesToFormat, FlotData flotSeries) {
        DatasetMetadata<T> metadata = seriesToFormat.getMetadata();
        Map<String, Data<T>> referenceValues = metadata.getReferenceValues();
        for (String referenceValueId : referenceValues.keySet()) {
            Data<T> referenceValueData = metadata.getReferenceValues().get(referenceValueId);
            flotSeries.addReferenceValues(referenceValueId, formatSeries(referenceValueData));
        }
        flotSeries.setValueBeforeTimespan(formatValue(metadata.getValueBeforeTimespan()));
        flotSeries.setValueAfterTimespan(formatValue(metadata.getValueAfterTimespan()));
    }

    private FlotData formatSeries(Data<T> data) {
        FlotData series = new FlotData();
        series.setValues(formatValues(data));
        if (data.hasMetadata()) {
            series.setValueBeforeTimespan(formatValue(data.getMetadata().getValueBeforeTimespan()));
            series.setValueAfterTimespan(formatValue(data.getMetadata().getValueAfterTimespan()));
        }
        return series;
    }

    private List<Number[]> formatValues(Data<T> timeseries) {
        List<Number[]> series = new ArrayList<>();
        for (T currentValue : timeseries.getValues()) {
            series.add(formatValue(currentValue));
        }
        return series;
    }

    private Number[] formatValue(T currentValue) {
        if (currentValue == null) {
            return null;
        }
        Number value = currentValue.getValue();
        Long timestamp = currentValue.getTimestamp().getMillis();
        return new Number[] { timestamp, value };
    }

}
