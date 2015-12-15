package org.n52.series.ckan.table;

import java.util.Objects;
import org.n52.series.ckan.beans.ResourceField;

public class JoinIndex {
    
    private final ResourceField field;
    
    private final String value;

    public JoinIndex(ResourceField field, String value) {
        this.field = field;
        this.value = value;
    }

    public ResourceField getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final JoinIndex other = (JoinIndex) obj;
        if (!Objects.equals(this.field, other.field)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "JoinIndex{" + "field=" + field + ", value=" + value + '}';
    }

    
}
