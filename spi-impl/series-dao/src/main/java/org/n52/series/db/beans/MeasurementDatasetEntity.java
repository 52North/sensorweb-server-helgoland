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
package org.n52.series.db.beans;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class MeasurementDatasetEntity extends DatasetEntity<MeasurementDataEntity> {

    private int numberOfDecimals;

    private UnitEntity unit;

    private Set<MeasurementDatasetEntity> referenceValues = new HashSet<>();

    public Set<MeasurementDatasetEntity> getReferenceValues() {
        return referenceValues;
    }

    public void setReferenceValues(Set<MeasurementDatasetEntity> referenceValues) {
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

    @Override
    public MeasurementDataEntity getFirstValue() {
        final MeasurementDataEntity firstValue = super.getFirstValue();
        if (firstValue != null) {
            Date when = firstValue.getTimestamp();
            Double value = firstValue.getValue();
            if (when == null || value == null) {
                return null; // empty component
            }
        }
        return firstValue;
    }

    @Override
    public MeasurementDataEntity getLastValue() {
        final MeasurementDataEntity lastValue = super.getLastValue();
        if (lastValue != null) {
            Date when = lastValue.getTimestamp();
            Double value = lastValue.getValue();
            if (when == null || value == null) {
                return null; // empty component
            }
        }
        return lastValue;
    }

}
