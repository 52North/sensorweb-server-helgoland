/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.locationtech.jts.geom.Geometry;
import org.n52.io.geojson.GeoJSONGeometrySerializer;
import org.n52.io.response.TimeOutput;
import org.n52.io.response.TimeOutputConverter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public abstract class AbstractValue<T> implements Comparable<AbstractValue<T>>, Serializable {

    private static final long serialVersionUID = -1606015864495830281L;

    private TimeOutput timestart;

    // serves also as timeend
    private TimeOutput timestamp;

    private T value;

    private ValueFormatter<T> valueFormatter;

    private Geometry geometry;

    private Set<Map<String, Object>> parameters;

    private ValidTime validTime;

    private DateTime resultTime;

    public AbstractValue() {
    }

    public AbstractValue(TimeOutput timestamp, T value) {
        this(null, timestamp, value);
    }

    public AbstractValue(TimeOutput timestart, TimeOutput timeend, T value) {
        this.timestart = timestart;
        this.timestamp = timeend;
        this.value = value;
    }

    /**
     * @return the timestamp/timeend when {@link #value} has been observed.
     */
    @JsonSerialize(converter = TimeOutputConverter.class)
    public TimeOutput getTimestamp() {
        return this.timestamp;
    }

    /**
     * @param timestamp
     *        sets the timestamp/timeend when {@link #value} has been observed.
     */
    public void setTimestamp(TimeOutput timestamp) {
        this.timestamp = timestamp;
    }

    @JsonIgnore
    public boolean isSetTimestamp() {
        return !isSetTimestart() || (isSetTimestart() && timestart.equals(timestamp));
    }

    @JsonSerialize(converter = TimeOutputConverter.class)
    public TimeOutput getTimeend() {
        return isSetTimestart()
                ? this.timestamp
                : null;
    }

    @JsonIgnore
    public boolean isSetTimeend() {
        return this.timestamp != null && isSetTimestart();
    }

    /**
     * Optional.
     *
     * @return the timestart when {@link #value} has been observed.
     */
    @JsonSerialize(converter = TimeOutputConverter.class)
    public TimeOutput getTimestart() {
        return timestart;
    }

    /**
     * Optional.
     *
     * @param timestart
     *        the timestart when {@link #value} has been observed.
     */
    public void setTimestart(TimeOutput timestart) {
        this.timestart = timestart;
    }

    @JsonIgnore
    public boolean isSetTimestart() {
        return this.timestart != null;
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

    @JsonIgnore
    public void setValueFormatter(ValueFormatter<T> valueFormatter) {
        this.valueFormatter = valueFormatter;
    }

    /**
     * Formats value as string by using {@link #valueFormatter}. If no formatter has been set
     * {@link Object#toString()} is being used. Otherwise {@code null} is returned.
     *
     * @return the {@link #value} formatted as string or {@code null} if value is {@code null}
     */
    @JsonIgnore
    public String getFormattedValue() {
        if (value == null) {
            return null;
        }
        return valueFormatter != null
                ? valueFormatter.format(value)
                : value.toString();
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

    public void setValidTime(TimeOutput start, TimeOutput end) {
        this.validTime = new ValidTime(start, end);
    }

    @JsonIgnore
    public DateTime getResultTime() {
        return resultTime;
    }

    public void setResultTime(DateTime resultTime) {
        this.resultTime = resultTime;
    }

    @Override
    public int compareTo(AbstractValue<T> o) {
        return getTimestamp().compareTo(o.getTimestamp());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        return sb.append(" [ ")
                 .append("timestart: ")
                 .append(getTimestart())
                 .append(", ")
                 .append("timestamp: ")
                 .append(getTimestamp())
                 .append(", ")
                 .append("value: ")
                 .append(getValue())
                 .append(" ]")
                 .toString();
    }

    public class ValidTime {
        private TimeOutput start;

        private TimeOutput end;

        public ValidTime(TimeOutput start, TimeOutput end) {
            this.start = start;
            this.end = end;
        }

        @JsonSerialize(converter = TimeOutputConverter.class)
        public TimeOutput getStart() {
            return start;
        }

        public void setStart(TimeOutput start) {
            this.start = start;
        }

        @JsonSerialize(converter = TimeOutputConverter.class)
        public TimeOutput getEnd() {
            return end;
        }

        public void setEnd(TimeOutput end) {
            this.end = end;
        }

    }

}
