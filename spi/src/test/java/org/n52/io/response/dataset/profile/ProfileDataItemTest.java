package org.n52.io.response.dataset.profile;

import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsNull;
import org.junit.Test;

public class ProfileDataItemTest {

    @Test
    public void getVerticalFrom_when_verticalFromIsNull() {
        ProfileDataItem<Object> value = new ProfileDataItem<Object>(new BigDecimal(1L), null);
        MatcherAssert.assertThat("verticalFrom is not null", value.getVerticalFrom(), IsNull.nullValue());
    }

    @Test
    public void getVerticalFrom_when_verticalFromIsNotNull() {
        ProfileDataItem<Object> value = new ProfileDataItem<Object>(new BigDecimal(1L), new BigDecimal(2L), null);
        MatcherAssert.assertThat("verticalFrom is null", value.getVerticalFrom(), IsNull.notNullValue());
        MatcherAssert.assertThat("verticalFrom is not of value 1L", value.getVerticalFrom(), is(new BigDecimal(1L)));
    }
    
    @Test
    public void getVerticalTo_when_verticalFromIsNull() {
        ProfileDataItem<Object> value = new ProfileDataItem<Object>(new BigDecimal(1L), null);
        MatcherAssert.assertThat("verticalTo is null", value.getVerticalTo(), IsNull.nullValue());
    }
    
    @Test
    public void getVerticalTo_when_verticalFromIsNotNull() {
        ProfileDataItem<Object> value = new ProfileDataItem<Object>(new BigDecimal(1L), new BigDecimal(2L), null);
        MatcherAssert.assertThat("verticalTo is null", value.getVerticalTo(), IsNull.notNullValue());
        MatcherAssert.assertThat("verticalTo is not of value 2L", value.getVerticalTo(), is(new BigDecimal(2L)));
    }

    @Test
    public void getVertical_when_verticalFromIsNull() {
        ProfileDataItem<Object> value = new ProfileDataItem<Object>(new BigDecimal(1L), null);
        MatcherAssert.assertThat("vertical is null", value.getVertical(), IsNull.notNullValue());
        MatcherAssert.assertThat("vertical is not of value 1L", value.getVertical(), is(new BigDecimal(1L)));
    }
    
    @Test
    public void getVertical_when_verticalFromIsNotNull() {
        ProfileDataItem<Object> value = new ProfileDataItem<Object>(new BigDecimal(1L), new BigDecimal(2L), null);
        MatcherAssert.assertThat("vertical is null", value.getVerticalFrom(), IsNull.notNullValue());
        MatcherAssert.assertThat("vertical is not of value 1L", value.getVerticalFrom(), is(new BigDecimal(1L)));
    }
    
}
