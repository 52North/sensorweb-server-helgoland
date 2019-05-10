/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.io;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

/**
 * <p>
 * Wraps a JodaTime Interval to retain timezone information. The API needs the given timezone information as a
 * best guess to respond timeseries data in a proper manner. JodaTime stores time instants (having no timezone
 * per se) along the interval, so timezone information is available once the timespan string has been parsed.
 * </p>
 *
 * @see <a href="http://stackoverflow.com/questions/18404433/joda-interval-losing-timezone-information"></a>
 */
public class IntervalWithTimeZone {

    private String timespan;

    /**
     * @param timespan
     *        the time interval in ISO8601 notation.
     * @throws IllegalArgumentException
     *         if timespan is not a valid interval.
     */
    public IntervalWithTimeZone(String timespan) {
        Interval.parse(timespan);
        this.timespan = timespan;
    }

    public DateTimeZone getTimezone() {
        String endTime = timespan.split("/")[1];
        return DateTime.parse(endTime)
                       .getZone();
    }

    public Interval toInterval() {
        return Interval.parse(timespan);
    }

    @Override
    public String toString() {
        // with retained timezone information
        return timespan;
    }
}
