/**
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
