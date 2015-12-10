package org.n52.series.ckan.da;

import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.CkanQuery;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CkanClientTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CkanClientTest.class);

    @Test
    @Ignore
    public void getAllDatasetNames() {
        LOGGER.info("#### all dataset names available");
        CkanClient client = new CkanClient("https://ckan.colabis.de/");
        List<String> datasetList = client.getDatasetList();
        for (String dataset : datasetList) {
            LOGGER.info("dataset {}", dataset);
        }
    }
    
    @Test
    @Ignore
    public void getAllDatasetsTaggedWithDWD() {
        LOGGER.info("#### first 10 datasets tagged with DWD");
        CkanClient client = new CkanClient("https://ckan.colabis.de/");
        CkanQuery query = CkanQuery.filter().byTagNames("DWD");
        List<CkanDataset> datasetList = client.searchDatasets(query, 10, 0).getResults();
        for (CkanDataset dataset : datasetList) {
            LOGGER.info("Dataset ##############################");
            LOGGER.info("id: {}", dataset.getId());
            LOGGER.info("name: {}", dataset.getName());
        }
    }

}
