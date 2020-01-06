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
package org.n52.io.type.quantity.format;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.n52.io.format.DataFormatter;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetMetadata;
import org.n52.io.response.dataset.quantity.QuantityValue;

public class HighchartFormatter implements DataFormatter<Data<QuantityValue>, HighchartData> {

    @Override
    public HighchartDataCollection format(DataCollection<Data<QuantityValue>> toFormat) {
        HighchartDataCollection dataCollection = new HighchartDataCollection();
        for (String timeseriesId : toFormat.getAllSeries().keySet()) {
            Data<QuantityValue> seriesToFormat = toFormat.getSeries(timeseriesId);
            HighchartData series = createHighchartSeries(timeseriesId, seriesToFormat);
            dataCollection.addNewSeries(timeseriesId, series);

            if (seriesToFormat.hasMetadata()) {
                DatasetMetadata<QuantityValue> metadata = seriesToFormat.getMetadata();
                if (metadata.hasReferenceValues()) {
                    Map<String, Data<QuantityValue>> referenceValues = metadata.getReferenceValues();
                    for (String referenceValueId : referenceValues.keySet()) {
                        Data<QuantityValue> timeseriesData = metadata.getReferenceValues().get(referenceValueId);
                        HighchartData referenceSeries = createHighchartSeries(referenceValueId, timeseriesData);
                        if (timeseriesData.hasMetadata()) {
                            referenceSeries.setValueBeforeTimespan(
                                    formatValue(timeseriesData.getMetadata().getValueBeforeTimespan()));
                            referenceSeries.setValueAfterTimespan(
                                    formatValue(timeseriesData.getMetadata().getValueAfterTimespan()));
                        }
                        dataCollection.addNewSeries(referenceValueId, referenceSeries);
                    }
                }
                series.setValueBeforeTimespan(formatValue(metadata.getValueBeforeTimespan()));
                series.setValueAfterTimespan(formatValue(metadata.getValueAfterTimespan()));
            }
        }
        return dataCollection;
    }

    private HighchartData createHighchartSeries(String seriesId, Data<QuantityValue> timeseriesData) {
        List<Number[]> formattedSeries = formatSeries(timeseriesData);
        HighchartData series = new HighchartData();
        series.setName(seriesId);
        series.setData(formattedSeries);
        return series;
    }

    private List<Number[]> formatSeries(Data<QuantityValue> timeseriesData) {
        List<Number[]> series = new ArrayList<>();
        for (QuantityValue currentValue : timeseriesData.getValues()) {
            series.add(formatValue(currentValue));
        }
        return series;
    }

    private Number[] formatValue(QuantityValue currentValue) {
        if (currentValue == null) {
            return null;
        }
        Long timestamp = currentValue.getTimestamp().getMillis();
        BigDecimal value = currentValue.getValue();
        return new Number[] { timestamp, value };
    }

}
