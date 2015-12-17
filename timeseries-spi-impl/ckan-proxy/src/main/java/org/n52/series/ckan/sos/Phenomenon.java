package org.n52.series.ckan.sos;

public class Phenomenon {
    
    private final String id;
    
    private final String label;
    
    private final String uom;

    public Phenomenon(String id, String label) {
        this(id, label, null);
    }

    public Phenomenon(String id, String label, String uom) {
        this.id = id;
        this.uom = uom;
        this.label = label;
    }

    public String getId() {
        return id;
    }
    
    public String getLabel() {
        return label;
    }

    public String getUom() {
        return uom;
    }

    
    
}
