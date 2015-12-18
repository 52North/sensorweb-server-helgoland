package org.n52.series.ckan.beans;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.series.ckan.da.CkanConstants;

public class ResourceMemberTest {
    
    private static final String SCHEMA_DESCRIPTOR = "/files/dwd/temperature-dwd/schema_descriptor.json";
    
    private SchemaDescriptor descriptor;
    
    @Before
    public void setUp() throws IOException {
        ObjectMapper om = new ObjectMapper();
        final JsonNode node = om.readTree(getClass().getResource(SCHEMA_DESCRIPTOR));
        descriptor = new SchemaDescriptor(new CkanDataset(), node);
        Assert.assertThat(descriptor.getSchemaDescriptionType(), Matchers.is(CkanConstants.ResourceType.CSV_OBSERVATIONS_COLLECTION));
    }
    
    @Test
    public void findJoinableFields() {
        List<ResourceMember> members = descriptor.getMembers();
        ResourceMember platformDescription = members.get(0);
        ResourceMember observationDescription = members.get(1);
        Set<ResourceField> joinableFields = platformDescription.getJoinableFields(observationDescription);
        MatcherAssert.assertThat(joinableFields.size(), CoreMatchers.is(6));
    }
}
