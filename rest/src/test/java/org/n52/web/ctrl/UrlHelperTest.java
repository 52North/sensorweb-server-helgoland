package org.n52.web.ctrl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

public class UrlHelperTest {
    
    @Test
    public void when_constructBackwardsCompatibleUrl_then_relativeLink() {
        String actual = new UrlHelper().constructHref(null, "/v1/procedures");
        assertThat(actual, is("./procedures"));
    }
    
    @Test
    public void when_constructUrlWithNonTrailingSlashBaseUrl_then_fullLink() {
        String actual = new UrlHelper().constructHref("http://localhost:8080/foo/bar/v1", "/v1/procedures");
        assertThat(actual, is("http://localhost:8080/foo/bar/v1/procedures"));
    }
    
    @Test
    public void when_constructUrlFromVersionLessBaseUrl_then_fullLink() {
        String actual = new UrlHelper().constructHref("http://localhost:8080/foo/bar/", "/v1/procedures");
        assertThat(actual, is("http://localhost:8080/foo/bar/v1/procedures"));
    }
    
    @Test
    public void when_constructUrlFromBaseUrl_then_fullLink() {
        String actual = new UrlHelper().constructHref("http://localhost:8080/foo/bar/v1/", "/v1/procedures");
        assertThat(actual, is("http://localhost:8080/foo/bar/v1/procedures"));
    }
    
    @Test
    public void when_constructUrlFromDeeperBaseUrl_then_fullLink() {
        String actual = new UrlHelper().constructHref("http://localhost:8080/foo/bar/v1/somewhere", "/v1/procedures");
        assertThat(actual, is("http://localhost:8080/foo/bar/v1/procedures"));
    }

    
}
