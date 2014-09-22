/**
 * Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
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

import static java.lang.Integer.parseInt;

import java.util.Properties;

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
public class LargestTriangleThreeBucketsGeneralizer implements Generalizer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DouglasPeuckerGeneralizer.class);

	private TvpDataCollection dataToGeneralize;
	 
	/**
     * Config-key for {@link #reductionRate}.
     */
    private static final String REDUCTION_RATE = "REDUCTION_RATE";
    
    /**
     * estimated reduction rate for this use case, where {@link #reductionRate} = 3 means the time series is
     * reduced to 1/3 of it's size; -1 means there is no proper empirical value
     */
    private int reductionRate = -1; // fallback default
	
	private LargestTriangleThreeBucketsGeneralizer(TvpDataCollection data,
			Properties configuration) {
		this.dataToGeneralize = data;
		configure(configuration);
	}

	private void configure(Properties configuration) {
		try {
            reductionRate = configuration.containsKey(REDUCTION_RATE)
                ? parseInt(configuration.getProperty(REDUCTION_RATE))
                : -1;
        }
        catch (NumberFormatException ne) {
            LOGGER.error("Error while reading properties!  Using fallback defaults.", ne);
            throw new IllegalStateException("Error while reading properties! Using fallback defaults.");
        }
	}

	public static Generalizer createNonConfigGeneralizer(TvpDataCollection data) {
		return new LargestTriangleThreeBucketsGeneralizer(data, new Properties());
	}
	
	@Override
	public TvpDataCollection generalize() throws GeneralizerException {
		TvpDataCollection generalizedDataCollection = new TvpDataCollection();
        for (String timeseriesId : dataToGeneralize.getAllTimeseries().keySet()) {
            TimeseriesData timeseries = dataToGeneralize.getTimeseries(timeseriesId);
            generalizedDataCollection.addNewTimeseries(timeseriesId, generalize(timeseries));
        }
        return generalizedDataCollection;
	}

	private TimeseriesData generalize(TimeseriesData timeseries) {
		TimeseriesValue[] data = timeseries.getValues();
		
		int dataLength = data.length;
		
		if (dataLength > 200) {
			int threshold = data.length / 10; // TODO define the threshold
			
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
					if(data[avgRangeStart].getValue() != null) {
						avgValue += data[avgRangeStart].getValue();
					}
				}
				
				avgTimestamp /= avgRangeLength;
				avgValue /= avgRangeLength;
				
				// get the range for this bucket
				int rangeOffs = (int) Math.floor((i+0)* every) + 1;
				int rangeTo = (int) Math.floor((i+1)* every) + 1;
				
				// Point a
				double tempTimestamp = data[pointIndex].getTimestamp();
				double tempValue = data[pointIndex].getValue();
				
				double area;
				TimeseriesValue maxAreaPoint = null;
				int nextPointIndex = 0;
				double maxArea = area = -1;
				
				for (; rangeOffs < rangeTo; rangeOffs++) {
					// calculate triangle area over three buckets
					if (data[rangeOffs].getValue() != null) {
						area = Math.abs((tempTimestamp - avgTimestamp) * (data[rangeOffs].getValue() - tempValue) - (tempTimestamp - data[rangeOffs].getTimestamp()) * (avgValue - tempValue)) * 0.5;
						if (area > maxArea){
							maxArea = area;
							maxAreaPoint = data[rangeOffs];
							nextPointIndex = rangeOffs;
						}
					}
				}
				
				sampled.addValues(maxAreaPoint); // Pick this point from the Bucket
				pointIndex = nextPointIndex; // This a is the next a
			}
			
			sampled.addValues(data[dataLength - 1]); // Allways add last value
			return sampled;
		} else {
			return timeseries;
		}
	}

}
