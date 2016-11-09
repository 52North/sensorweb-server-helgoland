/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.series.db.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ServiceEntity;

/**
 *
 * @author jansch
 */
public class ProxyPhenomenonDao extends PhenomenonDao implements InsertDao<PhenomenonEntity>, ClearDao<PhenomenonEntity> {

    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SERVICE_PKID = "service.pkid";

    public ProxyPhenomenonDao(Session session) {
        super(session);
    }

    @Override
    public PhenomenonEntity getOrInsertInstance(PhenomenonEntity phenomenon) {
        PhenomenonEntity instance = getInstance(phenomenon);
        if (instance == null) {
            this.session.save(phenomenon);
            instance = phenomenon;
        }
        return instance;
    }

    @Override
    public void clearUnusedForService(ServiceEntity service) {
        Criteria criteria = session.createCriteria(getEntityClass())
                .add(Restrictions.eq("service.pkid", service.getPkid()))
                .add(Subqueries.propertyNotIn("pkid", createDetachedDatasetFilter()));
        criteria.list().forEach(entry -> {
            session.delete(entry);
        });
    }

    private PhenomenonEntity getInstance(PhenomenonEntity phenomenon) {
        Criteria criteria = session.createCriteria(getEntityClass())
                .add(Restrictions.eq(COLUMN_NAME, phenomenon.getName()))
                .add(Restrictions.eq(COLUMN_SERVICE_PKID, phenomenon.getService().getPkid()));
        return (PhenomenonEntity) criteria.uniqueResult();
    }

    private DetachedCriteria createDetachedDatasetFilter() {
        DetachedCriteria filter = DetachedCriteria.forClass(DatasetEntity.class)
                .setProjection(Projections.distinct(Projections.property(getSeriesProperty())));
        return filter;
    }
}
