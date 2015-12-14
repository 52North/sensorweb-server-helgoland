package org.n52.series.ckan.cache;

import eu.trentorise.opendata.jackan.model.CkanDataset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.n52.series.ckan.beans.CsvObservationsCollection;

public class InMemoryCkanDataCache implements CkanDataSink {
    
    private final Map<String, Entry<CkanDataset, CsvObservationsCollection>> datasets = new HashMap<>();

    @Override
    public int size() {
        return datasets.size();
    }

    @Override
    public void clear() {
        datasets.clear();
    }

    @Override
    public void insertOrUpdate(CkanDataset dataset, CsvObservationsCollection csvObservationsCollection) {
        if (dataset == null) {
            return;
        }
        
        if (datasets.containsKey(dataset.getId())) {
            // TODO update
        } else {
            datasets.put(dataset.getId(), new Entry<>(dataset, csvObservationsCollection));
        }
    }

    public Iterable<Entry<CkanDataset, CsvObservationsCollection>> getCollections() {
        return datasets.values();
    }
    
    public class Entry<M,D> {
        private M dataset;
        private D data;

        public Entry(M dataset, D data) {
            this.dataset = dataset;
            this.data = data;
        }
        
        public M getDataset() {
            return dataset;
        }

        public void setDataset(M dataset) {
            this.dataset = dataset;
        }

        public D getData() {
            return data;
        }

        public void setData(D data) {
            this.data = data;
        }
        
    }
    
}
