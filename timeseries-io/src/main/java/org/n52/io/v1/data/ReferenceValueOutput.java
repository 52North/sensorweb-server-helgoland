package org.n52.io.v1.data;

public class ReferenceValueOutput {
    
    private String referenceValueId;
    
    private String label;
    
    private TimeseriesValue lastValue;
    
    public String getReferenceValueId() {
        return referenceValueId;
    }

    public void setReferenceValueId(String referenceValueId) {
        this.referenceValueId = referenceValueId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public TimeseriesValue getLastValue() {
        return lastValue;
    }

    public void setLastValue(TimeseriesValue lastValue) {
        this.lastValue = lastValue;
    }
    
}
