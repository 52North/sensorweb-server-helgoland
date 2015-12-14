package org.n52.series.ckan.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ResourceMember {
    
    private String id;
    
    private int headerRows;
    
    private String resourceType;
    
    private List<ResourceField> resourceFields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public int getHeaderRows() {
        return headerRows;
    }

    public void setHeaderRows(int headerRows) {
        this.headerRows = headerRows;
    }

    public List<ResourceField> getResourceFields() {
        return Collections.unmodifiableList(resourceFields);
    }
    
    public ResourceField getField(String fieldId) {
        for (ResourceField field : resourceFields) {
            if (field.getFieldId().equalsIgnoreCase(fieldId)) {
                return field;
            }
        }
        return null;
    }
    
    public boolean containsField(String fieldId) {
        for (ResourceField field : resourceFields) {
            if (field.getFieldId().equalsIgnoreCase(fieldId)) {
                return true;
            }
        }
        return false;
    }
    
    public List<String> getColumnHeaders() {
        List<String> headers = new ArrayList<>();
        for (ResourceField field : getResourceFields()) {
            headers.add(field.getShortName());
        }
        return headers;
    }

    public void setResourceFields(List<ResourceField> resourceFields) {
        this.resourceFields = resourceFields;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResourceMember other = (ResourceMember) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }
    
    
}
    
