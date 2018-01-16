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

public class ProfileDataItem<T> implements Comparable<ProfileDataItem<T>> {

    private String verticalUnit;

    private Double vertical;

    private Double verticalFrom;

    private Double verticalTo;

    private T value;

    public String getVerticalUnit() {
        return verticalUnit;
    }

    public void setVerticalUnit(String verticalUnit) {
        this.verticalUnit = verticalUnit;
    }

    public Double getVertical() {
        return vertical;
    }

    public void setVertical(Double vertical) {
        this.vertical = vertical;
    }

    public Double getVerticalFrom() {
        return verticalFrom;
    }

    public void setVerticalFrom(Double verticalFrom) {
        this.verticalFrom = verticalFrom;
    }

    public Double getVerticalTo() {
        return verticalTo;
    }

    public void setVerticalTo(Double verticalTo) {
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
            return Double.compare(getVertical(), o.getVertical());
        } else if (getVerticalFrom() != null && o.getVerticalFrom() != null && getVerticalTo() != null
                && o.getVerticalTo() != null) {
            int from = Double.compare(getVerticalFrom(), o.getVerticalFrom());
            int to = Double.compare(getVerticalTo(), o.getVerticalTo());
            return from == to ? from : -1;
        }
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null)
                ? 0
                : value.hashCode());
        result = prime * result + ((vertical == null)
                ? 0
                : vertical.hashCode());
        result = prime * result + ((verticalFrom == null)
                ? 0
                : verticalFrom.hashCode());
        result = prime * result + ((verticalTo == null)
                ? 0
                : verticalTo.hashCode());
        result = prime * result + ((verticalUnit == null)
                ? 0
                : verticalUnit.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ProfileDataItem<?> other = (ProfileDataItem) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        if (vertical == null) {
            if (other.vertical != null) {
                return false;
            }
        } else if (!vertical.equals(other.vertical)) {
            return false;
        } else if (!verticalFrom.equals(other.verticalFrom)) {
            return false;
        } else if (!verticalTo.equals(other.verticalTo)) {
            return false;
        }
        if (verticalUnit == null) {
            if (other.verticalUnit != null) {
                return false;
            }
        } else if (!verticalUnit.equals(other.verticalUnit)) {
            return false;
        }
        return true;
    }

}
