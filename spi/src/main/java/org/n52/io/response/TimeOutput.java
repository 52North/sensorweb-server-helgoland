/*
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.io.response;

import org.joda.time.DateTime;

public class TimeOutput implements Comparable<TimeOutput> {

    private DateTime dateTime;

    private boolean unixTime;

    public TimeOutput(DateTime time, boolean unixTime) {
        this.dateTime = time;
        this.unixTime = unixTime;
    }

    public TimeOutput(DateTime time) {
        this(time, false);
    }

    public TimeOutput(Long time, boolean unixTime) {
        this(time != null ? new DateTime(time) : null, false);
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public Long getMillis() {
        return getDateTime() != null ? getDateTime().getMillis() : null;
    }

    public TimeOutput setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public boolean isUnixTime() {
        return unixTime;
    }

    public TimeOutput setUnixTime(boolean unixTime) {
        this.unixTime = unixTime;
        return this;
    }

    @Override
    public int hashCode() {
        return getDateTime() != null ? getDateTime().hashCode() : 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof TimeOutput) {
            return getDateTime().equals(((TimeOutput) obj).getDateTime());
        }
        return false;
    }

    @Override
    public int compareTo(TimeOutput o) {
        return getDateTime().compareTo(o.getDateTime());
    }

}
