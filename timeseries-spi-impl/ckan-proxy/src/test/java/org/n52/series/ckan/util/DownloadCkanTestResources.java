package org.n52.series.ckan.util;

import eu.trentorise.opendata.jackan.CkanClient;
import java.io.IOException;
import java.net.URISyntaxException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Ignore;
import org.junit.Test;
import org.n52.series.ckan.cache.InMemoryCkanDataCache;
import org.n52.series.ckan.cache.InMemoryCkanMetadataCache;
import org.n52.series.ckan.da.CkanHarvestingService;

@Ignore("to download current test data, run this test (requires remote access). After downloading, the resources can be found in the test-classes fodler.")
public class DownloadCkanTestResources {
    
    @Test
    public void downloadCurrentTestDataFromCkan() throws URISyntaxException, IOException {
        InMemoryCkanMetadataCache ckanMetadataCache = new InMemoryCkanMetadataCache();
        InMemoryCkanDataCache ckanDataCache = new InMemoryCkanDataCache();
        
        CkanHarvestingService ckanHarvester = new CkanHarvestingService();
        ckanHarvester.setCkanClient(new CkanClient("https://ckan.colabis.de"));
        ckanHarvester.setResourceClient(new ResourceClient());
        
        String baseFolder = getClass().getResource("/files").toString();
        ckanHarvester.setResourceDownloadBaseFolder(baseFolder + "/dwd");
        ckanHarvester.setMetadataCache(ckanMetadataCache);
        ckanHarvester.setDataCache(ckanDataCache);
        
        MatcherAssert.assertThat(ckanMetadataCache.size(), CoreMatchers.is(0));
        ckanHarvester.harvestDatasets();
        ckanHarvester.harvestResources();
    }
}
