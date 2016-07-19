/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.io.output.dataset.count;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.n52.io.output.dataset.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CountObservationData extends Data {

    private static final long serialVersionUID = -3990317208637642482L;

    private List<CountValue> values = new ArrayList<>();

    private CountObservationDataMetadata metadata;

    public void addValues(CountValue... values) {
        if (values != null && values.length > 0) {
            this.values.addAll(Arrays.asList(values));
        }
    }

    /**
     * @param values the timestamp &lt;-&gt; value map.
     * @return a measurement data object.
     */
    public static CountObservationData newCountObservationData(Map<Long, Integer> values) {
        CountObservationData timeseries = new CountObservationData();
        for (Entry<Long, Integer> data : values.entrySet()) {
            timeseries.addNewValue(data.getKey(), data.getValue());
        }
        return timeseries;
    }

    public static CountObservationData newCountObservationData(CountValue... values) {
        CountObservationData timeseries = new CountObservationData();
        timeseries.addValues(values);
        return timeseries;
    }

    private void addNewValue(Long timestamp, Integer value) {
        values.add(new CountValue(timestamp, value));
    }

    /**
     * @return a sorted list of measurement values.
     */
    public CountValue[] getValues() {
        Collections.sort(values);
        return values.toArray(new CountValue[0]);
    }

    void setValues(CountValue[] values) {
        this.values = Arrays.asList(values);
    }

    @JsonProperty("extra")
    public CountObservationDataMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(CountObservationDataMetadata metadata) {
        this.metadata = metadata;
    }

    public long size() {
        return values.size();
    }

    @JsonIgnore
    public boolean hasReferenceValues() {
        return metadata != null
                && metadata.getReferenceValues() != null
                && !metadata.getReferenceValues().isEmpty();
    }

}
