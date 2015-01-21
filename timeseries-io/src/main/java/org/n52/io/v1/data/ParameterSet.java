/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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

import org.joda.time.DateTime;
import org.joda.time.Interval;

public abstract class ParameterSet {

    // XXX refactor ParameterSet, DesignedParameterSet, UndesingedParameterSet and QueryMap
    
    /**
     * The timespan of interest (as <a href="http://en.wikipedia.org/wiki/ISO_8601#Time_intervals">ISO8601
     * interval</a> excluding the Period only version).
     */
    private String timespan;
    
    /**
     * If image data shall be encoded in Base64 to be easily embedded in HTML by JS clients.
     */
    private boolean base64;

    /**
     * If timeseries data shall be generalized or not.
     */
    private boolean generalize;
    
    /**
     * If reference values shall be appended to the timeseries data.
     */
    private boolean expanded;
    
    /**
     * A language code to determine the requested locale. "en" is the default.
     */
    private String language = "en";
    
    protected ParameterSet() {
        timespan = createDefaultTimespan();
    }

    private String createDefaultTimespan() {
        DateTime now = new DateTime();
        DateTime lastWeek = now.minusWeeks(1);
        return new Interval(lastWeek, now).toString();
    }
    
    public boolean isGeneralize() {
        return generalize;
    }
    
    public void setGeneralize(boolean generalize) {
        this.generalize = generalize;
    }

    public String getTimespan() {
        return timespan;
    }
    
    public void setTimespan(String timespan) {
        if (timespan == null) {
            this.timespan = createDefaultTimespan();
        }
        else {
            this.timespan = validateTimespan(timespan);
        }
    }

    public boolean isBase64() {
		return base64;
	}

	public void setBase64(boolean base64) {
		this.base64 = base64;
	}

	public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    private String validateTimespan(String timespan) {
        return Interval.parse(timespan).toString();
    }

    public abstract String[] getTimeseries();

}
