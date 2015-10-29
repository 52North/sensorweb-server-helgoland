package org.n52.io.extension;

import java.util.Date;

public class DatabaseMetadataOutput<T> {

    private T value;
    
    private Date lastUpdate;

    public T getValue() {
        return value;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }
    
    static <T> DatabaseMetadataOutput<T> create() {
        return new DatabaseMetadataOutput<>();
    }

    DatabaseMetadataOutput<T> withValue(T value) {
        this.value = value;
        return this;
    }
    
    DatabaseMetadataOutput<T> lastUpdatedAt(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }
}
