package org.n52.series.db.da.dao.v1;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import static org.hibernate.criterion.Restrictions.eq;
import org.n52.io.request.IoParameters;
import org.n52.series.db.da.AbstractDbQuery;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.ext.AbstractObservationEntity;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;
import org.n52.series.db.da.v1.DbQuery;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 * @param <T>
 */
public abstract class AbstractObservationDao<T extends AbstractObservationEntity> extends AbstractDao<T> {

    private static final String COLUMN_SERIES_PKID = "seriesPkid";

    private static final String COLUMN_DELETED = "deleted";

    private final Class<T> entityType;

    public AbstractObservationDao(Session session) {
        super(session);
        this.entityType = (Class<T>) AbstractObservationEntity.class;
    }

    @Override
    public List<T> find(String search, DbQuery query) {
        return new ArrayList<>();
    }

    @Override
    public T getInstance(Long key, DbQuery parameters) throws DataAccessException {
        return (T) session.get(entityType, key);
    }

    /**
     * Retrieves all available observations belonging to a particular series.
     *
     * @param series the entity to get all observations for.
     * @return all observation entities belonging to the series.
     * @throws org.n52.series.db.da.DataAccessException if accessing database
     * fails.
     */
    public List<T> getAllInstancesFor(AbstractSeriesEntity series) throws DataAccessException {
        return getAllInstancesFor(series, DbQuery.createFrom(IoParameters.createDefaults()));
    }

    /**
     * <p>
     * Retrieves all available observation instances.</p>
     *
     * @param parameters query parameters.
     * @return all instances matching the given query parameters.
     * @throws DataAccessException if accessing database fails.
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<T> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .add(eq(COLUMN_DELETED, Boolean.FALSE));
        parameters.addTimespanTo(criteria);
        parameters.addPagingTo(criteria);
        return (List<T>) criteria.list();
    }

    /**
     * Retrieves all available observation instances belonging to a particular
     * series.
     *
     * @param series the series the observations belongs to.
     * @param parameters some query parameters to restrict result.
     * @return all observation entities belonging to the given series which
     * match the given query.
     * @throws DataAccessException if accessing database fails.
     */
    @SuppressWarnings("unchecked")
    public List<T> getAllInstancesFor(AbstractSeriesEntity series, AbstractDbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .add(eq(COLUMN_SERIES_PKID, series.getPkid()))
                .add(eq(COLUMN_DELETED, Boolean.FALSE));
        parameters.addTimespanTo(criteria);
        parameters.addPagingTo(criteria);
        return (List<T>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<T> getObservationsFor(AbstractSeriesEntity series, AbstractDbQuery query) {
        Criteria criteria = query.addTimespanTo(getDefaultCriteria())
                .add(eq(COLUMN_SERIES_PKID, series.getPkid()))
                .add(eq(COLUMN_DELETED, Boolean.FALSE));
        return criteria.list();
    }

    /**
     * Counts all observations not including deleted observations.
     *
     * @return amount of observations
     * @throws org.n52.series.db.da.DataAccessException if accessing database
     * fails.
     */
    @Override
    public int getCount() throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .add(eq(COLUMN_DELETED, Boolean.FALSE))
                .setProjection(Projections.rowCount());
        return criteria != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
    }

    @Override
    protected Criteria getDefaultCriteria() {
        return session.createCriteria(entityType);
    }

}
