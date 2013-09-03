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

package org.n52.io.v1.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TimeseriesData {

    private List<TimeseriesValue> values = new ArrayList<TimeseriesValue>();
    
    private TimeseriesMetadata metadata;
    
    public void addValues(TimeseriesValue... values) {
        if (values != null) {
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
        for (Long timestamp : values.keySet()) {
            Double value = values.get(timestamp);
            timeseries.addNewValue(timestamp, value);
        }
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
    
    @JsonProperty("_metadata")
    public TimeseriesMetadata getMetadata() {
        return metadata;
    }
    
    public void setMetadata(TimeseriesMetadata metadata) {
        this.metadata = metadata;
    }
    
}
