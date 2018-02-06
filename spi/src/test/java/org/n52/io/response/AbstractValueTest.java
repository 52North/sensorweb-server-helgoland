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
