/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

public class OptionalOutputTest {

    @Test
    public void when_created_then_defaultIsToSerialize() {
        OptionalOutput<String> optional = OptionalOutput.of("42");
        MatcherAssert.assertThat(optional.isSerialize(), Matchers.is(true));
    }

    @Test
    public void when_createdWithFalseSerialization_then_valueIsNotToSerialize() {
        OptionalOutput<String> optional = OptionalOutput.of("42", false);
        MatcherAssert.assertThat(optional.isSerialize(), Matchers.is(false));
    }

    @Test
    public void when_createdWithValue_then_valueIsPresent() {
        OptionalOutput<String> optional = OptionalOutput.of("42");
        MatcherAssert.assertThat(optional.isPresent(), Matchers.is(true));
    }

    @Test
    public void when_createdWithFalseSerialization_then_valueIsPresent() {
        OptionalOutput<String> optional = OptionalOutput.of("42", false);
        MatcherAssert.assertThat(optional.isPresent(), Matchers.is(true));
    }

    @Test
    public void when_createdWithNull_then_valueIsAbsent() {
        OptionalOutput<String> optional = OptionalOutput.of(null);
        MatcherAssert.assertThat(optional.isAbsent(), Matchers.is(true));
    }

    @Test
    public void when_created_then_valueIsReturned() {
        OptionalOutput<String> optional = OptionalOutput.of("42");
        MatcherAssert.assertThat(optional.getValue(), Matchers.is("42"));
    }

    @Test
    public void when_createdWithFalseSerialization_then_valueIsNotReturned() {
        OptionalOutput<String> optional = OptionalOutput.of("42", false);
        MatcherAssert.assertThat(optional.getValue(), Matchers.is(CoreMatchers.nullValue()));
    }

}
