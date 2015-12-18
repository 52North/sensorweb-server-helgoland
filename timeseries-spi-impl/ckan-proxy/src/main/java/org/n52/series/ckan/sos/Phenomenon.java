package org.n52.series.ckan.sos;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode(this.id);
        hash = 19 * hash + Objects.hashCode(this.label);
        hash = 19 * hash + Objects.hashCode(this.uom);
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
        final Phenomenon other = (Phenomenon) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        if (!Objects.equals(this.uom, other.uom)) {
            return false;
        }
        return true;
    }
    
}
