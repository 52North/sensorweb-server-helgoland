package org.n52.io.extension.resulttime;

import java.util.Set;

import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.DatasetType;

public class ResultTimeService {

    private final ResultTimeRepository repository;

    public ResultTimeService(ResultTimeRepository repository) {
        this.repository = repository;
    }

    public Set<String> getResultTimeList(IoParameters parameters, String timeseriesId) {
        return repository.getExtras(DatasetType.extractId(timeseriesId), parameters);
    }

}
