package org.n52.series.ckan.da;

import org.n52.series.ckan.cache.InMemoryCkanMetadataCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.CkanQuery;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.n52.series.ckan.beans.CsvObservationsCollection;
import org.n52.series.ckan.beans.DataFile;
import org.n52.series.ckan.beans.ResourceMember;
import org.n52.series.ckan.table.ObservationTable;
import org.n52.series.ckan.cache.InMemoryCkanDataCache;
import org.n52.series.ckan.cache.InMemoryCkanDataCache.Entry;
import org.n52.series.ckan.util.ResourceClient;

public class CkanHarvestingServiceTest {
    
    private final ObjectMapper om = new ObjectMapper();
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    private InMemoryCkanMetadataCache ckanMetadataCache;
    
    private InMemoryCkanDataCache ckanDataCache;
    
    private CkanHarvestingService ckanHarvester;
    
    @Before
    public void setUp() throws URISyntaxException {
        ckanMetadataCache = new InMemoryCkanMetadataCache();
        ckanDataCache = new InMemoryCkanDataCache();
        ckanHarvester = new CkanHarvestingService();
        
        ckanHarvester.setResourceClient(new ResourceClient()); // TODO load locally from seam
        ckanHarvester.setCkanClient(new CkanClient("https://ckan.colabis.de")); // TODO load locally from seam
        ckanHarvester.setResourceDownloadBaseFolder(testFolder.getRoot().toURI().toString());
        ckanHarvester.setMetadataCache(ckanMetadataCache);
        ckanHarvester.setDataCache(ckanDataCache);
        
        MatcherAssert.assertThat(ckanMetadataCache.size(), CoreMatchers.is(0));
        CkanQuery query = CkanQuery.filter().byTagNames("DWD");
        ckanHarvester.harvestDatasets(query);
    }
    
    @Test
    public void harvestDatasets() {
        MatcherAssert.assertThat(ckanMetadataCache.size(), CoreMatchers.is(4));
    }
    
    @Test
    public void harvestResources() throws IOException {
        MatcherAssert.assertThat(testFolder.getRoot().list().length, CoreMatchers.is(0));
        ckanHarvester.harvestResources();
        
        Entry<CkanDataset, CsvObservationsCollection> entry = ckanDataCache.getCollections().iterator().next();
        Map<ResourceMember, DataFile> dataCollection = entry.getData().getDataCollection();
        Map.Entry<ResourceMember, DataFile> data = dataCollection.entrySet().iterator().next();
        ObservationTable observationTable = new ObservationTable(data.getKey(), data.getValue());
        observationTable.readIntoMemory();
        
//        for (Entry<CkanDataset, CsvObservationsCollection> entry : ckanDataCache.getCollections()) {
//            TableMerger tableMerger = new TableMerger(entry.getData());
//            System.out.println("#### DATASET: " + entry.getDataset().getId());
//            tableMerger.printEachMemberIndividually(entry.getData());
//        }
        
        // TODO
    }
    
}
