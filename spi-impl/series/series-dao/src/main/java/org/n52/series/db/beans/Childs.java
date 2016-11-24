package org.n52.series.db.beans;

import java.util.Set;

public interface Childs<T> {

    public void setChilds(Set<T> childs);

    public Set<T> getChilds();

    public default boolean hasChilds() {
        return getChilds() != null && !getChilds().isEmpty();
    }
}
