package org.n52.io.response;

import java.util.Collection;

public class HierarchicalParameterOutput extends AbstractOutput {

    private Collection<ProcedureOutput> parents;

    private Collection<ProcedureOutput> children;

    public Collection<ProcedureOutput> getParents() {
        return hasParents()
                ? parents
                : null;
    }

    public boolean hasParents() {
        return parents != null && !parents.isEmpty();
    }

    public void setParents(Collection<ProcedureOutput> parents) {
        this.parents = parents;
    }

    public Collection<ProcedureOutput> getChildren() {
        return hasChildren()
                ? children
                : null;
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public void setChildren(Collection<ProcedureOutput> children) {
        this.children = children;
    }

}
