/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.n52.series.db.dao;

import static org.hibernate.criterion.Restrictions.eq;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.io.request.IoParameters;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.PlatformEntity;
import org.n52.series.db.dao.AbstractDao;
import org.n52.series.db.dao.AbstractDao;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.db.dao.DbQuery;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class PlatformDao extends AbstractDao<PlatformEntity> {

    public PlatformDao(Session session) {
        super(session);
    }

    @Override
    public List<PlatformEntity> find(DbQuery query) {
        throw new UnsupportedOperationException("not implemented yet.");
    }

    @Override
    public PlatformEntity getInstance(Long key, DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria().add(eq(PlatformEntity.COLUMN_PKID, key));
        return (PlatformEntity) criteria.uniqueResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PlatformEntity> getAllInstances(DbQuery query) throws DataAccessException {
        Criteria criteria = getDefaultCriteria("platform"); // TODO filter

        // TODO translation

        DetachedCriteria filter = query.createDetachedFilterCriteria("platform");
        criteria.add(Subqueries.propertyIn("platform.pkid", filter));

        IoParameters parameters = query.getParameters();
        if (!parameters.shallIncludeAllPlatformTypes()) {
            boolean includeStationary = parameters.shallIncludeStationaryPlatformTypes();
			boolean includeMobile = parameters.shallIncludeMobilePlatformTypes();
			criteria.add(Restrictions.or(
            		Restrictions.eq(PlatformEntity.MOBILE, !includeStationary), // inverse to match filter
            		Restrictions.eq(PlatformEntity.MOBILE, includeMobile)));

            boolean includeInsitu = parameters.shallIncludeInsituPlatformTypes();
            boolean includeRemote = parameters.shallIncludeRemotePlatformTypes();
			criteria.add(Restrictions.or(
            		Restrictions.eq(PlatformEntity.INSITU, includeInsitu),
            		Restrictions.eq(PlatformEntity.INSITU, !includeRemote))); // inverse to match filter
        }
        return (List<PlatformEntity>) criteria.list();
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
        return session.createCriteria(PlatformEntity.class, alias);
    }

}
