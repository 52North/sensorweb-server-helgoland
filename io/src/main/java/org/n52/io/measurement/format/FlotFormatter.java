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
package org.n52.io.measurement.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.n52.io.format.SeriesDataFormatter;
import org.n52.io.response.series.MeasurementData;
import org.n52.io.response.series.MeasurementDataMetadata;
import org.n52.io.response.series.MeasurementValue;
import org.n52.io.response.series.DataCollection;

import com.vividsolutions.jts.geom.Coordinate;

public class FlotFormatter implements SeriesDataFormatter<MeasurementData, FlotSeries> {

    @Override
    public FlotDataCollection format(DataCollection<MeasurementData> toFormat) {
        FlotDataCollection flotDataCollection = new FlotDataCollection();
        for (String timeseriesId : toFormat.getAllSeries().keySet()) {
            MeasurementData seriesToFormat = toFormat.getSeries(timeseriesId);
            FlotSeries series = createFlotSeries(seriesToFormat);
            flotDataCollection.addNewSeries(timeseriesId, series);
        }
        return flotDataCollection;
    }

    private FlotSeries createFlotSeries(MeasurementData seriesToFormat) {
        FlotSeries flotSeries = new FlotSeries();
        flotSeries.setValues(formatSeries(seriesToFormat));
        MeasurementDataMetadata metadata = seriesToFormat.getMetadata();
        if (metadata != null) {
            Map<String, MeasurementData> referenceValues = metadata.getReferenceValues();
            for (String referenceValueId : referenceValues.keySet()) {
                MeasurementData referenceValueData = metadata.getReferenceValues().get(referenceValueId);
                flotSeries.addReferenceValues(referenceValueId, formatSeries(referenceValueData));
            }
        }
        return flotSeries;
    }

    private List<Number[]> formatSeries(MeasurementData timeseries) {
        List<Number[]> series = new ArrayList<>();
        for (MeasurementValue currentValue : timeseries.getValues()) {
            List<Number> list = new ArrayList<>();
            list.add(currentValue.getTimestamp());
            list.add(currentValue.getValue());
            if (currentValue.isSetGeometry()) {
                Coordinate coordinate = currentValue.getGeometry().getCoordinate();
                list.add(coordinate.x);
                list.add(coordinate.y);
                if (!Double.isNaN(coordinate.z)) {
                    list.add(coordinate.z);
                }
            }
            series.add(list.toArray(new Number[0]));
        }
        return series;
    }

}
