/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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
import org.n52.io.response.TimeseriesData;
import org.n52.io.response.TimeseriesDataCollection;
import org.n52.io.response.v1.TimeseriesDataMetadata;
import org.n52.io.response.v2.SeriesValue;

import com.vividsolutions.jts.geom.Coordinate;

import org.n52.io.response.TimeseriesValue;

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
        flotSeries.setValues(formatSeries(seriesToFormat));
        TimeseriesDataMetadata metadata = seriesToFormat.getMetadata();
        if (metadata != null) {
            Map<String, TimeseriesData> referenceValues = metadata.getReferenceValues();
            for (String referenceValueId : referenceValues.keySet()) {
                TimeseriesData referenceValueData = metadata.getReferenceValues().get(referenceValueId);
                flotSeries.addReferenceValues(referenceValueId, formatSeries(referenceValueData));
            }
        }
        return flotSeries;
    }

    private List<Number[]> formatSeries(TimeseriesData timeseries) {
        List<Number[]> series = new ArrayList<Number[]>();
        for (TimeseriesValue currentValue : timeseries.getValues()) {
            List<Number> list = new ArrayList<>();
            list.add(currentValue.getTimestamp());
            list.add(currentValue.getValue());
            if (currentValue instanceof SeriesValue && ((SeriesValue) currentValue).isSetGeometry()) {
                Coordinate coordinate = ((SeriesValue)currentValue).getGeometry().getCoordinate();
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
