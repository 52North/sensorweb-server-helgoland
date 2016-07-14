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
package org.n52.io.measurement.generalize;

import static java.lang.Double.parseDouble;

import org.n52.io.request.IoParameters;
import org.n52.io.response.series.MeasurementData;
import org.n52.io.response.series.MeasurementValue;
import org.n52.io.response.series.DataCollection;
import org.n52.io.series.TvpDataCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a generalizer using the Largest-Triangle-Three-Buckets
 * algorithm
 *
 * https://github.com/sveinn-steinarsson/flot-downsample/
 */
public class LargestTriangleThreeBucketsGeneralizer extends Generalizer<MeasurementData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DouglasPeuckerGeneralizer.class);

    private static final String THRESHOLD = "threshold";

    private static final String NO_DATA_GAP_THRESHOLD = "noDataGapThreshold";

    private double maxOutputValues = 200; // fallback default

    private double noDataGapThreshold = 0.2d; // fallback default

    public LargestTriangleThreeBucketsGeneralizer(IoParameters parameters) {
        super(parameters);
        try {
            maxOutputValues = parameters.containsParameter(THRESHOLD)
                    ? parseDouble(parameters.getOther(THRESHOLD))
                    : maxOutputValues;
            noDataGapThreshold = parameters.containsParameter(NO_DATA_GAP_THRESHOLD.toLowerCase())
                    ? parseDouble(parameters.getOther(NO_DATA_GAP_THRESHOLD.toLowerCase()))
                    : noDataGapThreshold;
        } catch (NumberFormatException ne) {
            LOGGER.error("Error while reading properties! Using fallback defaults.", ne);
//            throw new IllegalStateException("Error while reading properties! Using fallback defaults.");
        }
    }

    @Override
    public String getName() {
        return "LargestTriangleThreeBuckets";
    }

    @Override
    public DataCollection<MeasurementData> generalize(DataCollection<MeasurementData> data) throws GeneralizerException {
        TvpDataCollection<MeasurementData> generalizedDataCollection = new TvpDataCollection<MeasurementData>();
        for (String timeseriesId : data.getAllSeries().keySet()) {
            MeasurementData timeseries = data.getSeries(timeseriesId);
            MeasurementData generalizedTimeseries = generalize(timeseries);
            generalizedTimeseries.setMetadata(timeseries.getMetadata());
            generalizedDataCollection.addNewSeries(timeseriesId, generalizedTimeseries);
        }
        return generalizedDataCollection;
    }

    private MeasurementData generalize(MeasurementData timeseries) {
        MeasurementValue[] data = timeseries.getValues();

        int dataLength = data.length;

        if (maxOutputValues >= dataLength || maxOutputValues == 0) {
            return timeseries; // nothing to do
        }

//        int amountOfNaN = 0;
//        for (int i = 0 ; i < dataLength ; i++) {
//            if (data[i].getValue().isNaN()) {
//                amountOfNaN++;
//            }
//        }
//        int offset = 0;
//        int amountNaNsInSequence = 0;
//        List<TimeseriesValue[]> dataChunks = new ArrayList<>();
//        for (int i = 0 ; i < dataLength ; ) {
//            if ( !data[i].getValue().isNaN()) {
//                i++; // continue, if normal number
//                continue;
//            }
//            for (int j = 0 ; i + j < dataLength ; j++) {
//                final int currentIdx = i + j;
//                final int lastIdx = currentIdx - 1;
//                if (lastIdx >= 0 && !data[lastIdx].getValue().isNaN()) {
//                    offset = currentIdx;
//                }
//                if (data[ currentIdx ].getValue().isNaN()) {
//                    amountNaNsInSequence++;
//                    if (amountNaNsInSequence == noDataGapThreshold) {
//                        TimeseriesValue[] chunk = new TimeseriesValue[i - offset];
//                        System.arraycopy(data, offset, chunk, 0, chunk.length);
//                        dataChunks.add(chunk);
//                    }
//                    if (amountNaNsInSequence > noDataGapThreshold) {
//                        offset++;
//                    }
//                } else {
//                    // end of NaN sequence
//                    amountNaNsInSequence = 0; // reset
//                    i += j + 1; // index of next normal number
//                    break; //
//                }
//            }
//        }
        return generalizeData(data);
    }

    private MeasurementData generalizeData(MeasurementValue[] data) {

        int dataLength = data.length;
        // Bucket size. Leave room for start and end data points
        double bucketSize = ((double) dataLength - 2) / (maxOutputValues - 2);

        int pointIndex = 0;
        MeasurementData sampled = new MeasurementData();
        sampled.addValues(data[pointIndex]);

        for (int bucketIndex = 0; bucketIndex < maxOutputValues - 2; bucketIndex++) {

            // get the range for this bucket
            int rangeOff = (int) Math.floor((bucketIndex + 0) * bucketSize) + 1;
            int rangeTo = (int) Math.floor((bucketIndex + 1) * bucketSize) + 1;

            // first point of triangle
            MeasurementValue triangleLeft = data[pointIndex];
            if (triangleLeft.getValue().isNaN()) {
                addNodataValue(sampled, triangleLeft.getTimestamp());
                pointIndex = rangeTo - 1;
                continue;
            }

            // last point of triangle (next bucket's average)
            BucketAverage triangleRight = calculateAverageOfBucket(bucketIndex + 1, bucketSize, data);

            // init fallback value
            BucketAverage avgCurrentBucket = calculateAverageOfBucket(bucketIndex, bucketSize, data);
            long fallBackTimestamp = avgCurrentBucket.toTimeseriesValue().getTimestamp();
            MeasurementValue maxAreaPoint = new MeasurementValue(fallBackTimestamp, Double.NaN);

            double area;
            int amountOfNodataValues = 0;
            double maxArea = area = -1;
            int nextPointIndex = 0;

            for (; rangeOff < rangeTo; rangeOff++) {

//                if (triangleRight.isNoDataBucket()) {
//                    triangleRight = // TODO
//                }
                // calculate triangle area over three buckets
                final MeasurementValue triangleMiddle = data[rangeOff];

                if (triangleMiddle.getValue().isNaN()) {
                    amountOfNodataValues++;
                    if (isExceededGapThreshold(amountOfNodataValues, bucketSize)) {
                        if (triangleMiddle.getValue().isNaN()) {
                            maxAreaPoint = avgCurrentBucket.toTimeseriesValue();
                            maxAreaPoint.setValue(Double.NaN);
                            LOGGER.debug("No data value for bucket {}.", bucketIndex);
                            pointIndex = rangeTo - 1;
                            break;
                        }
                    }
                } else {
                    area = calcTriangleArea(triangleLeft, triangleRight, triangleMiddle);
                    if (area > maxArea) {
                        maxArea = area;
                        maxAreaPoint = triangleMiddle;
                        nextPointIndex = rangeOff;
                    }
                }
            }

            sampled.addValues(maxAreaPoint); // Pick this point from the Bucket
            pointIndex = nextPointIndex; // This a is the next a
        }

        sampled.addValues(data[dataLength - 1]); // Always add last value
        return sampled;
    }

    private boolean isExceededGapThreshold(int amountOfNodataValues, double bucketSize) {
        return noDataGapThreshold <= 1
                ? amountOfNodataValues > noDataGapThreshold * bucketSize // max percent
                : amountOfNodataValues > noDataGapThreshold; // max absolute
    }

    private void addNodataValue(MeasurementData sampled, long timestamp) {
        sampled.addValues(new MeasurementValue(timestamp, Double.NaN));
    }

    private static double calcTriangleArea(MeasurementValue left, BucketAverage right, MeasurementValue middle) {
        Double middleValue = middle.getValue();
        final Double leftValue = left.getValue();
        final Double rightValue = right.value;
        return Math.abs((left.getTimestamp() - right.timestamp)
                * (middleValue - leftValue)
                - (left.getTimestamp() - middle.getTimestamp())
                * (rightValue - leftValue)) * 0.5;
    }

    private BucketAverage calculateAverageOfBucket(int bucketIndex, double bucketSize, MeasurementValue[] data) {

        int dataLength = data.length;

        int avgRangeStart = (int) Math.floor((bucketIndex + 0) * bucketSize) + 1;
        int avgRangeEnd = (int) Math.floor((bucketIndex + 1) * bucketSize) + 1;
        avgRangeEnd = avgRangeEnd < dataLength ? avgRangeEnd : dataLength;
        double avgRangeLength = avgRangeEnd - avgRangeStart;

        Double avgValue = 0d;
        Double avgTimestamp = 0d;
        int amountOfNodataValues = 0;
        boolean noDataThresholdExceeded = false;
        for (; avgRangeStart < avgRangeEnd; avgRangeStart++) {
            final MeasurementValue current = data[avgRangeStart];
            avgTimestamp += current.getTimestamp();
            if (noDataThresholdExceeded) {
                continue; // keep on calc avg timestamp
            }
            if (current.getValue().isNaN()) {
                amountOfNodataValues++;
                if (amountOfNodataValues == noDataGapThreshold) {
                    avgValue = Double.NaN;
                    noDataThresholdExceeded = true;
                }
            } else {
                avgValue += current.getValue();
            }
        }

        avgTimestamp /= avgRangeLength;
        avgValue /= avgRangeLength;
        return new BucketAverage(avgTimestamp, avgValue);
    }

    private class BucketAverage {

        private Double timestamp;
        private Double value;

        BucketAverage(Double timestamp, Double value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        boolean isNoDataBucket() {
            return value.isNaN();
        }

        MeasurementValue toTimeseriesValue() {
            return new MeasurementValue(timestamp.longValue(), value);
        }
    }
}
