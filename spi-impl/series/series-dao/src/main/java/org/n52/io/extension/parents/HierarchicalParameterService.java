package org.n52.io.extension.parents;

import java.util.Collection;
import java.util.Map;

import org.n52.io.request.IoParameters;

public class HierarchicalParameterService {

    private final HierarchicalParameterRepository repository;

    public HierarchicalParameterService(HierarchicalParameterRepository repository) {
        this.repository = repository;
    }

    Map<String, Collection<String>> getExtras(String platformId, IoParameters parameters) {
        return repository.getExtras(platformId, parameters);
    }

}
