/**
 * ﻿Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.io.v1.data;

import org.joda.time.Interval;



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
        return timeseriesIds.clone();
    }

    void setTimeseries(String[] timeseries) {
        this.timeseriesIds = timeseries.clone();
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public static UndesignedParameterSet createForSingleTimeseries(String timeseriesId, Interval timespan) {
        UndesignedParameterSet parameters = new UndesignedParameterSet();
        parameters.setTimeseries(new String[] { timeseriesId });
        parameters.setTimespan(timespan.toString());
        return parameters;
    }

    public static UndesignedParameterSet createFromDesignedParameters(DesignedParameterSet designedSet) {
        UndesignedParameterSet parameters = new UndesignedParameterSet();
        parameters.setTimeseries(designedSet.getTimeseries());
        parameters.setTimespan(designedSet.getTimespan());
        return parameters;
    }
}
