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
package org.n52.series.db.da.beans.ext;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.n52.series.db.da.beans.UnitEntity;

public class MeasurementSeriesEntity extends AbstractSeriesEntity<MeasurementEntity> {

    private int numberOfDecimals;

    private UnitEntity unit;

    private Set<MeasurementSeriesEntity> referenceValues = new HashSet<>();

    private MeasurementEntity firstValue;

    private MeasurementEntity lastValue;

    public Set<MeasurementSeriesEntity> getReferenceValues() {
        return referenceValues;
    }

    public void setReferenceValues(Set<MeasurementSeriesEntity> referenceValues) {
        this.referenceValues = referenceValues;
    }

    public int getNumberOfDecimals() {
        return numberOfDecimals;
    }

    public void setNumberOfDecimals(int numberOfDecimals) {
        this.numberOfDecimals = numberOfDecimals;
    }

    public UnitEntity getUnit() {
        return unit;
    }

    public void setUnit(UnitEntity unit) {
        this.unit = unit;
    }

    public boolean hasUnit() {
        return unit != null;
    }

    public String getUnitI18nName(String locale) {
        String name = null;
        if (unit != null) {
            name = unit.getNameI18n(locale);
        }
        return name;
    }

    public MeasurementEntity getFirstValue() {
        if (firstValue != null) {
            Date when = firstValue.getTimestamp();
            Double value = firstValue.getValue();
            if (when == null || value == null) {
                return null; // empty component
            }
        }
        return firstValue;
    }

    public void setFirstValue(MeasurementEntity firstValue) {
        this.firstValue = firstValue;
    }

    public MeasurementEntity getLastValue() {
        if (lastValue != null) {
            Date when = lastValue.getTimestamp();
            Double value = lastValue.getValue();
            if (when == null || value == null) {
                return null; // empty component
            }
        }
        return lastValue;
    }

    public void setLastValue(MeasurementEntity lastValue) {
        this.lastValue = lastValue;
    }

}
