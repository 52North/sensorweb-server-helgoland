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





public class TimeseriesMetadataOutput {

    private String id;

    private String uom;
    
    private StationOutput station;
    
//    private Map<String, TimeseriesValue> refValues;
    
    private String[] referenceValues;

    private TimeseriesValue firstValue;

    private TimeseriesValue lastValue;

    private TimeseriesOutput parameters;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public StationOutput getStation() {
        return station;
    }

    public void setStation(StationOutput station) {
        this.station = station;
    }

    public String[] getRefValues() {
        return referenceValues;
    }

    public void setRefValues(String[] referenceValues) {
        this.referenceValues = referenceValues;
    }

    public TimeseriesValue getFirstValue() {
        return firstValue;
    }

    public void setFirstValue(TimeseriesValue firstValue) {
        this.firstValue = firstValue;
    }

    public TimeseriesValue getLastValue() {
        return lastValue;
    }

    public void setLastValue(TimeseriesValue lastValue) {
        this.lastValue = lastValue;
    }

    public TimeseriesOutput getParameters() {
        return parameters;
    }

    public void setParameters(TimeseriesOutput timeseries) {
        this.parameters = timeseries;
    }
    
}
