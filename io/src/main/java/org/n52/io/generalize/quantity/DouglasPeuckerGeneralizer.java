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
package org.n52.io.generalize.quantity;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.util.Arrays;

import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.io.series.TvpDataCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a generalizer using the Douglas-Peucker Algorithm
 *
 * Characteristic quantity values are picked depending on a given tolerance value. Values that
 * differ less than this tolerance value from an ideal line between some minima and maxima will be
 * dropped.
 */
public final class DouglasPeuckerGeneralizer extends Generalizer<Data<QuantityValue>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DouglasPeuckerGeneralizer.class);

    /**
     * Config-key for {@link #maxEntries} of entries.
     */
    private static final String MAX_ENTRIES = "MAX_ENTRIES";

    /**
     * Config-key for {@link #reductionRate}.
     */
    private static final String REDUCTION_RATE = "REDUCTION_RATE";

    /**
     * Config-key for the {@link #toleranceValue}.
     */
    private static final String TOLERANCE_VALUE = "TOLERANCE_VALUE";

    /**
     * {@link #maxEntries} is the value for the maximum points the generalizer will handle,
     * otherwise an exception will be thrown; -1 is unlimited
     */
    // fallback default
    private int maxEntries = -1;

    /**
     * estimated reduction rate for this use case, where {@link #reductionRate} = 3 means the time
     * series is reduced to 1/3 of it's size; -1 means there is no proper empirical value
     */
    // fallback default
    private int reductionRate = -1;

    /**
     * Absolute tolerance value.
     */
    // fallback default
    private double toleranceValue = 0.1;

    /**
     * Creates a new instance. Use static constructors for instantiation.
     *
     * @param parameters Configuration parameters. If <code>null</code> a fallback configuration
     * will be used.
     */
    public DouglasPeuckerGeneralizer(IoParameters parameters) {
        super(parameters);
        try {
            maxEntries = parameters.containsParameter(MAX_ENTRIES)
                    ? Integer.parseInt(parameters.getOther(MAX_ENTRIES))
                    : maxEntries;
            reductionRate = parameters.containsParameter(REDUCTION_RATE)
                    ? Integer.parseInt(parameters.getOther(REDUCTION_RATE))
                    : reductionRate;
            toleranceValue = parameters.containsParameter(TOLERANCE_VALUE)
                    ? Double.parseDouble(parameters.getOther(TOLERANCE_VALUE))
                    : toleranceValue;
        } catch (NumberFormatException ne) {
            LOGGER.error(
                    "Error while reading properties!  Using fallback defaults.",
                    ne);
            throw new IllegalStateException(
                    "Error while reading properties! Using fallback defaults.");
        }
    }

    @Override
    public String getName() {
        return "Douglas-Peucker";
    }

    @Override
    public DataCollection<Data<QuantityValue>> generalize(DataCollection<Data<QuantityValue>> data)
            throws GeneralizerException {
        TvpDataCollection<Data<QuantityValue>> generalizedDataCollection = new TvpDataCollection<>();
        for (String timeseriesId : data.getAllSeries().keySet()) {
            Data<QuantityValue> timeseries = data.getSeries(timeseriesId);
            generalizedDataCollection.addNewSeries(timeseriesId, generalize(timeseries));
        }
        return generalizedDataCollection;
    }

    private Data<QuantityValue> generalize(Data<QuantityValue> timeseries) throws
            GeneralizerException {
        QuantityValue[] originalValues = getValueArray(timeseries);
        if (originalValues.length < 3 || toleranceValue <= 0) {
            return timeseries;
        }

        if (maxEntries != -1 && originalValues.length > maxEntries) {
            throw new GeneralizerException(
                    "Maximum number of entries exceeded ("
                    + originalValues.length + ">"
                    + maxEntries + ")!");
        }

        Data<QuantityValue> generalizedTimeseries = new Data<>();
        QuantityValue[] generalizedValues = recursiveGeneralize(timeseries);
        generalizedTimeseries.addValues(generalizedValues);

        // add first element if new list is empty
        if (generalizedValues.length == 0/* && originalValues.length > 0*/) {
            generalizedTimeseries.addValues(originalValues[0]);
        }

        // add the last one if not already contained!
        if (generalizedValues.length > 0/* && originalValues.length > 0*/) {
            QuantityValue lastOriginialValue = originalValues[originalValues.length - 1];
            QuantityValue lastGeneralizedValue = generalizedValues[generalizedValues.length - 1];
            if (!lastGeneralizedValue.getTimestamp().equals(lastOriginialValue.getTimestamp())) {
                generalizedTimeseries.addValues(lastOriginialValue);
            }
        }
        return generalizedTimeseries;
    }

    private QuantityValue[] getValueArray(Data<QuantityValue> timeseries) {
        return timeseries.getValues()
                .toArray(new QuantityValue[0]);
    }

    private QuantityValue[] recursiveGeneralize(Data<QuantityValue> timeseries) {
        QuantityValue[] values = getValueArray(timeseries);
        QuantityValue startValue = getFirstValue(timeseries);
        QuantityValue endValue = getLastValue(timeseries);
        Line2D.Double line = createTendencyLine(startValue, endValue);

        // find the point of maximum distance to the line
        int index = 0;
        double maxDist = 0;
        double distance;

        // start and end value are not mentioned
        for (int i = 1; i < values.length - 1; i++) {
            QuantityValue timeseriesValue = values[i];
            distance = calculateDistance(line, timeseriesValue);
            if (distance > maxDist) {
                index = i;
                maxDist = distance;
            }
        }

        if (maxDist < toleranceValue) {
            return getValueArray(timeseries);
        } else {
            // split and handle both parts separately
            Data<QuantityValue> generalizedData = new Data<>();
            Data<QuantityValue> firstPartToBeGeneralized = new Data<>();
            Data<QuantityValue> restPartToBeGeneralized = new Data<>();
            firstPartToBeGeneralized.addValues(Arrays.copyOfRange(values, 0, index));
            restPartToBeGeneralized.addValues(Arrays.copyOfRange(values, index + 1, values.length));
            generalizedData.addValues(recursiveGeneralize(firstPartToBeGeneralized));
            generalizedData.addValues(recursiveGeneralize(restPartToBeGeneralized));
            return getValueArray(generalizedData);
        }

    }

    private double calculateDistance(Line2D.Double line, QuantityValue timeseriesValue) {
        return line.ptLineDist(createPoint(timeseriesValue));
    }

    private Point2D.Double createPoint(QuantityValue timeseriesValue) {
        Long timestamp = timeseriesValue.getTimestamp();
        BigDecimal value = timeseriesValue.getValue();

        Point2D.Double p = new Point2D.Double();
        p.setLocation(timestamp, value.doubleValue());
        return p;
    }

    private Line2D.Double createTendencyLine(QuantityValue start, QuantityValue end) {
        Long startTime = start.getTimestamp();
        BigDecimal startValue = start.getValue();
        Long endTime = end.getTimestamp();
        BigDecimal endValue = end.getValue();
        return new Line2D.Double(startTime, startValue.doubleValue(), endTime, endValue.doubleValue());
    }

    private QuantityValue getFirstValue(Data<QuantityValue> timeseries) {
        QuantityValue[] values = getValueArray(timeseries);
        if (values == null || values.length == 0) {
            throwNewMustNotBeEmptyException();
        }
        return values[0];
    }

    private QuantityValue getLastValue(Data<QuantityValue> timeseries) {
        QuantityValue[] values = getValueArray(timeseries);
        if (values == null || values.length == 0) {
            throwNewMustNotBeEmptyException();
            return null;
        }
        return values[values.length - 1];
    }

    private void throwNewMustNotBeEmptyException() throws IllegalArgumentException {
        throw new IllegalArgumentException("Timeseries must not be empty.");
    }

}
