package org.n52.series.db.beans;

import java.util.Collections;
import java.util.Set;

public class HierarchicalEntity<T> extends DescribableEntity {

    private Set<T> children;

    private Set<T> parents;

    public final void setChildren(Set<T> children) {
        this.children = children;
    }

    public final Set<T> getChildren() {
        return children != null
                ? Collections.unmodifiableSet(children)
                : null;
    }

    public final void setParents(Set<T> parents) {
        this.parents = parents;

    }

    public final Set<T> getParents() {
        return parents != null
                ? Collections.unmodifiableSet(parents)
                : null;
    }

    public final boolean hasChildren() {
        return getChildren() != null && !getChildren().isEmpty();
    }

    public final boolean hasParents() {
        return getParents() != null && !getParents().isEmpty();
    }

}
