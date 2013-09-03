package org.n52.io.format;

import org.n52.io.v1.data.TimeseriesDataCollection;



public interface TimeseriesDataFormatter<T extends TimeseriesDataCollection<?>> {

    public TimeseriesDataCollection<?> format(TvpDataCollection toFormat);
}
