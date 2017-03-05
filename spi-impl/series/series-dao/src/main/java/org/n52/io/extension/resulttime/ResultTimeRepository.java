package org.n52.io.extension.resulttime;

import java.util.Collections;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.SessionAwareRepository;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.dao.DatasetDao;

class ResultTimeRepository extends SessionAwareRepository {

    Set<String> getExtras(String timeseriesId, IoParameters parameters) {
        Session session = getSession();
        try {
            DatasetDao<DatasetEntity<?>> dao = new DatasetDao<>(session);
            DatasetEntity<?> instance = dao.getInstance(Long.parseLong(timeseriesId), getDbQuery(parameters));
            Set<String> resultTimes = instance.getResultTimes();
            Hibernate.initialize(resultTimes);
            return resultTimes;
        } catch (NumberFormatException e) {
            DatabaseResultTimeService.LOGGER.debug("Could not convert id '{}' to long.", timeseriesId, e);
        } catch (DataAccessException e) {
            DatabaseResultTimeService.LOGGER.error("Could not query result times for series with id '{}'", timeseriesId, e);
        } finally {
            returnSession(session);
        }
        return Collections.emptySet();
    }
}
