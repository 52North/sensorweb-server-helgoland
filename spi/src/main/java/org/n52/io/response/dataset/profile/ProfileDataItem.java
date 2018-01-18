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

package org.n52.io.response.dataset.profile;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;

public class ProfileDataItem<T> implements Comparable<ProfileDataItem<T>> {

    private String verticalUnit;

    private BigDecimal vertical;

    private BigDecimal verticalFrom;

    private BigDecimal verticalTo;

    private T value;

    public String getVerticalUnit() {
        return verticalUnit;
    }

    public void setVerticalUnit(String verticalUnit) {
        this.verticalUnit = verticalUnit;
    }

    public BigDecimal getVertical() {
        return vertical;
    }

    public void setVertical(BigDecimal vertical) {
        this.vertical = vertical;
    }

    public BigDecimal getVerticalFrom() {
        return verticalFrom;
    }

    public void setVerticalFrom(BigDecimal verticalFrom) {
        this.verticalFrom = verticalFrom;
    }

    public BigDecimal getVerticalTo() {
        return verticalTo;
    }

    public void setVerticalTo(BigDecimal verticalTo) {
        this.verticalTo = verticalTo;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public int compareTo(ProfileDataItem<T> o) {
        if (getVertical() != null && o.getVertical() != null) {
            return Comparator.comparing(ProfileDataItem<T>::getVertical)
                             .compare(this, o);
        } else {
            return Comparator.comparing(ProfileDataItem<T>::getVerticalFrom)
                             .thenComparing(ProfileDataItem<T>::getVerticalTo)
                             .compare(this, o);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, vertical, verticalFrom, verticalTo, verticalUnit);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ProfileDataItem)) {
            return false;
        }

        @SuppressWarnings("rawtypes")
        ProfileDataItem< ? > other = (ProfileDataItem) obj;
        return Objects.equals(this.value, other.value)
                && Objects.equals(this.vertical, other.vertical)
                && Objects.equals(this.verticalFrom, other.verticalFrom)
                && Objects.equals(this.verticalTo, other.verticalTo)
                && Objects.equals(this.verticalUnit, other.verticalUnit);
    }

}
