package org.n52.series.ckan.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.n52.series.ckan.cache.InMemoryCkanMetadataCache;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

public class InMemoryCkanMetadataCacheTest {
    
    private InMemoryCkanMetadataCache ckanCache;
    
    @Before
    public void setUp() {
        ckanCache = new InMemoryCkanMetadataCache();
    }
    
    @Test
    public void shouldInstantiateEmpty() {
        final Iterable<String> ids = ckanCache.getDatasetIds();
        MatcherAssert.assertThat(ids.iterator().hasNext(), CoreMatchers.is(false));
    }
    
    @Test
    public void shouldReturnResourceDescription() {
        CkanDataset dataset = new CkanDataset("test-dataset");
        
        CkanResource normalResource = createRandomCkanResource(dataset);
        CkanResource resourceDescription = createRandomCkanResource(dataset);
        resourceDescription.setName("Resource Description");
        resourceDescription.setFormat("json");
        
        List<CkanResource> resources = new ArrayList<>();
        resources.add(normalResource);
        resources.add(resourceDescription);
        dataset.setMetadataModified(Timestamp.valueOf(LocalDateTime.now()));
        dataset.setResources(resources);
        
        ckanCache.insertOrUpdate(dataset);
        final JsonNode actual = ckanCache.getSchemaDescription(dataset.getId());
        String actualId = actual.get("id").asText();
        MatcherAssert.assertThat(actualId, CoreMatchers.is(resourceDescription.getId()));
    }
    
    private CkanResource createRandomCkanResource(CkanDataset dataset) {
        String id = UUID.randomUUID().toString();
        String url = "https://nonsense.eu/" + id;
        CkanResource normalResource = new CkanResource(url, dataset.getId());
        normalResource.setId(id);
        return normalResource;
    }
}
