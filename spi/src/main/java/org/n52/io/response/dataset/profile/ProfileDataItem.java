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

import org.n52.io.response.DetectionLimitOutput;
import org.n52.io.response.dataset.ValueFormatter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonPropertyOrder({
    "verticalFrom",
    "verticalTo",
    "vertical",
    "value",
    "detectionLimit"
})
public class ProfileDataItem<T> implements Comparable<ProfileDataItem<T>> {

    private BigDecimal verticalFrom;

    // serves also as verticalTo
    private BigDecimal vertical;

    private T value;

    private ValueFormatter<T> valueFormatter;

    private DetectionLimitOutput detectionLimit;

    public ProfileDataItem() {
    }

    public ProfileDataItem(BigDecimal vertical, T value) {
        this(null, vertical, value);
    }

    public ProfileDataItem(BigDecimal verticalFrom, BigDecimal verticalTo, T value) {
        this.verticalFrom = verticalFrom;
        this.vertical = verticalTo;
        this.value = value;
    }

    public BigDecimal getVerticalFrom() {
        return verticalFrom;
    }

    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    public void setVerticalFrom(BigDecimal verticalFrom) {
        this.verticalFrom = verticalFrom;
    }

    private boolean isSetVerticalFrom() {
        return this.verticalFrom != null;
    }

    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    public BigDecimal getVerticalTo() {
        return isSetVerticalFrom()
                ? this.vertical
                : null;
    }

    @JsonFormat(shape = JsonFormat.Shape.NUMBER_FLOAT)
    public BigDecimal getVertical() {
        return !isSetVerticalFrom()
                ? this.vertical
                : null;
    }

    public void setVertical(BigDecimal vertical) {
        this.vertical = vertical;
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

    public DetectionLimitOutput getDetectionLimit() {
        return detectionLimit;
    }

    public void setDetectionLimit(DetectionLimitOutput detectionLimit) {
        this.detectionLimit = detectionLimit;
    }

    @Override
    public int compareTo(ProfileDataItem<T> o) {
        if (isSetVerticalFrom() && o.isSetVerticalFrom()) {
            return Comparator.comparing(ProfileDataItem<T>::getVerticalFrom)
                             .thenComparing(ProfileDataItem<T>::getVerticalTo)
                             .compare(this, o);
        } else {
            if (getVertical() != null) {
                if (o.getVertical() != null) {
                    return Comparator.comparing(ProfileDataItem<T>::getVertical)
                            .compare(this, o);
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
        return Objects.hash(value, vertical, verticalFrom, vertical);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || ! (obj instanceof ProfileDataItem)) {
            return false;
        }

        @SuppressWarnings("rawtypes")
        ProfileDataItem< ? > other = (ProfileDataItem) obj;
        return Objects.equals(this.value, other.value)
                && Objects.equals(this.vertical, other.vertical)
                && Objects.equals(this.verticalFrom, other.verticalFrom)
                && Objects.equals(this.vertical, other.vertical);
    }

}
