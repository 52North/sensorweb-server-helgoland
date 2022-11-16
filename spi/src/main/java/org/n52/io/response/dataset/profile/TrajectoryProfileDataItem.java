/*
 * Copyright (C) 2013-2022 52°North Spatial Information Research GmbH
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
package org.n52.io.response.dataset.profile;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;

import org.locationtech.jts.geom.Geometry;
import org.n52.io.geojson.GeoJSONGeometrySerializer;
import org.n52.io.response.TimeOutput;
import org.n52.io.response.TimeOutputConverter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
public class TrajectoryProfileDataItem<T> extends ProfileDataItem<T> {

    private TimeOutput timestart;

    // serves also as timeend
    private TimeOutput timestamp;

    private Geometry geometry;

    public TrajectoryProfileDataItem() {
    }

    public TrajectoryProfileDataItem(BigDecimal vertical, T value) {
        this(null, vertical, value);
    }

    public TrajectoryProfileDataItem(BigDecimal verticalFrom, BigDecimal verticalTo, T value) {
        super(verticalFrom, verticalTo, value);
    }

    /**
     * @return the timestamp/timeend when {@link #value} has been observed.
     */
    @JsonSerialize(converter = TimeOutputConverter.class)
    public TimeOutput getTimestamp() {
        return isSetTimestamp() ? this.timestamp : null;
    }

    /**
     * @param timestamp
     *            sets the timestamp/timeend when {@link #value} has been
     *            observed.
     */
    public void setTimestamp(TimeOutput timestamp) {
        this.timestamp = timestamp;
    }

    @JsonIgnore
    public boolean isSetTimestamp() {
        return !isSetTimestart() || isSetTimestart() && timestart.equals(timestamp);
    }

    @JsonSerialize(converter = TimeOutputConverter.class)
    public TimeOutput getTimeend() {
        return isSetTimestart() ? this.timestamp : null;
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
     *            the timestart when {@link #value} has been observed.
     */
    public void setTimestart(TimeOutput timestart) {
        this.timestart = timestart;
    }

    @JsonIgnore
    public boolean isSetTimestart() {
        return this.timestart != null;
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

    @Override
    public int compareTo(ProfileDataItem<T> o) {
        if (o == null || !(o instanceof TrajectoryProfileDataItem)) {
            return Comparator.comparing(TrajectoryProfileDataItem<T>::getTimestamp).compare(this,
                    (TrajectoryProfileDataItem<T>) o);
        } else if (isSetVerticalFrom() && o.isSetVerticalFrom()) {
            return Comparator.comparing(ProfileDataItem<T>::getVerticalFrom)
                    .thenComparing(ProfileDataItem<T>::getVerticalFrom)
                    .thenComparing(ProfileDataItem<T>::getVerticalTo).compare(this, o);
        } else {
            if (getVertical() != null) {
                if (o.getVertical() != null) {
                    return Comparator.comparing(ProfileDataItem<T>::getVertical).compare(this, o);
                } else {
                    return 0;
                }
            } else {
                return 1;
            }
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), timestart, timestamp, geometry);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TrajectoryProfileDataItem)) {
            return false;
        }

        @SuppressWarnings("rawtypes")
        TrajectoryProfileDataItem<?> other = (TrajectoryProfileDataItem) obj;
        return super.equals(obj) && Objects.equals(this.timestart, other.timestart)
                && Objects.equals(this.timestamp, other.timestamp) && Objects.equals(this.geometry, other.geometry);
    }
}
