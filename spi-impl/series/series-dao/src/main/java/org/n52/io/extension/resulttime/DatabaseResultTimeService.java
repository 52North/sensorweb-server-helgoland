package org.n52.io.extension.resulttime;

import java.util.Set;

import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.DatasetType;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.spi.srv.ResultTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseResultTimeService implements ResultTimeService {
    
    static final Logger LOGGER = LoggerFactory.getLogger(DatabaseResultTimeService.class);

    private final ResultTimeRepository repository;
    
    public DatabaseResultTimeService(ResultTimeRepository repository) {
        this.repository = repository;
    }

    @Override
    public Set<String> getResultTimeList(IoParameters parameters, String timeseriesId) {
        return repository.getExtras(DatasetType.extractId(timeseriesId), parameters);
    }

}
