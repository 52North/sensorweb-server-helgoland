package org.n52.series.db.da.v1;

import org.n52.io.response.series.SeriesData;
import org.n52.series.db.da.DataAccessException;

public interface DataRepository<T extends SeriesData> {

    T getData(String id, DbQuery dbQuery) throws DataAccessException;
}
