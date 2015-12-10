package org.n52.series.ckan.cache;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import org.n52.series.ckan.beans.CsvObservationsCollection;

public interface CkanDataCache {

    public void insertOrUpdate(CkanDataset dataset, CsvObservationsCollection csvObservationsCollection);
    
}
