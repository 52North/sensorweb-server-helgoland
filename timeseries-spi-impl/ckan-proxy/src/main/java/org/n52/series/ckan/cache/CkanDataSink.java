package org.n52.series.ckan.cache;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import org.n52.series.ckan.beans.CsvObservationsCollection;

public interface CkanDataSink {
    
    public int size();
    
    public void clear();

    public void insertOrUpdate(CkanDataset dataset, CsvObservationsCollection csvObservationsCollection);
    
    //public Iterable<InMemoryCkanDataCache.Entry<CkanDataset, CsvObservationsCollection>> getCollections();
}
