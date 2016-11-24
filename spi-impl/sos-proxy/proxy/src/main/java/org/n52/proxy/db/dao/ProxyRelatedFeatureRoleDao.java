package org.n52.proxy.db.dao;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.n52.proxy.db.beans.RelatedFeatureRoleEntity;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.dao.AbstractDao;
import org.n52.series.db.dao.DbQuery;

public class ProxyRelatedFeatureRoleDao extends AbstractDao<RelatedFeatureRoleEntity> implements InsertDao<RelatedFeatureRoleEntity>, ClearDao<RelatedFeatureRoleEntity> {
    

    private static final String SERIES_PROPERTY = "relatedFeature";

    public ProxyRelatedFeatureRoleDao(Session session) {
        super(session);
    }

    @Override
    public List<RelatedFeatureRoleEntity> find(DbQuery query) {
        return new ArrayList<RelatedFeatureRoleEntity>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RelatedFeatureRoleEntity> getAllInstances(DbQuery query) throws DataAccessException {
        return (List<RelatedFeatureRoleEntity>) getDefaultCriteria().list();
    }

    @Override
    protected String getSeriesProperty() {
        return SERIES_PROPERTY;
    }

    @Override
    protected Class<RelatedFeatureRoleEntity> getEntityClass() {
        return RelatedFeatureRoleEntity.class;
    }

    @Override
    public RelatedFeatureRoleEntity getOrInsertInstance(RelatedFeatureRoleEntity relatedFeature) {
        RelatedFeatureRoleEntity instance = getInstance(relatedFeature);
        if (instance == null) {
            this.session.save(relatedFeature);
            instance = relatedFeature;
        }
        return instance;
    }

    @Override
    public void clearUnusedForService(ServiceEntity service) {
        // nothing to do
    }

    private RelatedFeatureRoleEntity getInstance(RelatedFeatureRoleEntity relatedFeature) {
        Criteria criteria = session.createCriteria(getEntityClass());;
        return (RelatedFeatureRoleEntity) criteria.uniqueResult();
    }

}
