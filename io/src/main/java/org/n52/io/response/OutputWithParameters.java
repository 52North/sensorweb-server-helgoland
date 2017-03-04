package org.n52.io.response;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OutputWithParameters extends AbstractOutput {

    private Set<Map<String, Object>> parameters;

    public void setParameters(Set<Map<String, Object>> parameters) {
        this.parameters = new HashSet<>(parameters);
    }

    public Set<Map<String, Object>> getParameters() {
        return parameters != null
                ? Collections.unmodifiableSet(parameters)
                : null;
    }

    public void addParameter(Map<String, Object> parameterValues) {
        if (parameters == null) {
            parameters = new HashSet<>();
        }
        parameters.add(parameterValues);
    }

}
