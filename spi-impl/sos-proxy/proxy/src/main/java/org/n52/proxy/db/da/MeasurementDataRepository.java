package org.n52.proxy.db.da;

import java.util.Date;
import org.hibernate.Session;
import org.n52.io.response.dataset.measurement.MeasurementValue;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.dao.DbQuery;

public class MeasurementDataRepository extends org.n52.series.db.da.MeasurementDataRepository {

    @Override
    public MeasurementValue getFirstValue(MeasurementDatasetEntity entity, Session session, DbQuery query) {
        // TODO get firstValue by GetObservation...
        return new MeasurementValue(new Date().getTime(), 123.0);
    }

    @Override
    public MeasurementValue getLastValue(MeasurementDatasetEntity entity, Session session, DbQuery query) {
        // TODO get lastValue by GetObservation...
        return new MeasurementValue(new Date().getTime(), 234.0);
    }

}
