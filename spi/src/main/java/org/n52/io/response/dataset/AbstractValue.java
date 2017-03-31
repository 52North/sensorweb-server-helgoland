/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vividsolutions.jts.geom.Geometry;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.n52.io.geojson.GeoJSONGeometrySerializer;

public abstract class AbstractValue<T> implements Comparable<AbstractValue<?>>, Serializable {

    private static final long serialVersionUID = -1606015864495830281L;

    private Long timestart;

     // serves also as timeend
    private Long timestamp;

    private T value;

    private Geometry geometry;

    private Set<Map<String, Object>> parameters;

    private ValidTime validTime;

    public AbstractValue() {
    }

    public AbstractValue(Long timestamp, T value) {
        this(null, timestamp, value);
    }

    public AbstractValue(Long timestart, Long timeend, T value) {
        this.timestart = timestart;
        this.timestamp = timeend;
        this.value = value;
    }

    /**
     * @return the timestamp/timeend when {@link #value} has been observed.
     */
    public Long getTimestamp() {
        return this.timestamp;
    }

    /**
     * @param timestamp sets the timestamp/timeend when {@link #value} has been observed.
     */
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Optional.
     *
     * @return the timestart when {@link #value} has been observed.
     */
    public Long getTimestart() {
        return timestart;
    }

    /**
     * Optional.
     *
     * @param timestart the timestart when {@link #value} has been observed.
     */
    public void setTimestart(Long timestart) {
        this.timestart = timestart;
    }

    @JsonIgnore
    public boolean isNoDataValue() {
        return value == null;
    }

    @JsonInclude(content = Include.ALWAYS)
    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @JsonSerialize(using = GeoJSONGeometrySerializer.class)
    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @JsonIgnore
    public boolean isSetGeometry() {
        return geometry != null && !geometry.isEmpty();
    }

    public void setParameters(Set<Map<String, Object>> parameters) {
        this.parameters = new HashSet<>(parameters);
    }

    public Set<Map<String, Object>> getParameters() {
        return parameters != null
                ? Collections.unmodifiableSet(parameters)
                : null;
    }

    public void addParameter(Map<String, Object> parameterValue) {
        if (parameters == null) {
            parameters = new HashSet<>();
        }
        parameters.add(parameterValue);
    }

    public ValidTime getValidTime() {
        return validTime;
    }

    public void setValidTime(ValidTime validTime) {
        this.validTime = validTime;
    }

    @Override
    public int compareTo(AbstractValue<?> o) {
        // TODO check odering when `showtimeintervals=true`
        return getTimestamp().compareTo(o.getTimestamp());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append(" [ ");
        sb.append("timestart: ").append(getTimestart()).append(", ");
        sb.append("timestamp: ").append(getTimestamp()).append(", ");
        sb.append("value: ").append(getValue());
        return sb.append(" ]").toString();
    }

    public static class ValidTime {
        private Long start;

        private Long end;

        public ValidTime(Long start, Long end) {
            this.start = start;
            this.end = end;
        }

        public Long getStart() {
            return start;
        }

        public void setStart(Long start) {
            this.start = start;
        }

        public Long getEnd() {
            return end;
        }

        public void setEnd(Long end) {
            this.end = end;
        }
    }

}
