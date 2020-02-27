/**
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.series.api.v1.db.da.dao;

import static org.n52.io.IoParameters.createDefaults;
import static org.n52.series.api.v1.db.da.DbQuery.createFrom;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.series.api.v1.db.da.DbQuery;
import org.n52.series.api.v1.db.da.beans.I18nEntity;
import org.n52.series.api.v1.db.da.beans.SeriesEntity;

abstract class AbstractDao<T> implements GenericDao<T, Long> {

    protected Session session;

    public AbstractDao(Session session) {
        if (session == null) {
            throw new NullPointerException("Cannot operate on a null session.");
        }
        this.session = session;
    }

    public abstract List<T> find(String search, DbQuery query);

    protected boolean hasTranslation(DbQuery parameters, Class<? extends I18nEntity> clazz) {
        Criteria i18nCriteria = session.createCriteria(clazz);
        return parameters.checkTranslationForLocale(i18nCriteria);
    }

    protected abstract String getDefaultAlias();

    protected abstract Class<?> getEntityClass();

    protected Criteria getDefaultCriteria(String alias) {
        return getDefaultCriteria(alias, createFrom(createDefaults()));
    }

    protected Criteria getDefaultCriteria(String alias, DbQuery query) {
        alias = alias != null ? alias : getDefaultAlias();
//        DetachedCriteria filter = createSeriesSubQuery(alias, query);
        DetachedCriteria filter = createSeriesSubQueryViaExplicitJoin(alias, query);
        return session.createCriteria(getEntityClass(), alias)
                .add(Subqueries.propertyIn("pkid", filter));
    }

    private DetachedCriteria createSeriesSubQueryViaExplicitJoin(String alias, DbQuery query) {
        return DetachedCriteria.forClass(SeriesEntity.class)
                .add(Restrictions.eq("published", Boolean.TRUE))
                .createAlias(alias, "ref")
                .setProjection(Projections.property("ref.pkid"));
    }

    private DetachedCriteria createSeriesSubQuery(String alias, DbQuery query) {
        String filterProperty = (alias != null) && !alias.isEmpty()
                ? alias + ".pkid"
                : "pkid";
        return DetachedCriteria.forClass(SeriesEntity.class)
                .add(Restrictions.eq("published", Boolean.TRUE))
                // XXX NPE when filterProperty is mapped by formula
                .setProjection(Projections.property(filterProperty));
    }

}
