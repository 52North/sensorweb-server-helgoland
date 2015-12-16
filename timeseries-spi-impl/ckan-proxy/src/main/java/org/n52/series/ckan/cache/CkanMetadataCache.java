package org.n52.series.ckan.cache;

import com.fasterxml.jackson.databind.JsonNode;
import eu.trentorise.opendata.jackan.model.CkanDataset;

public interface CkanMetadataCache {
    
    public int size();
    
    public void clear();
    
    public boolean contains(CkanDataset dataset);
    
    public boolean containsNewerThan(CkanDataset dataset);
    
    public void insertOrUpdate(CkanDataset dataset);
    
    public void delete(CkanDataset dataset);
    
    public Iterable<String> getDatasetIds();
    
    public Iterable<CkanDataset> getDatasets();
    
    public CkanDataset getDataset(String datasetId);
    
    public boolean hasResourceDescription(CkanDataset datasetId);
    
    public JsonNode getSchemaDescription(String datasetId);
}
