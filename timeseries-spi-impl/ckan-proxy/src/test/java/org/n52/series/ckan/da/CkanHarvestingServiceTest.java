package org.n52.series.ckan.da;

import org.n52.series.ckan.cache.InMemoryCkanMetadataCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.CkanQuery;
import java.io.IOException;
import java.net.URISyntaxException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.n52.series.ckan.util.ResourceClient;

public class CkanHarvesterTest {
    
    private final ObjectMapper om = new ObjectMapper();
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    private InMemoryCkanMetadataCache ckanCache;
    
    private CkanHarvester ckanHarvester;
    
    @Before
    public void setUp() throws URISyntaxException {
        ckanCache = new InMemoryCkanMetadataCache();
        ckanHarvester = new CkanHarvester();
//        ckanHarvester.setResourceClient(new SeamResourceClient());
        ckanHarvester.setResourceClient(new ResourceClient());
        ckanHarvester.setCkanClient(new CkanClient("https://ckan.colabis.de"));
        ckanHarvester.setResourceDownloadBaseFolder(testFolder.getRoot().toURI().toString());
        ckanHarvester.setDatasetCache(ckanCache);
        
        MatcherAssert.assertThat(ckanCache.size(), CoreMatchers.is(0));
        CkanQuery query = CkanQuery.filter().byTagNames("DWD");
        ckanHarvester.harvestDatasets(query);
    }
    
    @Test
    public void harvestDatasets() {
        MatcherAssert.assertThat(ckanCache.size(), CoreMatchers.is(4));
    }
    
    @Test
    public void harvestResources() throws IOException {
        MatcherAssert.assertThat(testFolder.getRoot().list().length, CoreMatchers.is(0));
        ckanHarvester.harvestResources();
        
        // TODO
    }
    
}
