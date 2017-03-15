package org.n52.io.extension.resulttime;

import java.util.Collections;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.da.SessionAwareRepository;
import org.n52.series.db.dao.DatasetDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ResultTimeRepository extends SessionAwareRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultTimeRepository.class);

    Set<String> getExtras(String datasetId, IoParameters parameters) {
        Session session = getSession();
        try {
            DatasetDao<DatasetEntity<?>> dao = new DatasetDao<>(session);
            DatasetEntity<?> instance = dao.getInstance(Long.parseLong(datasetId), getDbQuery(parameters));
            Set<String> resultTimes = instance.getResultTimes();
            Hibernate.initialize(resultTimes);
            return resultTimes;
        } catch (NumberFormatException e) {
            LOGGER.debug("Could not convert id '{}' to long.", datasetId, e);
        } catch (DataAccessException e) {
            LOGGER.error("Could not query result times for dataset with id '{}'", datasetId, e);
        } finally {
            returnSession(session);
        }
        return Collections.emptySet();
    }
}
