package org.n52.series.ckan.da;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.CkanQuery;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeamCkanHarvester extends CkanHarvestingService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SeamCkanHarvester.class);
    
    private static final String TEST_FILES_BASE_PATH = "/files";
    
    private final ObjectMapper om = new ObjectMapper();
    
    {
        CkanClient.configureObjectMapper(om);
    }

    @Override
    public void harvestDatasets() {
        final String path = TEST_FILES_BASE_PATH + "/dwd";
        for (File file : getDatasets(path)) {
            CkanDataset dataset = parseDatasetTestFile(file);
            getMetadataCache().insertOrUpdate(dataset);
        }
    }
    
    private List<File> getDatasets(String baseFolder) {
        List<File> datasets = new ArrayList<>();
        File folder = new File(getClass().getResource(baseFolder).getFile());
        File[] datasetFiles = folder.listFiles();
        for (File file : datasetFiles) {
            if (file.isDirectory()) {
                Path datasetPath = file.toPath().resolve("dataset.json");
                datasets.add(datasetPath.toFile());
            }
        } 
        return datasets;
    }

    private CkanDataset parseDatasetTestFile(File file) {
        try {
            return om.readValue(file, CkanDataset.class);
        } catch (IOException e) {
            LOGGER.error("could not read/parse test file", e);
            return new CkanDataset();
        }
    }
    
}
