/**
 * ﻿Copyright (C) 2013
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
package org.n52.io.v1.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TimeseriesData implements Serializable {

    private static final long serialVersionUID = 4717558247670336015L;

    private List<TimeseriesValue> values = new ArrayList<TimeseriesValue>();
    
    private TimeseriesMetadata metadata;
    
    public void addValues(TimeseriesValue... values) {
        if (values != null && values.length > 0) {
            this.values.addAll(Arrays.asList(values));
        }
    }

    /**
     * @param values
     *        the timestamp &lt;-&gt; value map.
     * @return a timeseries object.
     */
    public static TimeseriesData newTimeseriesData(Map<Long, Double> values) {
        TimeseriesData timeseries = new TimeseriesData();
        for (Entry<Long, Double> data : values.entrySet()) {
            timeseries.addNewValue(data.getKey(), data.getValue());
        }
        return timeseries;
    }
    
    public static TimeseriesData newTimeseriesData(TimeseriesValue... values) {
        TimeseriesData timeseries = new TimeseriesData();
        timeseries.addValues(values);
        return timeseries;
    }

    private void addNewValue(Long timestamp, Double value) {
        values.add(new TimeseriesValue(timestamp, value));
    }
    
    /**
     * @return a sorted list of timeseries values.
     */
    public TimeseriesValue[] getValues() {
        Collections.sort(values);
        return values.toArray(new TimeseriesValue[0]);
    }

    void setValues(TimeseriesValue[] values) {
        this.values = Arrays.asList(values);
    }
    
    @JsonProperty("extra")
    public TimeseriesMetadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(TimeseriesMetadata metadata) {
        this.metadata = metadata;
    }
    
    @JsonIgnore
    public boolean hasReferenceValues() {
        return metadata != null 
                && metadata.getReferenceValues() != null
                && !metadata.getReferenceValues().isEmpty();
    }
    
}
