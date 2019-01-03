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
package org.n52.io.response;

import static org.hamcrest.Matchers.is;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.n52.io.response.dataset.AbstractValue;

public class AbstractValueTest {

    @Test
    public void getTimestart_when_timestartIsNull() {
        AbstractValue<Object> value = new AbstractValue<Object>(1L, null) {};
        MatcherAssert.assertThat("timestart is not null", value.getTimestart(), IsNull.nullValue());
    }

    @Test
    public void getTimestart_when_timestartIsNotNull() {
        AbstractValue<Object> value = new AbstractValue<Object>(1L, 2L, null) {};
        MatcherAssert.assertThat("timestart is null", value.getTimestart(), IsNull.notNullValue());
        MatcherAssert.assertThat("timestart is not of value 1L", value.getTimestart(), is(1L));
    }

    @Test
    public void getTimeend_when_timestartIsNull() {
        AbstractValue<Object> value = new AbstractValue<Object>(1L, null) {};
        MatcherAssert.assertThat("timeend is null", value.getTimeend(), IsNull.nullValue());
    }

    @Test
    public void getTimeend_when_timestartIsNotNull() {
        AbstractValue<Object> value = new AbstractValue<Object>(1L, 2L, null) {};
        MatcherAssert.assertThat("timeend is null", value.getTimeend(), IsNull.notNullValue());
        MatcherAssert.assertThat("timeend is not of value 2L", value.getTimeend(), is(2L));
    }

    @Test
    public void getTimestamp_when_timestartIsNull() {
        AbstractValue<Object> value = new AbstractValue<Object>(1L, null) {};
        MatcherAssert.assertThat("timestamp is null", value.getTimestamp(), IsNull.notNullValue());
        MatcherAssert.assertThat("timestamp is not of value 1L", value.getTimestamp(), is(1L));
    }

    @Test
    public void getTimestamp_when_timestartIsNotNull() {
        AbstractValue<Object> value = new AbstractValue<Object>(1L, 2L, null) {};
        MatcherAssert.assertThat("timestart is null", value.getTimestart(), IsNull.notNullValue());
        MatcherAssert.assertThat("timestart is not of value 1L", value.getTimestart(), is(1L));
    }

}
