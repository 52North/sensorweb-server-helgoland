package org.n52.series.db.da.dao.v1.ext;

import static org.hibernate.criterion.Restrictions.eq;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.ext.AbstractObservationEntity;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;
import org.n52.series.db.da.dao.v1.AbstractDao;
import org.n52.series.db.da.v1.DbQuery;

public class SeriesDao extends AbstractDao<AbstractSeriesEntity<AbstractObservationEntity>> {

    public SeriesDao(Session session) {
        super(session);
    }

    @Override
    public List<AbstractSeriesEntity<AbstractObservationEntity>> find(DbQuery query) {
        throw new UnsupportedOperationException("not implemented yet.");
    }

    @Override
    public AbstractSeriesEntity getInstance(Long key, DbQuery parameters)
            throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .add(eq("pkid", key));
        return (AbstractSeriesEntity) criteria.uniqueResult();
    }

    @Override
    public List<AbstractSeriesEntity<AbstractObservationEntity>> getAllInstances(DbQuery parameters)
            throws DataAccessException {
        Criteria criteria = getDefaultCriteria("series"); // TODO filter
        parameters.addPagingTo(criteria);
        return (List<AbstractSeriesEntity<AbstractObservationEntity>>) criteria.list();
    }

    @Override
    public int getCount() throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .setProjection(Projections.rowCount());
        return criteria != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
    }

    @Override
    protected Criteria getDefaultCriteria() {
        return getDefaultCriteria(null);
    }

    private Criteria getDefaultCriteria(String alias) {
        return session.createCriteria(AbstractSeriesEntity.class, alias);
    }


}
