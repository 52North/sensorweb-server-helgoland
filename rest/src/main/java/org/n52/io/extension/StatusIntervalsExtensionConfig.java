/*
 * Copyright (C) 2013-2021 52°North Spatial Information Research GmbH
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
package org.n52.io.extension;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.n52.io.response.StatusInterval;

public class StatusIntervalsExtensionConfig {

    private Map<String, ConfigInterval> phenomenonIntervals = new LinkedHashMap<>();

    private Map<String, ConfigInterval> datasetIntervals = new LinkedHashMap<>();

    public Map<String, ConfigInterval> getPhenomenonIntervals() {
        return Collections.unmodifiableMap(phenomenonIntervals);
    }

    public void setPhenomenonIntervals(Map<String, ConfigInterval> phenomenonIntervals) {
        if (phenomenonIntervals != null) {
            this.phenomenonIntervals.putAll(phenomenonIntervals);
        }
    }

    /**
     * @return the intervals
     * @deprecated use {@link StatusIntervalsExtensionConfig#getDatasetIntervals()}
     */
    @Deprecated
    public Map<String, ConfigInterval> getTimeseriesIntervals() {
        return Collections.unmodifiableMap(getSeriesIntervals());
    }

    /**
     * @param timeseriesIntervals
     *            the intervals
     * @deprecated use {@link StatusIntervalsExtensionConfig#setDatasetIntervals(Map)}
     */
    @Deprecated
    public void setTimeseriesIntervals(Map<String, ConfigInterval> timeseriesIntervals) {
        setSeriesIntervals(timeseriesIntervals);
    }

    /**
     * @return the intervals
     * @deprecated use {@link StatusIntervalsExtensionConfig#getDatasetIntervals()}
     */
    @Deprecated
    public Map<String, ConfigInterval> getSeriesIntervals() {
        return getDatasetIntervals();
    }

    /**
     * @param seriesIntervals
     *            the intervals
     * @deprecated use {@link StatusIntervalsExtensionConfig#setDatasetIntervals(Map)}
     */
    @Deprecated
    public void setSeriesIntervals(Map<String, ConfigInterval> seriesIntervals) {
        setDatasetIntervals(seriesIntervals);
    }

    public Map<String, ConfigInterval> getDatasetIntervals() {
        return Collections.unmodifiableMap(datasetIntervals);
    }

    public void setDatasetIntervals(Map<String, ConfigInterval> datasetIntervals) {
        if (datasetIntervals != null) {
            this.datasetIntervals.putAll(datasetIntervals);
        }
    }

    public static class ConfigInterval {

        private Map<String, StatusInterval> statusIntervals = new HashMap<>();

        public Map<String, StatusInterval> getStatusIntervals() {
            return Collections.unmodifiableMap(statusIntervals);
        }

        public void setStatusIntervals(Map<String, StatusInterval> statusIntervals) {
            if (statusIntervals != null) {
                this.statusIntervals.putAll(statusIntervals);
            }
        }

    }
}
