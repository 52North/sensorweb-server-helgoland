package org.n52.series.ckan.table;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.n52.series.ckan.beans.ResourceField;

public class JoinIndexTest {
    
    @Test
    public void testEqualityUsingId() {
        final ResourceField first = new ResourceFieldSeam("test42");
        final ResourceFieldSeam second = new ResourceFieldSeam("test42");
        JoinIndex index1 = new JoinIndex(first, "42");
        JoinIndex index2 = new JoinIndex(second, "42");
        
        MatcherAssert.assertThat(index1.equals(index2), CoreMatchers.is(true));
    }
    
    @Test
    public void testEqualityIgnoringCase() {
        final ResourceField first = new ResourceFieldSeam("test42");
        final ResourceFieldSeam second = new ResourceFieldSeam("Test42");
        
        JoinIndex index1 = new JoinIndex(first, "42");
        JoinIndex index2 = new JoinIndex(second, "42");
        
        MatcherAssert.assertThat(index1.equals(index2), CoreMatchers.is(true));
    }
    
    
    @Test
    public void shouldFailTestingEqualityWithDifferentValue() {
        final ResourceField first = new ResourceFieldSeam("test42");
        final ResourceFieldSeam second = new ResourceFieldSeam("Test42");
        
        JoinIndex index1 = new JoinIndex(first, "42");
        JoinIndex index2 = new JoinIndex(second, "10");
        
        MatcherAssert.assertThat(index1.equals(index2), CoreMatchers.is(false));
    }
    
    private static class ResourceFieldSeam extends ResourceField {

        public ResourceFieldSeam(String fieldId) {
            super(fieldId);
        }
    }
}
