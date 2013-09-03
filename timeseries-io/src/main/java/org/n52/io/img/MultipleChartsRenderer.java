/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.io.img;

import static org.n52.io.img.LineRenderer.createStyledLineRenderer;
import static org.n52.io.style.LineStyle.createLineStyle;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.jfree.data.general.DatasetGroup;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.n52.io.format.TvpDataCollection;
import org.n52.io.style.BarStyle;
import org.n52.io.style.LineStyle;
import org.n52.io.v1.data.FeatureOutput;
import org.n52.io.v1.data.StyleProperties;
import org.n52.io.v1.data.TimeseriesData;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.n52.io.v1.data.TimeseriesValue;

public class MultipleChartsRenderer extends ChartRenderer {

    public MultipleChartsRenderer(RenderingContext context, String locale) {
        super(context, locale);
    }

    @Override
    public void generateOutput(TvpDataCollection data) {
        Map<String, TimeseriesData> allTimeseries = data.getAllTimeseries();
        TimeseriesMetadataOutput[] timeseriesMetadatas = getTimeseriesMetadataOutputs();
        for (int rendererIndex = 0; rendererIndex < timeseriesMetadatas.length; rendererIndex++) {
            
            /*
             * For each index put data and a corresponding renderer specific to a style
             */
            
            TimeseriesMetadataOutput timeseriesMetadata = timeseriesMetadatas[rendererIndex];
            TimeseriesData timeseriesData = allTimeseries.get(timeseriesMetadata.getId());
            TimeseriesData sortedTimeseriesData = sortTimeseriesData(timeseriesData);
            putRendererAtIndex(rendererIndex, sortedTimeseriesData, timeseriesMetadata);
            configureRangeAxis(timeseriesMetadata, rendererIndex);
        }
    }

    private String createTimeseriesLabel(TimeseriesMetadataOutput metadata) {
        FeatureOutput feature = metadata.getParameters().getFeature();
        StringBuilder timeseriesLabel = new StringBuilder();
        timeseriesLabel.append(feature.getLabel());
        timeseriesLabel.append(" (").append(metadata.getId()).append(")");
        return timeseriesLabel.toString();
    }

    private void putRendererAtIndex(int rendererIndex, TimeseriesData timeseriesData, TimeseriesMetadataOutput timeMetadata) {

        /*
         * As each timeseries may define its custom styling and different chart types we have to loop over all
         * styles and process chart rendering iteratively
         */
    	String timeseriesId = timeMetadata.getId();
    	String timeseriesLabel = createTimeseriesLabel(timeMetadata);
        StyleProperties properties = getTimeseriesStyleFor(timeseriesId);
        TimeSeriesCollection timeseriesCollection = createTimeseriesCollection(timeseriesLabel, timeseriesData, timeseriesId);
        getXYPlot().setDataset(rendererIndex, timeseriesCollection);
        if (isLineStyle(properties)) {
            // do line chart rendering
            LineStyle lineStyle = createLineStyle(properties);
            LineRenderer lineRenderer = createStyledLineRenderer(lineStyle);
            getXYPlot().setRenderer(rendererIndex, lineRenderer.getXYRenderer());
            lineRenderer.setColorForSeriesAt(rendererIndex);
        }
        else if (isBarStyle(properties)) {
        	BarStyle barStyle = BarStyle.createFrom(properties);
            BarRenderer barRenderer = BarRenderer.createBarRenderer(barStyle);
        	getXYPlot().setRenderer(rendererIndex, barRenderer.getXYRenderer());
        	barRenderer.setColorForSeriesAt(rendererIndex);
        }
    }

    private TimeSeriesCollection createTimeseriesCollection(String dataId, TimeseriesData sortedData, String timeseriesID) {
        TimeSeriesCollection timeseriesCollection = new TimeSeriesCollection();
        timeseriesCollection.addSeries(createPerDayTimeseriesToRender(dataId, sortedData, timeseriesID));
        timeseriesCollection.setGroup(new DatasetGroup(dataId));
        return timeseriesCollection;
    }

    private TimeSeries createPerDayTimeseriesToRender(String dataId, TimeseriesData timeseriesData, String timeseriesID) {
        TimeSeries timeseries = new TimeSeries(dataId);
        double intervalSum = 0.0;
        if (timeseriesData.getValues().length > 0) {
        	RegularTimePeriod timeinterval = createTimePeriod(new Date(timeseriesData.getValues()[0].getTimestamp()), timeseriesID); // TODO empty array
            for (TimeseriesValue value : timeseriesData.getValues()) {
            	Date timestamp = new Date(value.getTimestamp());
            	if (timeinterval.getEnd().getTime() > timestamp.getTime()) {
            		intervalSum = intervalSum + value.getValue();
            	} else {
            		timeseries.add(timeinterval, intervalSum);
            		timeinterval = createTimePeriod(new Date(value.getTimestamp()), timeseriesID);
            		intervalSum = value.getValue();
            	}
            }
        }
        return timeseries;
    }

	private RegularTimePeriod createTimePeriod(Date date, String timeseriesId) {
		StyleProperties styleProperties = getTimeseriesStyleFor(timeseriesId);
		if (styleProperties.getProperties().containsKey("interval")) {
			String interval = styleProperties.getProperties().get("interval");
			if (interval.equals("byHour")) {
				return new Hour(date);
			} else if (interval.equals("byDay")) {
				return new Day(date);
			} else if (interval.equals("byWeek")) {
				return new Week(date);
			} else if (interval.equals("byMonth")) {
				return new Month(date);
			}
		}
		return new Second(date);
	}

	private TimeseriesData sortTimeseriesData(TimeseriesData timeseriesData) {
        Arrays.sort(timeseriesData.getValues());
        return timeseriesData;
    }

}
