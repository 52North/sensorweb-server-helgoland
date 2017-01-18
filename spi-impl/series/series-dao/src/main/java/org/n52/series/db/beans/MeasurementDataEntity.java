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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class MeasurementDataEntity extends DataEntity<Double> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeasurementDataEntity.class);

    private static final Double DOUBLE_THRESHOLD = 0.01;

    @Override
    public boolean isNoDataValue(Collection<String> noDataValues) {
        Double value = getValue();
        return value == null
                || Double.isNaN(value)
                || containsValue(noDataValues, value);
    }

    private boolean containsValue(Collection<String> collection, double key) {
        if (collection == null) {
            return false;
        }
        for (Double noDataValue : convertToDoubles(collection)) {
            if (Math.abs(noDataValue / key - 1) < DOUBLE_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    private Collection<Double> convertToDoubles(Collection<String> collection) {
        List<Double> validatedValues = new ArrayList<>();
        for (String value : collection) {
            String trimmed = value.trim();
            try {
                validatedValues.add(Double.parseDouble(trimmed));
            } catch (NumberFormatException e) {
                LOGGER.trace("Ignoring NO_DATA value {} (not a double).", trimmed);
            }
        }
        return validatedValues;
    }

}
