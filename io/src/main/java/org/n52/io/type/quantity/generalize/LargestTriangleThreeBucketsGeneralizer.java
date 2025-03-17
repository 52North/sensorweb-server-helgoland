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
package org.n52.io.type.quantity.generalize;

import java.math.BigDecimal;
import java.math.MathContext;

import org.joda.time.DateTime;
import org.n52.io.TvpDataCollection;
import org.n52.io.request.IoParameters;
import org.n52.io.response.TimeOutput;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetMetadata;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a generalizer using the Largest-Triangle-Three-Buckets algorithm
 *
 * @see <a href="https://github.com/sveinn-steinarsson/flot-downsample/">
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
        return generalizeData(data, timeseries.getMetadata());
    }

    private Data<QuantityValue> generalizeData(final QuantityValue[] data,
                                               final DatasetMetadata<QuantityValue> metadata) {
        final int dataLength = data.length;
        // Bucket size. Leave room for start and end data points
        double bucketSize = ((double) dataLength - 2) / (maxOutputValues - 2);

        Data<QuantityValue> sampled = new Data<>(metadata);
        sampled.addNewValue(data[0]);
        QuantityValue triangleLeft = data[0];

        for (int bucketIndex = 0; bucketIndex < (maxOutputValues - 2); bucketIndex++) {

            // get the range for this bucket
            int rangeOff = (int) Math.floor((bucketIndex + 0) * bucketSize) + 1;
            int rangeTo = (int) Math.floor((bucketIndex + 1) * bucketSize) + 1;

            // If this Bucket only contains noData we can skip all processing and just add nodata with at the beginning and end timestamps
            BucketAverage triangleMiddle = calculateBucketAverage(bucketIndex, bucketSize, data);
            if (triangleMiddle.getPointCount() == 0) {
                addNodataValue(sampled, data[rangeOff].getTimestamp());
                addNodataValue(sampled, data[rangeTo - 1].getTimestamp());
                triangleLeft = null;
                continue;
            } else if (triangleMiddle.getPointCount() == 1) {
                // If this Bucket only has one value skip all processing
                sampled.addNewValue(triangleMiddle.toTimeseriesValue());
                triangleLeft = triangleMiddle.toTimeseriesValue();
                continue;
            }

            // first point of triangle
            // if left bucket was empty, use first data value of this bucket instead.
            if (triangleLeft == null || triangleLeft.isNoDataValue()) {
                // Find first value in this bucket
                for (int i = rangeOff; i < rangeTo; i++) {
                    if (!data[i].isNoDataValue()) {
                        sampled.addNewValue(data[i]);
                        triangleLeft = data[i];
                        break;
                    }
                }
            }

            // last point of triangle (next bucket's average)
            // if right bucket was empty, use last data value of this bucket instead.
            BucketAverage rightBucket = calculateBucketAverage(bucketIndex + 1, bucketSize, data);
            QuantityValue triangleRight = new QuantityValue();
            if (rightBucket.getPointCount() == 0) {
                for (int i = rangeTo; i > rangeOff; i--) {
                    if (!data[i].isNoDataValue()) {
                        triangleRight = data[i];
                        break;
                    }
                }
            } else {
                triangleRight = rightBucket.toTimeseriesValue();
            }

            int amountOfNodataValues = 0;
            double maxArea = -1;
            boolean thresholdExceeded = false;
            QuantityValue maxAreaPoint = null;
            for (; rangeOff < rangeTo; rangeOff++) {
                final QuantityValue triangleMiddleCandidate = data[rangeOff];

                if (triangleMiddleCandidate.isNoDataValue()) {
                    amountOfNodataValues++;
                    if (isExceededGapThreshold(amountOfNodataValues, bucketSize)) {
                        thresholdExceeded = true;
                        break;
                    }
                } else {
                    double area = calcTriangleArea(triangleLeft, triangleRight, triangleMiddleCandidate);
                    if (area > maxArea) {
                        maxArea = area;
                        maxAreaPoint = triangleMiddleCandidate;
                    }
                }
            }

            if (thresholdExceeded) {
                for (; rangeOff < rangeTo; rangeOff++) {
                    sampled.addNewValue(data[rangeOff]);
                }
                triangleLeft = data[rangeOff];
            } else {
                // Pick this point from the Bucket
                sampled.addNewValue(maxAreaPoint);
                triangleLeft = maxAreaPoint;
            }

            // Our next bucket is empty so we add last point of this bucket to have a clean
            if (rightBucket.getPointCount() == 0) {
                sampled.addNewValue(triangleRight);
                triangleLeft = triangleRight;
            }
        }

        // Always add last value
        sampled.addNewValue(data[dataLength - 1]);
        return sampled;
    }

    private boolean isExceededGapThreshold(int amountOfNodataValues,
                                           double bucketSize) {
        return noDataGapThreshold <= 1
                // max percent
                ? amountOfNodataValues > (noDataGapThreshold * bucketSize)
                // max absolute
                : amountOfNodataValues > noDataGapThreshold;
    }

    private void addNodataValue(Data<QuantityValue> sampled, TimeOutput timeOutput) {
        sampled.addNewValue(createQuantityValue(timeOutput));
    }

    private static double calcTriangleArea(QuantityValue left,
                                           QuantityValue right, QuantityValue middle) {
        BigDecimal middleValue = middle.getValue();
        final BigDecimal leftValue = left.getValue();
        final BigDecimal rightValue = right.getValue();
        return Math.abs(((left.getTimestamp().getMillis() - right.getTimestamp().getMillis())
                * (middleValue.subtract(leftValue).doubleValue()))
                - ((left.getTimestamp().getMillis() - middle.getTimestamp().getMillis())
                * (rightValue.subtract(leftValue).doubleValue()))) * 0.5;
    }

    private BucketAverage calculateBucketAverage(int bucketIndex,
                                                 double bucketSize, QuantityValue[] data) {

        int dataLength = data.length;
        int avgRangeStart = (int) Math.floor((bucketIndex + 0) * bucketSize) + 1;
        int avgRangeEnd = (int) Math.floor((bucketIndex + 1) * bucketSize) + 1;
        avgRangeEnd = avgRangeEnd < dataLength ? avgRangeEnd : dataLength;
        double avgRangeLength = avgRangeEnd - avgRangeStart;

        double avgTimestamp = 0d;
        BigDecimal avgValue = BigDecimal.ZERO;
        int amountOfNodataValues = 0;
        boolean unixTime = false;

        for (; avgRangeStart < avgRangeEnd; avgRangeStart++) {
            final QuantityValue current = data[avgRangeStart];
            avgTimestamp += current.getTimestamp().getMillis();
            unixTime = current.getTimestamp().isUnixTime();
            if (current.isNoDataValue()) {
                // exclude this value from average calculation.
                amountOfNodataValues++;
            } else {
                avgValue = avgValue.add(current.getValue());
            }
        }

        avgTimestamp /= avgRangeLength;
        if (avgRangeLength == amountOfNodataValues) {
            // Totally empty bucket
            return new BucketAverage(avgTimestamp, null, unixTime, 0);
        } else {
            // at least (avgRangeLength-amountOfNodataValues) values were added
            avgValue = avgValue.divide(BigDecimal.valueOf(avgRangeLength - amountOfNodataValues), MathContext.DECIMAL128);
            return new BucketAverage(avgTimestamp, avgValue, unixTime, avgRangeLength - amountOfNodataValues);
        }
    }

    private QuantityValue createQuantityValue(TimeOutput timeOutput) {
        QuantityValue value = new QuantityValue();
        value.setTimestamp(timeOutput);
        return value;
    }

    private QuantityValue createQuantityValue(long fallBackTimestamp, TimeOutput timestamp) {
        QuantityValue value = new QuantityValue();
        value.setTimestamp(new TimeOutput(new DateTime(fallBackTimestamp), timestamp.isUnixTime()));
        return value;
    }

    private static class BucketAverage {

        private final Double timestamp;
        private final BigDecimal value;
        private final boolean unixTime;
        private final double pointCount;


        BucketAverage(Double timestamp, BigDecimal value, boolean unixTime, double pointCount) {
            this.timestamp = timestamp;
            this.value = value;
            this.unixTime = unixTime;
            this.pointCount = pointCount;
        }

        QuantityValue toTimeseriesValue() {
            QuantityValue quantity = new QuantityValue();
            quantity.setTimestamp(new TimeOutput(new DateTime(timestamp.longValue()), unixTime));
            quantity.setValue(value);
            return quantity;
        }

        public double getPointCount() {
            return pointCount;
        }

    }
}
