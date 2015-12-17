package org.n52.series.ckan.sos;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.n52.series.ckan.beans.CsvObservationsCollection;
import org.n52.series.ckan.beans.DataFile;
import org.n52.series.ckan.beans.ResourceMember;
import org.n52.series.ckan.cache.InMemoryCkanDataCache;
import org.n52.series.ckan.util.FileBasedCkanHarvestingService;

public class DefaultSosInsertionStrategyTest {
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    private FileBasedCkanHarvestingService service;
    
    private DefaultSosInsertionStrategy insertionStrategy;
    
    private InMemoryCkanDataCache ckanDataCache;
    
    @Before
    public void setUp() throws IOException, URISyntaxException {
        service = new FileBasedCkanHarvestingService(testFolder.getRoot());
        ckanDataCache = service.getCkanDataCache();
    }
    
    @Test
    public void parseSensorsFromObservationCollection() {
        insertionStrategy = new DefaultSosInsertionStrategy(null, null);
        for (InMemoryCkanDataCache.Entry<CkanDataset, CsvObservationsCollection> data : ckanDataCache.getCollections()) {
            insertionStrategy.insertOrUpdate(data.getDataset(), data.getData());
        }
    }
}
