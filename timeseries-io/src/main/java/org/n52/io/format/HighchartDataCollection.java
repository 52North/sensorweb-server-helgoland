
package org.n52.io.format;

import org.n52.io.v1.data.TimeseriesDataCollection;

public final class HighchartDataCollection extends TimeseriesDataCollection<HighchartSeries> {

    /*
     * (non-Javadoc)
     * 
     * @see org.n52.io.v1.data.TimeseriesDataCollection#getTimeseriesOutput()
     */
    @Override
    public Object getTimeseriesOutput() {
        /*
         * Output will look like:
         * 
         * [{ 
         *   "name":'station 1', 
         *   "data": [ 
         *       [360191600,398.625], [360191600,398.625], [360191600,398.625] ...  [360191600,398.625] 
         *   ]
         * }, 
         * { 
         *   "name":'station 2', 
         *   "data": [ 
         *     [360191600,398.625], [360191600,398.625], [360191600,398.625] ... [360191600,398.625] 
         *   ] 
         * }]
         */
        return getAllTimeseries().values();
    }

}
