package org.n52.io.format;

import java.util.List;

public class HighchartSeries {
    
    private String name;
    
    private List<Number[]> data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Number[]> getData() {
        return data;
    }

    public void setData(List<Number[]> series) {
        this.data = series;
    }
    
}
