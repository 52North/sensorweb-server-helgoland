/**
 * ﻿Copyright (C) 2012
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

package org.n52.io.v1.data;

import java.util.HashMap;
import java.util.Map;

public abstract class TimeseriesDataCollection<T> {

    /**
     * Associates timeseries to a (custom client) id.
     */
    private Map<String, T> allTimeseries = new HashMap<String, T>();

    public TimeseriesDataCollection() {
        // for serialization
    }

    public void addAll(TimeseriesDataCollection<T> timseriesCollection) {
        allTimeseries.putAll(timseriesCollection.getAllTimeseries());
    }

    public void addNewTimeseries(String reference, T timeseries) {
        this.allTimeseries.put(reference, timeseries);
    }

    public T getTimeseries(String timeseriesId) {
        return allTimeseries.get(timeseriesId);
    }

    /**
     * Returns all timeseries mapped by timeseriesId.
     * 
     * @return all timeseries hold by this data collection.
     */
    public Map<String, T> getAllTimeseries() {
        return allTimeseries;
    }

    /**
     * Returns all timeseries as simple collection. This method is intended only for output serialization.<br/>
     * <br/>
     * <b>Note:</b> Depending on the actual timeseries data type no reference can be made to the concrete
     * timeseries anymore! Use {@link #getAllTimeseries()} if you need to keep reference.
     * 
     * @return the timeseries hold by this data collection.
     */
    public abstract Object getTimeseriesOutput();

    public void setAllTimeseries(HashMap<String, T> timeseries) {
        this.allTimeseries = timeseries;
    }

}
