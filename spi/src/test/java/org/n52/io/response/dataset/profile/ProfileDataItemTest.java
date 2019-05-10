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
package org.n52.io.response.dataset.profile;

import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsNull;
import org.junit.Test;

public class ProfileDataItemTest {

    @Test
    public void getVerticalFrom_when_verticalFromIsNull() {
        ProfileDataItem<Object> value = new ProfileDataItem<Object>(BigDecimal.valueOf(1L), null);
        MatcherAssert.assertThat("verticalFrom is not null", value.getVerticalFrom(), IsNull.nullValue());
    }

    @Test
    public void getVerticalFrom_when_verticalFromIsNotNull() {
        ProfileDataItem<Object> value = new ProfileDataItem<Object>(BigDecimal.valueOf(1L), BigDecimal.valueOf(2L), null);
        MatcherAssert.assertThat("verticalFrom is null", value.getVerticalFrom(), IsNull.notNullValue());
        MatcherAssert.assertThat("verticalFrom is not of value 1L", value.getVerticalFrom(), is(BigDecimal.valueOf(1L)));
    }

    @Test
    public void getVerticalTo_when_verticalFromIsNull() {
        ProfileDataItem<Object> value = new ProfileDataItem<Object>(BigDecimal.valueOf(1L), null);
        MatcherAssert.assertThat("verticalTo is null", value.getVerticalTo(), IsNull.nullValue());
    }

    @Test
    public void getVerticalTo_when_verticalFromIsNotNull() {
        ProfileDataItem<Object> value = new ProfileDataItem<Object>(BigDecimal.valueOf(1L), BigDecimal.valueOf(2L), null);
        MatcherAssert.assertThat("verticalTo is null", value.getVerticalTo(), IsNull.notNullValue());
        MatcherAssert.assertThat("verticalTo is not of value 2L", value.getVerticalTo(), is(BigDecimal.valueOf(2L)));
    }

    @Test
    public void getVertical_when_verticalFromIsNull() {
        ProfileDataItem<Object> value = new ProfileDataItem<Object>(BigDecimal.valueOf(1L), null);
        MatcherAssert.assertThat("vertical is null", value.getVertical(), IsNull.notNullValue());
        MatcherAssert.assertThat("vertical is not of value 1L", value.getVertical(), is(BigDecimal.valueOf(1L)));
    }

    @Test
    public void getVertical_when_verticalFromIsNotNull() {
        ProfileDataItem<Object> value = new ProfileDataItem<Object>(BigDecimal.valueOf(1L), BigDecimal.valueOf(2L), null);
        MatcherAssert.assertThat("vertical is null", value.getVerticalFrom(), IsNull.notNullValue());
        MatcherAssert.assertThat("vertical is not of value 1L", value.getVerticalFrom(), is(BigDecimal.valueOf(1L)));
    }

}
