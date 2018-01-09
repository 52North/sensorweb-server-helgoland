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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

public class ParameterOutputTest {

    private <T> T resolve(T value, Function<OptionalOutput<T>, T> resolver) {
        return resolve(value, resolver, true);
    }

    private <T> T resolve(T value, Function<OptionalOutput<T>, T> resolver, boolean serialize) {
        return resolve(OptionalOutput.of(value, serialize), resolver);
    }

    private <T> T resolve(OptionalOutput<T> optional, Function<OptionalOutput<T>, T> resolver) {
        return resolver.apply(optional);
    }

    @Test
    public void when_nullCollection_then_serializedCollectionIsNull() {
        ParameterOutput output = new ParameterOutput() {};
        Collection<Object> actual = resolve((Collection<Object>)null, output::getIfSerializedCollection);
        MatcherAssert.assertThat(actual, Matchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void when_nullMap_then_serializedMapIsNull() {
        ParameterOutput output = new ParameterOutput() {};
        Map<Object, Object> actual = resolve((Map<Object, Object>)null, output::getIfSerializedMap);
        MatcherAssert.assertThat(actual, Matchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void when_emptyCollection_then_serializedCollectionIsNull() {
        ParameterOutput output = new ParameterOutput() {};
        Collection< ? > actual = resolve(Collections.emptyList(), output::getIfSerializedCollection);
        MatcherAssert.assertThat(actual, Matchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void when_emptyMap_then_serializedMapIsNull() {
        ParameterOutput output = new ParameterOutput() {};
        Map< ?, ? > actual = resolve(Collections.emptyMap(), output::getIfSerializedMap);
        MatcherAssert.assertThat(actual, Matchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void when_emptyNonSerializationCollection_then_serializedCollectionIsNull() {
        ParameterOutput output = new ParameterOutput() {};
        Collection< ? > actual = resolve(Collections.emptyList(), output::getIfSerializedCollection);
        MatcherAssert.assertThat(actual, Matchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void when_emptyNonSerializationMap_then_serializedMapIsNull() {
        ParameterOutput output = new ParameterOutput() {};
        Map< ?, ? > actual = resolve(Collections.emptyMap(), output::getIfSerializedMap);
        MatcherAssert.assertThat(actual, Matchers.is(CoreMatchers.nullValue()));
    }

    @Test
    public void when_nonEmptyNonSerializationCollection_then_serializedCollection() {
        ParameterOutput output = new ParameterOutput() {};
        Collection< ? > actual = resolve(Collections.singleton("foo"), output::getIfSerializedCollection);
        MatcherAssert.assertThat(actual, Matchers.is(Matchers.not(Matchers.empty())));
    }

    @Test
    public void when_nonEmptyNonSerializationMap_then_serializedMap() {
        ParameterOutput output = new ParameterOutput() {};
        Map<String, String> actual = resolve(Collections.singletonMap("foo", "bar"), output::getIfSerializedMap);
        MatcherAssert.assertThat(actual.keySet(), Matchers.is(Matchers.not(Matchers.empty())));
    }

    @Test
    public void when_nonEmptyNonSerializationCollection_then_serializedCollectionIsNull() {
        ParameterOutput output = new ParameterOutput() {};
        Collection< ? > actual = resolve(Collections.singleton("foo"), output::getIfSerializedCollection, false);
        MatcherAssert.assertThat(actual, Matchers.is(CoreMatchers.nullValue()));
    }


    @Test
    public void when_nonEmptyNonSerializationMap_then_serializedMapIsNull() {
        ParameterOutput output = new ParameterOutput() {};
        Map< ?, ? > actual = resolve(Collections.singletonMap("foo", "bar"), output::getIfSerializedMap, false);
        MatcherAssert.assertThat(actual, Matchers.is(CoreMatchers.nullValue()));
    }
}
