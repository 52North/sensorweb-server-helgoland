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
package org.n52.io.response.series;

import java.io.Serializable;

public class MeasurementValue extends SeriesData implements Comparable<MeasurementValue>, Serializable {

    private static final long serialVersionUID = -7292181682632614697L;

    private Long timestamp;

    private Double value;

    public MeasurementValue() {
        // for serialization
    }

    public MeasurementValue(long timestamp, Double value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getValue() {
        return value == null
                ? Double.NaN
                : value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("TimeseriesValue [ ");
        sb.append("timestamp: ").append(timestamp).append(", ");
        sb.append("value: ").append(value);
        return sb.append(" ]").toString();
    }

    @Override
    public int compareTo(MeasurementValue o) {
        return getTimestamp().compareTo(o.getTimestamp());
    }
}
