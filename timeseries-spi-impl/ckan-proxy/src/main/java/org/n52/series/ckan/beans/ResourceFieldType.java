package org.n52.series.ckan.beans;

public abstract class ResourceFieldType<T> {
    
    private final String fieldType;
    
    private String fieldFormat;

    public ResourceFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
    
    public String getFieldFormat() {
        return fieldFormat;
    }

    public void setFieldFormat(String fieldFormat) {
        this.fieldFormat = fieldFormat;
    }

}
