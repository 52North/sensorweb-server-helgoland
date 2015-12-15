package org.n52.series.ckan.cache;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;

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
    
    public CkanResource getResourceDescription(String datasetId);
}
