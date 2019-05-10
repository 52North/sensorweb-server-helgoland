/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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

import java.math.BigDecimal;
import java.math.MathContext;

import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.io.series.TvpDataCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a generalizer using the Largest-Triangle-Three-Buckets algorithm
 *
 * @see
 * <a href="https://github.com/sveinn-steinarsson/flot-downsample/">
 * https://github.com/sveinn-steinarsson/flot-downsample/</a>
 */
public class LargestTriangleThreeBucketsGeneralizer extends Generalizer<Data<QuantityValue>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            LargestTriangleThreeBucketsGeneralizer.class);

    private static final String THRESHOLD = "threshold";

    private static final String NO_DATA_GAP_THRESHOLD = "noDataGapThreshold";

    // fallback default
    private double maxOutputValues = 200;

    // fallback default
    private double noDataGapThreshold = 0.2d;

    public LargestTriangleThreeBucketsGeneralizer(IoParameters parameters) {
        super(parameters);
        try {
            maxOutputValues = parameters.containsParameter(THRESHOLD)
                    ? Double.parseDouble(parameters.getOther(THRESHOLD))
                    : maxOutputValues;
            noDataGapThreshold = parameters.containsParameter(NO_DATA_GAP_THRESHOLD)
                    ? Double.parseDouble(parameters.getOther(NO_DATA_GAP_THRESHOLD))
                    : noDataGapThreshold;
        } catch (NumberFormatException ne) {
            LOGGER.error("Error reading properties! Using fallback.", ne);
        }
    }

    @Override
    public String getName() {
        return "LargestTriangleThreeBuckets";
    }

    @Override
    public DataCollection<Data<QuantityValue>> generalize(
            DataCollection<Data<QuantityValue>> data) throws GeneralizerException {
        TvpDataCollection<Data<QuantityValue>> generalizedDataCollection = new TvpDataCollection<>();
        for (String timeseriesId : data.getAllSeries().keySet()) {
            Data<QuantityValue> timeseries = data.getSeries(timeseriesId);
            Data<QuantityValue> generalizedTimeseries = generalize(timeseries);
            generalizedTimeseries.setMetadata(timeseries.getMetadata());
            generalizedDataCollection.addNewSeries(timeseriesId, generalizedTimeseries);
        }
        return generalizedDataCollection;
    }

    private Data<QuantityValue> generalize(Data<QuantityValue> timeseries) {
        QuantityValue[] data = timeseries.getValues().toArray(new QuantityValue[0]);

        int dataLength = data.length;
        if (maxOutputValues >= dataLength || maxOutputValues == 0) {
            // nothing to do
            return timeseries;
        }
        return generalizeData(data);
    }

    private Data<QuantityValue> generalizeData(QuantityValue[] data) {
        int dataLength = data.length;
        // Bucket size. Leave room for start and end data points
        double bucketSize = ((double) dataLength - 2) / (maxOutputValues - 2);

        int pointIndex = 0;
        Data<QuantityValue> sampled = new Data<>();
        sampled.addValues(data[pointIndex]);

        for (int bucketIndex = 0; bucketIndex < maxOutputValues - 2;
                bucketIndex++) {

            // get the range for this bucket
            int rangeOff = (int) Math.floor((bucketIndex + 0) * bucketSize) + 1;
            int rangeTo = (int) Math.floor((bucketIndex + 1) * bucketSize) + 1;

            // first point of triangle
            QuantityValue triangleLeft = data[pointIndex];
            if (triangleLeft.isNoDataValue()) {
                addNodataValue(sampled, triangleLeft.getTimestamp());
                pointIndex = rangeTo - 1;
                continue;
            }

            // last point of triangle (next bucket's average)
            BucketAverage triangleRight = calculateBucketAverage(bucketIndex + 1,
                    bucketSize, data);

            // init fallback value
            BucketAverage avgCurrentBucket = calculateBucketAverage(bucketIndex, bucketSize, data);
            long fallBackTimestamp = avgCurrentBucket.toTimeseriesValue().getTimestamp();
            QuantityValue maxAreaPoint = new QuantityValue(fallBackTimestamp, null);

            double area;
            int amountOfNodataValues = 0;
            double maxArea = area = -1;
            int nextPointIndex = 0;

            for (; rangeOff < rangeTo; rangeOff++) {

                //if (triangleRight.isNoDataBucket()) {
                //  triangleRight = // TODO
                //}
                // calculate triangle area over three buckets
                final QuantityValue triangleMiddle = data[rangeOff];

                if (triangleMiddle.isNoDataValue()) {
                    amountOfNodataValues++;
                    if (isExceededGapThreshold(amountOfNodataValues, bucketSize)) {
                        if (triangleMiddle.isNoDataValue()) {
                            maxAreaPoint = avgCurrentBucket.toTimeseriesValue();
                            LOGGER.debug("No data value for bucket {}.",
                                    bucketIndex);
                            pointIndex = rangeTo - 1;
                            break;
                        }
                    }
                } else {
                    area = calcTriangleArea(triangleLeft, triangleRight,
                            triangleMiddle);
                    if (area > maxArea) {
                        maxArea = area;
                        maxAreaPoint = triangleMiddle;
                        nextPointIndex = rangeOff;
                    }
                }
            }

            // Pick this point from the Bucket
            sampled.addValues(maxAreaPoint);
            // This a is the next a
            pointIndex = nextPointIndex;
        }

        // Always add last value
        sampled.addValues(data[dataLength - 1]);
        return sampled;
    }

    private boolean isExceededGapThreshold(int amountOfNodataValues,
            double bucketSize) {
        return noDataGapThreshold <= 1
                // max percent
                ? amountOfNodataValues > noDataGapThreshold * bucketSize
                // max absolute
                : amountOfNodataValues > noDataGapThreshold;
    }

    private void addNodataValue(Data<QuantityValue> sampled, long timestamp) {
        sampled.addValues(new QuantityValue(timestamp, null));
    }

    private static double calcTriangleArea(QuantityValue left,
            BucketAverage right, QuantityValue middle) {
        BigDecimal middleValue = middle.getValue();
        final BigDecimal leftValue = left.getValue();
        final BigDecimal rightValue = right.value;
        return Math.abs((left.getTimestamp() - right.timestamp)
                * (middleValue.subtract(leftValue).doubleValue())
                - (left.getTimestamp() - middle.getTimestamp())
                * (rightValue.subtract(leftValue).doubleValue())) * 0.5;
    }

    private BucketAverage calculateBucketAverage(int bucketIndex,
            double bucketSize, QuantityValue[] data) {

        int dataLength = data.length;
        int avgRangeStart = (int) Math.floor((bucketIndex + 0) * bucketSize) + 1;
        int avgRangeEnd = (int) Math.floor((bucketIndex + 1) * bucketSize) + 1;
        avgRangeEnd = avgRangeEnd < dataLength ? avgRangeEnd : dataLength;
        double avgRangeLength = avgRangeEnd - avgRangeStart;

        Double avgTimestamp = 0d;
        BigDecimal avgValue = BigDecimal.ZERO;
        int amountOfNodataValues = 0;
        boolean noDataThresholdExceeded = false;
        for (; avgRangeStart < avgRangeEnd; avgRangeStart++) {
            final QuantityValue current = data[avgRangeStart];
            avgTimestamp += current.getTimestamp();
            if (noDataThresholdExceeded) {
                // keep on calc avg timestamp
                continue;
            }
            if (current.isNoDataValue()) {
                amountOfNodataValues++;
                if (amountOfNodataValues == noDataGapThreshold) {
                    noDataThresholdExceeded = true;
                }
            } else {
                avgValue = avgValue.add(current.getValue());
            }
        }

        avgTimestamp /= avgRangeLength;
        avgValue = avgValue.divide(BigDecimal.valueOf(avgRangeLength), MathContext.DECIMAL128);
        return new BucketAverage(avgTimestamp, avgValue);
    }

    private static class BucketAverage {

        private Double timestamp;
        private BigDecimal value;

        BucketAverage(Double timestamp, BigDecimal value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        QuantityValue toTimeseriesValue() {
            return new QuantityValue(timestamp.longValue(), value);
        }
    }
}
