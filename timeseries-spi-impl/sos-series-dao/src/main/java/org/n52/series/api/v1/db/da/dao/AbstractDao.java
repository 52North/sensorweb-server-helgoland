/**
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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

import static org.hibernate.criterion.Restrictions.eq;
import static org.n52.io.IoParameters.createDefaults;
import static org.n52.series.api.v1.db.da.DbQuery.createFrom;

import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.n52.series.api.v1.db.da.DbQuery;
import org.n52.series.api.v1.db.da.SessionAwareRepository;
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
        String mergeRoleParameter = SessionAwareRepository.SERIES_MERGE_ROLES;
        return DetachedCriteria.forClass(SeriesEntity.class)
                .add(createMergeRolesDisjunction(mergeRoleParameter, query, "master"))
                .add(Restrictions.eq("published", Boolean.TRUE))
                .createAlias(alias, "ref")
                .setProjection(Projections.property("ref.pkid"));
    }

    private DetachedCriteria createSeriesSubQuery(String alias, DbQuery query) {
        String filterProperty = alias != null && !alias.isEmpty()
                ? alias + ".pkid"
                : "pkid";
        String mergeRoleParameter = SessionAwareRepository.SERIES_MERGE_ROLES;
        return DetachedCriteria.forClass(SeriesEntity.class)
                .add(createMergeRolesDisjunction(mergeRoleParameter, query, "master"))
                .add(Restrictions.eq("published", Boolean.TRUE))
                // XXX NPE when filterProperty is mapped by formula
                .setProjection(Projections.property(filterProperty));
    }
    
    protected Disjunction createMergeRolesDisjunction(String roleParameter, DbQuery query, String... defaults) {
        return createMergeRolesDisjunction(roleParameter, "", query, defaults);
    }

    protected Disjunction createMergeRolesDisjunction(String roleParameter, String alias, DbQuery query, String... defaults) {
        if ( !query.getParameters().containsParameter(roleParameter)) {
            query = DbQuery.createFrom(query.getParameters()
                    .extendWith(roleParameter, defaults));
        }

        Disjunction disjunction = Restrictions.disjunction();
        if (query.getParameters().containsParameter(roleParameter)) {
            String property = alias != null && !alias.isEmpty()
                    ? alias + ".mergeRole"
                    : "mergeRole";
            Set<String> roles = query.getParameters().getOthers(roleParameter);
            for (String role : roles) {
                disjunction.add(eq(property, role));
            }
        }
        return disjunction;
    }
    
    

}
