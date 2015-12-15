package org.n52.series.ckan.da;

import org.n52.series.ckan.cache.CkanMetadataCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.n52.series.ckan.util.ResourceClient;
import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.CkanQuery;
import eu.trentorise.opendata.jackan.SearchResults;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import eu.trentorise.opendata.traceprov.internal.org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.n52.series.ckan.beans.CsvObservationsCollection;
import org.n52.series.ckan.beans.DataFile;
import org.n52.series.ckan.beans.DescriptionFile;
import org.n52.series.ckan.cache.CkanDataSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CkanHarvestingService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CkanHarvestingService.class);
    
    private ObjectMapper om = new ObjectMapper(); // TODO use global om config
    
    private URI resourceDownloadBaseFolder;
    
    private int pagingLimit = 20;
    
    private CkanClient ckanClient;
    
    private ResourceClient resourceClient;
    
    private CkanMetadataCache metadataCache;
    
    private CkanDataSink dataCache;

    public CkanHarvestingService() {
        try {
            this.resourceDownloadBaseFolder = getClass().getResource("/").toURI();
        } catch (URISyntaxException e) {
            LOGGER.error("Could not set download base folder!", e);
        }
    }
    
    public void harvestDatasets() {
        harvestDatasets(CkanQuery.filter());
    }
    
    public void harvestDatasets(CkanQuery query) {
        LOGGER.info("Start harvesting CKAN datasets.");
        int limit = pagingLimit;
        int offset = 0;
        int lastSize = -1;
        while (hasMorePages(lastSize)) {
            offset += lastSize > 0 ? lastSize : 0;
            SearchResults<CkanDataset> datasets = ckanClient.searchDatasets(query, limit, offset);
            for (CkanDataset dataset : datasets.getResults()) {
                metadataCache.insertOrUpdate(dataset);
            }
            lastSize = datasets.getCount();
        }
        LOGGER.info("Finished harvesting CKAN datasets (got #{} datasets).", metadataCache.size());
    }
    
    private boolean hasMorePages(int lastSize) {
        boolean atStart = lastSize < 0;
        boolean hadResults = lastSize > 0;
        boolean fullPage = lastSize == pagingLimit;
        return atStart || hadResults && fullPage;
    }
    
    public void harvestResources() throws IOException {
        LOGGER.info("Start harvesting data resources.");
        for (CkanDataset dataset : metadataCache.getDatasets()) {
            if (metadataCache.hasResourceDescription(dataset)) {
                LOGGER.info("Download resources for dataset {}.", dataset.getId());
                DescriptionFile description = downloadResourceDescription(dataset);
                
                String datasetId = dataset.getId();
                List<String> resourceIds = getNonResourceDescriptionIds(description.getNode());
                Map<String, DataFile> csvContents = downloadCsvFiles(dataset, resourceIds);
                
                // TODO check when to delete or update resource
                
                dataCache.insertOrUpdate(dataset, 
                        new CsvObservationsCollection(datasetId, description, csvContents));
            }
        }
        LOGGER.info("Finished harvesting data resources (got #{} resources).", dataCache.size());
    }
    
    private DescriptionFile downloadResourceDescription(CkanDataset dataset) throws IOException {
        CkanResource resourceDescriptionResource = metadataCache.getResourceDescription(dataset.getId());
        final String resourceName = extractFileName(resourceDescriptionResource);
        File file = getDatasetDownloadFolder(dataset)
                .resolve(resourceName)
                .toFile();
        downloadToFile(resourceDescriptionResource, file);
        LOGGER.info("Downloaded resource description to {}.", file.getAbsolutePath());
        return new DescriptionFile(dataset, file, om.readTree(file));
    }

    protected List<String> getNonResourceDescriptionIds(JsonNode resourceDescription) {
        List<String> resourceIds = new ArrayList<>();
        for (JsonNode node : resourceDescription.at("/members")) {
            JsonNode resourceId = node.findValue("resourceId");
            resourceId = resourceId == null // XXX fix data
                    ? node.findValue("resourceid")
                    : resourceId;
            resourceIds.add(resourceId.asText());
        }
        return resourceIds;
    }

    private Map<String, DataFile> downloadCsvFiles(CkanDataset dataset, List<String> resourceIds) {
        Map<String, DataFile> csvFiles = new HashMap<>();
        Path datasetDownloadFolder = getDatasetDownloadFolder(dataset);
        for (CkanResource resource : dataset.getResources()) {
            if (resourceIds.contains(resource.getId())) {
                final String resourceName = extractFileName(resource);
                File file = datasetDownloadFolder.resolve(resourceName).toFile();
                
                // TODO download only when newer
                
                downloadToFile(resource, file);
                LOGGER.info("Downloaded data to {}.", file.getAbsolutePath());
                csvFiles.put(resource.getId(), new DataFile(resource, file));
            }
        }
        return csvFiles;
    }

    private String extractFileName(CkanResource resource) {
        final String url = resource.getUrl();
        return url != null
                ? url.substring(url.lastIndexOf("/") + 1)
                : resource.getName() + "." + resource.getFormat().toLowerCase();
    }

    private Path getDatasetDownloadFolder(CkanDataset dataset) {
        return Paths.get(resourceDownloadBaseFolder).resolve(dataset.getName());
    }
    
    private void downloadToFile(CkanResource resource, File file) {
        try {
            String csvContent = resourceClient.downloadTextResource(resource.getUrl());
            FileUtils.writeStringToFile(file, csvContent);
        } catch (IOException e) {
            LOGGER.error("Could not download resource '{}' from {}.", 
                    resource.getId(), resource.getUrl(), e);
        }
    }

    public String getResourceDownloadBaseFolder() throws URISyntaxException {
        return resourceDownloadBaseFolder.toString();
    }

    public void setResourceDownloadBaseFolder(String resourceDownloadBaseFolder) throws URISyntaxException {
        this.resourceDownloadBaseFolder = new URI(resourceDownloadBaseFolder);
    }
    
    public int getPagingLimit() {
        return pagingLimit;
    }

    public void setPagingLimit(int pagingLimit) {
        this.pagingLimit = pagingLimit;
    }

    public CkanClient getCkanClient() {
        return ckanClient;
    }

    public void setCkanClient(CkanClient ckanClient) {
        this.ckanClient = ckanClient;
    }
    
    public ResourceClient getResourceClient() {
        return resourceClient;
    }

    public void setResourceClient(ResourceClient resourceClient) {
        this.resourceClient = resourceClient;
    }

    public CkanDataSink getDataCache() {
        return dataCache;
    }

    public void setDataCache(CkanDataSink dataCache) {
        this.dataCache = dataCache;
    }

    public CkanMetadataCache getMetadataCache() {
        return metadataCache;
    }

    public void setMetadataCache(CkanMetadataCache metadataCache) {
        this.metadataCache = metadataCache;
    }
    
}
