package org.n52.io.extension.resulttime;

import java.util.Set;

import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.DatasetType;
import org.n52.series.spi.srv.ResultTimeService;

public class DatabaseResultTimeService implements ResultTimeService {

    private final ResultTimeRepository repository;

    public DatabaseResultTimeService(ResultTimeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Set<String> getResultTimeList(IoParameters parameters, String timeseriesId) {
        return repository.getExtras(DatasetType.extractId(timeseriesId), parameters);
    }

}
