/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.series.db.dao;

import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.ServiceEntity;

/**
 *
 * @author jansch
 */
public class ServiceDao extends AbstractDao<ServiceEntity>{

    private static final String SERIES_PROPERTY = "service";

    public ServiceDao(Session session) {
        super(session);
    }
    
    @Override
    public List<ServiceEntity> find(DbQuery query) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Class<ServiceEntity> getEntityClass() {
        return ServiceEntity.class;
    }

    @Override
    protected String getSeriesProperty() {
        return SERIES_PROPERTY;
    }

    @Override
    public List<ServiceEntity> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria();
        return criteria.list();
    }
    
}
