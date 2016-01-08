package org.n52.series.ckan.sos;

import java.util.Objects;

public class Phenomenon {
    
    private final String id;
    
    private final int fieldIdx;
    
    private final String label;
    
    private final String uom;

    public Phenomenon(String id, String label, int fieldIdx) {
        this(id, label, fieldIdx, null);
    }

    public Phenomenon(String id, String label, int fieldIdx, String uom) {
        this.id = id;
        this.uom = uom;
        this.label = label;
        this.fieldIdx = fieldIdx;
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

    public int getFieldIdx() {
        return fieldIdx;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.id);
        hash = 37 * hash + this.fieldIdx;
        hash = 37 * hash + Objects.hashCode(this.label);
        hash = 37 * hash + Objects.hashCode(this.uom);
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
        if (this.fieldIdx != other.fieldIdx) {
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
