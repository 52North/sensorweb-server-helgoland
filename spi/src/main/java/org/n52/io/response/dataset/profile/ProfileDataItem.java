
package org.n52.io.response.dataset.profile;

public class ProfileDataItem<T> {
    
    private String verticalUnit;
    
    private Double vertical;
    
    private T value;

    public String getVerticalUnit() {
        return verticalUnit;
    }

    public void setVerticalUnit(String verticalUnit) {
        this.verticalUnit = verticalUnit;
    }

    public Double getVertical() {
        return vertical;
    }

    public void setVertical(Double vertical) {
        this.vertical = vertical;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

}
