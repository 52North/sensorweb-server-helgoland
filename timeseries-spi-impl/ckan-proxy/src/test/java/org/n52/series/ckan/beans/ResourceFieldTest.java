package org.n52.series.ckan.beans;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

public class ResourceFieldTest {
    
    @Test
    public void testEqualityUsingId() {
        ResourceField first = new ResourceField("test42");
        MatcherAssert.assertThat(first.equals(new ResourceField("test42")), CoreMatchers.is(true));
    }
    
    @Test
    public void testEqualityIgnoringCase() {
        ResourceField first = new ResourceField("test42");
        MatcherAssert.assertThat(first.equals(new ResourceField("Test42")), CoreMatchers.is(true));
    }
}
