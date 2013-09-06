/**
 * ﻿Copyright (C) 2013
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
package org.n52.io.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.n52.io.v1.data.TimeseriesData;
import org.n52.io.v1.data.TimeseriesMetadata;
import org.n52.io.v1.data.TimeseriesValue;


public class HighchartFormatter implements TimeseriesDataFormatter<HighchartDataCollection> {

    @Override
    public HighchartDataCollection format(TvpDataCollection toFormat) {
        HighchartDataCollection dataCollection = new HighchartDataCollection();
        for (String timeseriesId : toFormat.getAllTimeseries().keySet()) {
            TimeseriesData seriesToFormat = toFormat.getTimeseries(timeseriesId);
            HighchartSeries series = createHighchartSeries(timeseriesId, seriesToFormat);
            dataCollection.addNewTimeseries(timeseriesId, series);
            
            TimeseriesMetadata metadata = seriesToFormat.getMetadata();
            if (metadata != null) {
                Map<String, TimeseriesData> referenceValues = metadata.getReferenceValues();
                for (String referenceValueId : referenceValues.keySet()) {
                    TimeseriesData timeseriesData = metadata.getReferenceValues().get(referenceValueId);
                    HighchartSeries referenceSeries = createHighchartSeries(referenceValueId, timeseriesData);
                    dataCollection.addNewTimeseries(referenceValueId, referenceSeries);
                }
            }
        }
        return dataCollection;
    }

    private HighchartSeries createHighchartSeries(String seriesId, TimeseriesData seriesToFormat) {
        List<Number[]> formattedSeries = formatSeries(seriesToFormat);
        HighchartSeries series = new HighchartSeries();
        series.setName(seriesId);
        series.setData(formattedSeries);
        return series;
    }

    private List<Number[]> formatSeries(TimeseriesData timeseries) {
        List<Number[]> series = new ArrayList<Number[]>();
        for (TimeseriesValue currentValue : timeseries.getValues()) {
            Long timestamp = currentValue.getTimestamp();
            Double value = currentValue.getValue();
            series.add(new Number[] {timestamp, value});
        }
        return series;
    }

}
