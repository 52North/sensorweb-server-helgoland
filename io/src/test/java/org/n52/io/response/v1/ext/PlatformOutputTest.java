package org.n52.io.response.v1.ext;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class PlatformOutputTest {

    @Test
    public void when_createdMobileRemote_then_hrefIncludesPrefix() {
        PlatformOutput platform = new PlatformOutput(PlatformType.MOBILE_REMOTE);
        platform.setHrefBase("http://localhost/context");
        platform.setId("12");

        assertThat(platform.getHref(), is("http://localhost/context/mobile/remote/12"));
    }

    @Test
    public void when_createdStationaryRemote_then_hrefIncludesPrefix() {
        PlatformOutput platform = new PlatformOutput(PlatformType.STATIONARY_REMOTE);
        platform.setHrefBase("http://localhost/context");
        platform.setId("12");

        assertThat(platform.getHref(), is("http://localhost/context/stationary/remote/12"));
    }

    @Test
    public void when_createdStationaryInsitu_then_hrefIncludesPrefix() {
        PlatformOutput platform = new PlatformOutput(PlatformType.STATIONARY_INSITU);
        platform.setHrefBase("http://localhost/context");
        platform.setId("12");

        assertThat(platform.getHref(), is("http://localhost/context/stationary/insitu/12"));
    }

    @Test
    public void when_createdMobileInsitu_then_hrefIncludesPrefix() {
        PlatformOutput platform = new PlatformOutput(PlatformType.MOBILE_INSITU);
        platform.setHrefBase("http://localhost/context");
        platform.setId("12");

        assertThat(platform.getHref(), is("http://localhost/context/mobile/insitu/12"));
    }

    @Test
    public void when_havingExplicitHref_then_hrefNotIncludingHrefBase() {
        PlatformOutput platform = new PlatformOutput(PlatformType.MOBILE_INSITU);
        platform.setHref("http://localhost/otherContext/12");
        platform.setHrefBase("http://localhost/context");
        platform.setId("12");

        assertThat(platform.getHref(), is("http://localhost/otherContext/12"));
    }
}
