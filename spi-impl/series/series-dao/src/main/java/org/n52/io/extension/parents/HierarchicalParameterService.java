package org.n52.io.extension.parents;

import java.util.Map;
import java.util.Set;

import org.n52.io.request.IoParameters;
import org.n52.io.response.HierarchicalParameterOutput;

public class HierarchicalParameterService {

    private final HierarchicalParameterRepository repository;

    public HierarchicalParameterService(HierarchicalParameterRepository repository) {
        this.repository = repository;
    }

    Map<String, Set<HierarchicalParameterOutput>> getExtras(String platformId, IoParameters parameters) {
        return repository.getExtras(platformId, parameters);
    }

}
