package org.n52.proxy.db.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.dao.DataDao;

public class ProxyDataDao<T extends DataEntity> extends DataDao<T> {

    public ProxyDataDao(Session session) {
        super(session);
    }
    
    public ProxyDataDao(Session session, Class<T> clazz) {
        super(session, clazz);
    }

    public Long getObservationCount(DatasetEntity<?> entity) {
        return (Long) getDefaultCriteria()
                .add(Restrictions.eq(DataEntity.SERIES_PKID, entity.getPkid()))
                .setProjection(Projections.rowCount())
                .uniqueResult();
    }

}
