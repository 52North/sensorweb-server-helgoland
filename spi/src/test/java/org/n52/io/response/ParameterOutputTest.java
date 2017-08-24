
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
