/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.io.response.dataset;

import java.util.HashMap;
import java.util.Map;

public class DataCollection<T> {

    /**
     * Associates series to a (custom client) id.
     */
    private Map<String, T> allSeries = new HashMap<>();

    public void addAll(DataCollection<T> seriesCollection) {
        allSeries.putAll(seriesCollection.getAllSeries());
    }

    public void addNewSeries(String reference, T series) {
        this.allSeries.put(reference, series);
    }

    public T getSeries(String seriesId) {
        return allSeries.get(seriesId);
    }

    /**
     * Returns all series mapped by series id.
     *
     * @return all series hold by this data collection.
     */
    public Map<String, T> getAllSeries() {
        return allSeries;
    }

    /**
     * <p>
     * Returns all series as simple collection. This method is intended only
     * for output serialization.
     * </p>
     * <p>
     * <b>Note:</b> Depending on the actual series data type no reference
     * can be made to the concrete series anymore! Use
     * {@link #getAllSeries()} if you need to keep reference.</p>
     *
     * @return the timeseries hold by this data collection.
     */
    public Object getSeriesOutput() {
        return getAllSeries();
    }

    public void setAllSeries(HashMap<String, T> series) {
        this.allSeries = series;
    }

}
