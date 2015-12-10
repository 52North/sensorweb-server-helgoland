package org.n52.series.ckan.da;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.CkanQuery;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeamCkanHarvester extends CkanHarvester {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SeamCkanHarvester.class);
    
    private static final String TEST_FILES_BASE_PATH = "/files";
    
    private final ObjectMapper om = new ObjectMapper();
    
    {
        CkanClient.configureObjectMapper(om);
    }

    @Override
    public void harvestDatasets(CkanQuery query) {
        // List<String> tagNames = query.getTagNames(); // TODO
        File folder = new File(TEST_FILES_BASE_PATH + "/dwd");
        File[] datasetFiles = folder.listFiles();
        for (File file : datasetFiles) {
            CkanDataset dataset = parseDatasetTestFile(file);
            getDatasetCache().insertOrUpdate(dataset);
        } 
    }


    private CkanDataset parseDatasetTestFile(File file) {
        try {
            return om.readValue(file, CkanDataset.class);
        } catch (IOException e) {
            LOGGER.error("could not read/parse test file", e);
            return null;
        }
    }
    
}
