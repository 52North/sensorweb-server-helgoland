package org.n52.series.ckan.beans;

import java.util.List;

public class ResourceMember {
    
    private enum ResourceMemberType {
        PLATFORMS,
        OBSERVATIONS
    }
    
    private String id;
    
    private ResourceMemberType resourceType;
    
    private int headerRows;
    
    private List<ResourceField> resourceFields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResourceType() {
        return resourceType.toString().toLowerCase();
    }

    public void setResourceType(String resourceType) {
        this.resourceType = ResourceMemberType.valueOf(resourceType.toUpperCase());
    }

    public int getHeaderRows() {
        return headerRows;
    }

    public void setHeaderRows(int headerRows) {
        this.headerRows = headerRows;
    }

    public List<ResourceField> getResourceFields() {
        return resourceFields;
    }

    public void setResourceFields(List<ResourceField> resourceFields) {
        this.resourceFields = resourceFields;
    }
    
}
    
