package org.n52.series.ckan.beans;

import eu.trentorise.opendata.jackan.model.CkanResource;
import java.io.File;
import java.nio.charset.Charset;
import org.joda.time.DateTime;

public class DataFile {
    
    private Charset encoding = Charset.forName("Cp1250"); // TODO 
    
    private final CkanResource resource;
    
    private final File file;
    
    public DataFile(CkanResource resource, File file) {
        this.resource = resource;
        this.file = file;
    }

    public Charset getEncoding() {
        return encoding;
    }

    public CkanResource getResource() {
        return resource;
    }

    public File getFile() {
        return file;
    }

    public DateTime getLastModified() {
        return DateTime.parse(resource.getLastModified());
    }
    
    public boolean isNewerThan(CkanResource resource) {
        if (resource == null) {
            return false;
        }
        DateTime probablyNewer = DateTime.parse(resource.getLastModified());
        DateTime current = DateTime.parse(this.resource.getLastModified());
        return this.resource.getId().equals(resource.getId())
                ? current.isAfter(probablyNewer)
                : false;
        
    }
    
}
