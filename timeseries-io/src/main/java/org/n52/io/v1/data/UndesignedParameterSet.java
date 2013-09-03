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



public class UndesignedParameterSet extends ParameterSet {

    // XXX refactor ParameterSet, DesignedParameterSet, UndesingedParameterSet and QueryMap
    
    /**
     * The timeseriesIds of interest.
     */
    private String[] timeseriesIds;
    
    /**
     * Which output format the raw data shall have.
     */
    private String format;

    @Override
    public String[] getTimeseries() {
        return timeseriesIds;
    }

    void setTimeseries(String[] timeseries) {
        this.timeseriesIds = timeseries;
    }
    
    public String getFormat() {
        return format;
    }
    
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public static UndesignedParameterSet createForSingleTimeseries(String timeseriesId, String timespan) {
        UndesignedParameterSet parameters = new UndesignedParameterSet();
        parameters.setTimeseries(new String[] { timeseriesId });
        parameters.setTimespan(timespan);
        return parameters;
    }

    public static UndesignedParameterSet createFromDesignedParameters(DesignedParameterSet designedSet) {
        UndesignedParameterSet parameters = new UndesignedParameterSet();
        parameters.setTimeseries(designedSet.getTimeseries());
        parameters.setTimespan(designedSet.getTimespan());
        return parameters;
    }
}
