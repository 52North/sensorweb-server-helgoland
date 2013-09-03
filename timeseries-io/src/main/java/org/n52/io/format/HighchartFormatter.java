package org.n52.io.format;

import java.util.ArrayList;
import java.util.List;

import org.n52.io.v1.data.TimeseriesData;
import org.n52.io.v1.data.TimeseriesValue;


public class HighchartFormatter implements TimeseriesDataFormatter<HighchartDataCollection> {

    @Override
    public HighchartDataCollection format(TvpDataCollection toFormat) {
        HighchartDataCollection dataCollection = new HighchartDataCollection();
        for (String timeseriesId : toFormat.getAllTimeseries().keySet()) {
            TimeseriesData seriesToFormat = toFormat.getTimeseries(timeseriesId);
            List<Number[]> formattedSeries = formatSeries(seriesToFormat);
            HighchartSeries series = new HighchartSeries();
            series.setName(timeseriesId);
            series.setData(formattedSeries);
            dataCollection.addNewTimeseries(timeseriesId, series);
        }
        return dataCollection;
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
