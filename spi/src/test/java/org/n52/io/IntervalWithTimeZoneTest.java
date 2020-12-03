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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.io;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.joda.time.Interval;
import org.junit.Test;

public class IntervalWithTimeZoneTest {

    private static final String VALID_ISO8601_RELATIVE_START = "PT6H/2013-08-13T+01:00";

    private static final String VALID_ISO8601_ABSOLUTE_START = "2013-07-13TZ/2013-08-13T12:00:00+12:00";

    private static final String VALID_ISO8601_DAYLIGHT_SAVING_SWITCH = "2013-10-28T02:00:00+02:00/2013-10-28T02:00:00+01:00";

    @Test
    public void shouldParseToJodaInterval() {
        IntervalWithTimeZone interval = new IntervalWithTimeZone(VALID_ISO8601_RELATIVE_START);
        assertThat(interval.toInterval(), is(equalTo(new Interval(VALID_ISO8601_RELATIVE_START))));
    }

    @Test
    public void shouldRetainTimezoneOfEndTimeRelativeStart() {
        IntervalWithTimeZone interval = new IntervalWithTimeZone(VALID_ISO8601_RELATIVE_START);
//        System.out.println(interval.getTimezone()
    }
}
