package org.n52.proxy.connector;

import java.util.List;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.series.db.beans.MeasurementDataEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.dao.DbQuery;

public class OceanotronSosConnector extends AbstractSosConnector {

    @Override
    public ServiceConstellation getConstellation(DataSourceConfiguration config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean canHandle(DataSourceConfiguration config) {
        return false;
    }

    @Override
    public List<MeasurementDataEntity> getObservations(MeasurementDatasetEntity seriesEntity, DbQuery query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
