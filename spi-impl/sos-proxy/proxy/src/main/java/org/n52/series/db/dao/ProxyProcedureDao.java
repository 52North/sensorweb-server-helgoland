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
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.beans.ServiceEntity;

public class ProxyProcedureDao extends ProcedureDao implements InsertDao<ProcedureEntity>, ClearDao<ProcedureEntity> {

    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SERVICE_PKID = "service.pkid";

    public ProxyProcedureDao(Session session) {
        super(session);
    }

    @Override
    public ProcedureEntity getOrInsertInstance(ProcedureEntity procedure) {
        ProcedureEntity instance = getInstance(procedure);
        if (instance == null) {
            this.session.save(procedure);
            instance = procedure;
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

    private ProcedureEntity getInstance(ProcedureEntity procedure) {
        Criteria criteria = session.createCriteria(getEntityClass())
                .add(Restrictions.eq(COLUMN_NAME, procedure.getName()))
                .add(Restrictions.eq(COLUMN_SERVICE_PKID, procedure.getService().getPkid()));
        return (ProcedureEntity) criteria.uniqueResult();
    }

    private DetachedCriteria createDetachedDatasetFilter() {
        DetachedCriteria filter = DetachedCriteria.forClass(DatasetEntity.class)
                .setProjection(Projections.distinct(Projections.property(getSeriesProperty())));
        return filter;
    }

}
