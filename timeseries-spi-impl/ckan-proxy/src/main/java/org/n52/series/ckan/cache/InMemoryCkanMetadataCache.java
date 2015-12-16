package org.n52.series.ckan.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.google.common.base.Strings;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanPair;
import eu.trentorise.opendata.jackan.model.CkanResource;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import org.n52.series.ckan.da.CkanConstants;
import org.n52.sos.exception.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryCkanMetadataCache implements CkanMetadataCache {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryCkanMetadataCache.class);
    
    private final ObjectMapper om = new ObjectMapper(); // TODO use global om config
    
    private final Map<String, CkanDataset> datasets;
    
    public InMemoryCkanMetadataCache() {
        datasets = new HashMap<>();
    }

    @Override
    public int size() {
        return datasets.size();
    }

    @Override
    public void clear() {
        datasets.clear();
    }
    
    @Override
    public boolean contains(CkanDataset dataset) {
        if (dataset == null) {
            return false;
        }
        return datasets.containsKey(dataset.getId());
    }

    @Override
    public boolean containsNewerThan(CkanDataset dataset) {
        if (dataset == null || !contains(dataset)) {
            return false;
        }
        Timestamp probablyNewer = dataset.getMetadataModified();
        Timestamp current = datasets.get(dataset.getId()).getMetadataModified();
        return current.after(probablyNewer)
                || current.equals(probablyNewer);
    }

    @Override
    public void insertOrUpdate(CkanDataset dataset) {
        if (dataset != null) {
            if (containsNewerThan(dataset)) {
                LOGGER.info("No metadata updates on dataset {}.", dataset.getId());
            }
            if (hasResourceDescription(dataset)) {
                datasets.put(dataset.getId(), dataset);
                // TODO load resource files if newer and 
                  // TODO update metadata
                  // TODO update observation data
            } else {
                LOGGER.info("Ignore dataset '{}' as it has no ResourceDescription.", dataset.getId());
            }
        }
    }

    @Override
    public void delete(CkanDataset dataset) {
        if (dataset != null) {
            datasets.remove(dataset.getId());
        }
    }

    @Override
    public Iterable<String> getDatasetIds() {
        return datasets.keySet();
    }

    @Override
    public Iterable<CkanDataset> getDatasets() {
        return datasets.values();
    }

    @Override
    public CkanDataset getDataset(String datasetId) {
        return datasets.get(datasetId);
    }
    
    @Override
    public JsonNode getSchemaDescription(String datasetId) {
        return datasets.containsKey(datasetId)
                ? getResourceDesciptionFor(getDataset(datasetId))
                : null;
    }

    @Override
    public boolean hasResourceDescription(CkanDataset dataset) {
        return getResourceDesciptionFor(dataset) != null;
    }

    private JsonNode getResourceDesciptionFor(CkanDataset dataset) {
        for (CkanPair extras : dataset.getExtras()) {
            if (extras.getKey().equalsIgnoreCase(CkanConstants.SCHEMA_DESCRIPTOR)) {
                try {
                    return om.readTree(extras.getValue());
                } catch (IOException e) {
                     LOGGER.error("Could not read schema_descriptor: {}", extras.getValue(), e);
                }
            }
        }
        return MissingNode.getInstance();
    }
    
}
