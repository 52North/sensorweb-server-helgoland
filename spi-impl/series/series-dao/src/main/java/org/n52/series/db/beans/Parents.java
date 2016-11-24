package org.n52.series.db.beans;

import java.util.Set;

public interface Parents<T> {

    public void setParents(Set<T> parents);

    public Set<T> getParents();

    public default boolean hasParents() {
        return getParents() != null && !getParents().isEmpty();
    }
    
}
