/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.proxy.db.da;

import com.google.common.base.Strings;
import org.hibernate.Session;
import org.n52.io.response.TimeseriesMetadataOutput;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.da.TimeseriesRepository;
import org.n52.series.db.dao.DbQuery;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jansch
 */
public class ProxyTimeseriesRepository extends TimeseriesRepository {

    @Autowired
    private ProxyDatasetRepository datasetRepository;

    @Override
    protected TimeseriesMetadataOutput createExpanded(Session session, MeasurementDatasetEntity series, DbQuery query) throws DataAccessException {
        TimeseriesMetadataOutput output = super.createExpanded(session, series, query);
        if (Strings.isNullOrEmpty(output.getUom())) {
            DatasetOutput datasetOutput = datasetRepository.createExpanded(series, query, session);
            output.setUom(datasetOutput.getUom());
        }
        return output;
    }
}
