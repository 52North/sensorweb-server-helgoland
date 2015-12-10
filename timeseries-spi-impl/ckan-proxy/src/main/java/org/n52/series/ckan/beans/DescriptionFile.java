package org.n52.series.ckan.beans;

import com.fasterxml.jackson.databind.JsonNode;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import java.io.File;
import java.sql.Timestamp;
import org.joda.time.DateTime;

public class DescriptionFile {
    
    private final CkanDataset dataset;
    
    private final File file;
    
    private final JsonNode node;
    
    public DescriptionFile(CkanDataset dataset, File file, JsonNode node) {
        this.dataset = dataset;
        this.file = file;
        this.node = node;
    }
    
    public CkanDataset getDataset() {
        return dataset;
    }

    public File getFile() {
        return file;
    }

    public JsonNode getNode() {
        return node;
    }
    
    public DateTime getLastModified() {
        return new DateTime(dataset.getMetadataModified());
    }
    
    public boolean isNewerThan(CkanDataset dataset) {
        if (dataset == null) {
            return false;
        }
        Timestamp probablyNewer = dataset.getMetadataModified();
        Timestamp current = this.dataset.getMetadataModified();
        return this.dataset.getId().equals(dataset.getId())
                ? current.after(probablyNewer)
                : false;
        
    }
    
}
