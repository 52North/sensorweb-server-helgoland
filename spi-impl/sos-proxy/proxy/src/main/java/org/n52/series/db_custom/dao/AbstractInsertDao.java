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
package org.n52.series.db_custom.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.ServiceEntity;

public abstract class AbstractInsertDao<T> extends AbstractDao<T> implements InsertDao<T> {

    public AbstractInsertDao(Session session) {
        super(session);
    }

    public void clearUnusedForService(ServiceEntity service) {
        Criteria criteria = session.createCriteria(getEntityClass())
                .add(Restrictions.eq("service.pkid", service.getPkid()))
                .add(Subqueries.propertyNotIn("pkid", createDetachedDatasetFilter()));
        criteria.list().forEach(entry -> {
            session.delete(entry);
        });
    }

    private DetachedCriteria createDetachedDatasetFilter() {
        DetachedCriteria filter = DetachedCriteria.forClass(DatasetEntity.class)
                .setProjection(Projections.distinct(Projections.property(getSeriesProperty())));
        return filter;
    }

}
