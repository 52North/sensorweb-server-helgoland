/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.io.generalize;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

import java.util.Properties;
import org.n52.io.IoParameters;

import org.n52.io.format.TvpDataCollection;
import org.n52.io.v1.data.TimeseriesData;
import org.n52.io.v1.data.TimeseriesValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a generalizer using the Largest-Triangle-Three-Buckets algorithm
 *
 * https://github.com/sveinn-steinarsson/flot-downsample/
 */
public class LargestTriangleThreeBucketsGeneralizer extends Generalizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DouglasPeuckerGeneralizer.class);

    private static final String THRESHOLD = "threshold";

    private double threshold = 200; // fallback default

    public LargestTriangleThreeBucketsGeneralizer(IoParameters parameters) {
        super(parameters);
        try {
            threshold = parameters.containsParameter(THRESHOLD)
                    ? parseDouble(parameters.getOther(THRESHOLD))
                    : threshold;
        } catch (NumberFormatException ne) {
            LOGGER.error("Error while reading properties! Using fallback defaults.", ne);
            throw new IllegalStateException("Error while reading properties! Using fallback defaults.");
        }
    }

    @Override
    public String getName() {
        return "LargestTriangleThreeBuckets";
    }



    @Override
    public TvpDataCollection generalize(TvpDataCollection data) throws GeneralizerException {
        TvpDataCollection generalizedDataCollection = new TvpDataCollection();
        for (String timeseriesId : data.getAllTimeseries().keySet()) {
            TimeseriesData timeseries = data.getTimeseries(timeseriesId);
            TimeseriesData generalizedTimeseries = generalize(timeseries);
            generalizedTimeseries.setMetadata(timeseries.getMetadata());
            generalizedDataCollection.addNewTimeseries(timeseriesId, generalizedTimeseries);
        }
        return generalizedDataCollection;
    }

    private TimeseriesData generalize(TimeseriesData timeseries) {
        TimeseriesValue[] data = timeseries.getValues();

        int dataLength = data.length;

        if (threshold >= dataLength || threshold == 0) {
            return timeseries; // nothing to do
        }

        TimeseriesData sampled = new TimeseriesData();

        // Bucket size. Leave room for start and end data points
        double every = ((double) dataLength - 2) / (threshold - 2);

        int pointIndex = 0;
        sampled.addValues(data[pointIndex]);

        for (int i = 0; i < threshold - 2; i++) {
            // Calculate point average for next bucket (containing c)
            int avgRangeStart = (int) Math.floor((i + 1) * every) + 1;
            int avgRangeEnd = (int) Math.floor((i + 2) * every) + 1;
            double avgTimestamp = 0;
            double avgValue = 0;
            avgRangeEnd = avgRangeEnd < dataLength ? avgRangeEnd : dataLength;

            double avgRangeLength = avgRangeEnd - avgRangeStart;

            for (; avgRangeStart < avgRangeEnd; avgRangeStart++) {
                avgTimestamp += data[avgRangeStart].getTimestamp();
                Double value = data[avgRangeStart].getValue();
                if (value != null && !Double.isNaN(value)) {
                    avgValue += data[avgRangeStart].getValue();
                }
            }

            avgTimestamp /= avgRangeLength;
            avgValue /= avgRangeLength;

            // get the range for this bucket
            int rangeOff = (int) Math.floor((i + 0) * every) + 1;
            int rangeTo = (int) Math.floor((i + 1) * every) + 1;

            // Point a
            double tempTimestamp = data[pointIndex].getTimestamp();
            Double tempValue = data[pointIndex].getValue(); // XXX case (value == null || value == Double.NaN)

            double area;
            TimeseriesValue maxAreaPoint = null;
            int nextPointIndex = 0;
            double maxArea = area = -1;

            for (; rangeOff < rangeTo; rangeOff++) {
                // calculate triangle area over three buckets
                Double rangeOffValue = data[rangeOff].getValue();
                if (rangeOffValue != null && !Double.isNaN(rangeOffValue)) {
                    area = Math.abs((tempTimestamp - avgTimestamp) * (rangeOffValue - tempValue)
                            - (tempTimestamp - data[rangeOff].getTimestamp()) * (avgValue - tempValue)) * 0.5;
                    if (area > maxArea) {
                        maxArea = area;
                        maxAreaPoint = data[rangeOff];
                        nextPointIndex = rangeOff;
                    }
                }
            }

            sampled.addValues(maxAreaPoint); // Pick this point from the Bucket
            pointIndex = nextPointIndex; // This a is the next a
        }

        sampled.addValues(data[dataLength - 1]); // Allways add last value
        return sampled;
    }

}
