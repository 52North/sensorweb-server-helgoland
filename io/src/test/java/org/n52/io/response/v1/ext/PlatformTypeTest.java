package org.n52.io.response.v1.ext;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PlatformTypeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void when_mobileInsituString_then_recognizeType() {
        Assert.assertThat(PlatformType.toInstance("mobile/insitu"), Matchers.is(PlatformType.MOBILE_INSITU));
    }

    @Test
    public void when_mobileRemoteString_then_recognizeType() {
        Assert.assertThat(PlatformType.toInstance("mobile/remote"), Matchers.is(PlatformType.MOBILE_REMOTE));
    }

    @Test
    public void when_stationaryInsituString_then_recognizeType() {
        Assert.assertThat(PlatformType.toInstance("stationary/insitu"), Matchers.is(PlatformType.STATIONARY_INSITU));
    }

    @Test
    public void when_stationaryRemoteString_then_recognizeType() {
        Assert.assertThat(PlatformType.toInstance("stationary/remote"), Matchers.is(PlatformType.STATIONARY_REMOTE));
    }

    @Test
    public void when_unknownType_then_throwException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("no type for 'does not exist'.");
        PlatformType.toInstance("does not exist");
    }

    @Test
    public void when_extractingId_then_typePrefixGone() {
        Assert.assertThat(PlatformType.extractId("mobile/insitu/foobar"), Matchers.is("foobar"));
    }

    @Test
    public void when_extractingWithInvalidPrefix_then_expectIdentity() {
        Assert.assertThat(PlatformType.extractId("invalid_prefix"), Matchers.is("invalid_prefix"));
    }

    @Test
    public void when_stationaryOnlyPrefix_then_expectIdentity() {
        Assert.assertThat(PlatformType.extractId("stationary"), Matchers.is("stationary"));
    }


    @Test
    public void when_mobileOnlyPrefix_then_expectIdentity() {
        Assert.assertThat(PlatformType.extractId("mobile"), Matchers.is("mobile"));
    }

    @Test
    public void when_idWithStationaryPrefix_then_detectType() {
        Assert.assertTrue(PlatformType.isStationaryId("stationary/something"));
    }

    @Test
    public void when_idWithMobilePrefix_then_detectType() {
        Assert.assertTrue(PlatformType.isMobileId("mobile/something"));
    }

}
