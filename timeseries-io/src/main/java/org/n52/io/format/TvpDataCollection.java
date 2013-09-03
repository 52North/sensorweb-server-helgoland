
package org.n52.io.format;

import org.n52.io.v1.data.TimeseriesData;
import org.n52.io.v1.data.TimeseriesDataCollection;

/**
 * Represents a time value pair data format. This class acts as a convenience class to encapsulate
 * <code>TimeseriesDataCollection&lt;TimeseriesData></code>.
 */
public final class TvpDataCollection extends TimeseriesDataCollection<TimeseriesData> {

    @Override
    public Object getTimeseriesOutput() {
        return getAllTimeseries();
    }
}
