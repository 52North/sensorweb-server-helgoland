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
package org.n52.series.db.beans;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.n52.series.db.beans.parameter.Parameter;


public abstract class DataEntity<T> {

    public static final String SERIES_PKID = "seriesPkid";

    private Long pkid;

    private Date timestart; // optional

    private Date timeend; // required

    private T value;

    private Long seriesPkid;

    private GeometryEntity geometry;

    private Boolean deleted;

    private Date validTimeStart;

    private Date validTimeEnd;

    private Date resultTime;

    private final Set<Parameter<?>> parameters = new HashSet<>(0);

    public Long getPkid() {
        return pkid;
    }

    public void setPkid(Long pkid) {
        this.pkid = pkid;
    }

    /**
     * @return timestamp
     * @deprecated use {@link #getTimeend()}
     */
    public Date getTimestamp() {
        return timeend;
    }

    /**
     * @param timestamp
     * @deprecated use {@link #setTimeend(java.util.Date)}
     */
    public void setTimestamp(Date timestamp) {
        this.timeend = timestamp;
    }

    /**
     * @return the timestart
     * @since 2.0.0
     */
    public Date getTimestart() {
        return timestart;
    }

    /**
     * @param timestart
     * @since 2.0.0
     */
    public void setTimestart(Date timestart) {
        this.timestart = timestart;
    }

    /**
     * @return the timeend
     * @since 2.0.0
     */
    public Date getTimeend() {
        return timeend;
    }

    /**
     *
     * @param timeend
     * @since 2.0.0
     */
    public void setTimeend(Date timeend) {
        this.timeend = timeend;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public abstract boolean isNoDataValue(Collection<String> noDataValues);

    public Long getSeriesPkid() {
        return seriesPkid;
    }

    public void setSeriesPkid(Long seriesPkid) {
        this.seriesPkid = seriesPkid;
    }

    public GeometryEntity getGeometry() {
        return geometry;
    }

    public void setGeometry(GeometryEntity geometry) {
        this.geometry = geometry;
    }

    public boolean isSetGeometry() {
        return geometry != null && !geometry.isEmpty();
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getValidTimeStart() {
        return validTimeStart;
    }

    public void setValidTimeStart(Date validTimeStart) {
        this.validTimeStart = validTimeStart;
    }

    public Date getValidTimeEnd() {
        return validTimeEnd;
    }

    public void setValidTimeEnd(Date validTimeEnd) {
        this.validTimeEnd = validTimeEnd;
    }

    public boolean isSetValidTime() {
        return isSetValidStartTime() && isSetValidEndTime();
    }

    public boolean isSetValidStartTime() {
        return validTimeStart != null;
    }

    public boolean isSetValidEndTime() {
        return validTimeEnd != null;
    }

    public Date getResultTime() {
        return resultTime;
    }

    public void setResultTime(Date resultTime) {
        this.resultTime = resultTime;
    }

    public Set<Parameter<?>> getParameters() {
        return parameters;
    }

    public void setParameters(Set<Parameter<?>> parameters) {
        if (parameters != null) {
            this.parameters.addAll(parameters);
        }
    }

    public boolean hasParameters() {
        return getParameters() != null && !getParameters().isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [");
        sb.append(" id: ").append(pkid);
        return sb.append(" ]").toString();
    }
}
