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
package org.n52.series.db.da.dao.v1;

import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.sql.JoinType;
import org.n52.series.db.da.v1.DbQuery;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.FeatureEntity;
import org.n52.series.db.da.beans.I18nFeatureEntity;
import org.n52.series.db.da.beans.I18nProcedureEntity;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;
import org.n52.series.db.da.beans.ext.PlatformEntity;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class SeriesDao<T extends AbstractSeriesEntity> extends AbstractDao<T> {

    private static final String COLUMN_PKID = "pkid";

    private final Class<T> entityType;

    private SeriesDao(Session session, Class<T> clazz) {
        super(session);
        this.entityType = clazz;//(Class<T>) AbstractSeriesEntity.class;
    }

    public SeriesDao(Session session) {
        super(session);
        this.entityType = (Class<T>) AbstractSeriesEntity.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> find(DbQuery query) {

        /*
         * Timeseries labels are constructed from labels of related feature
         * and phenomenon. Therefore we have to join both tables and search
         * for given pattern on any of the stored labels.
         */
        List<T> series = new ArrayList<>();
        Criteria criteria = addIgnoreNonPublishedSeriesTo(getDefaultCriteria());
        Criteria featureCriteria = criteria.createCriteria("feature", LEFT_OUTER_JOIN);
        Criteria procedureCriteria = criteria.createCriteria("procedure", LEFT_OUTER_JOIN);

        if (hasTranslation(query, I18nFeatureEntity.class)) {
            featureCriteria = query.addLocaleTo(featureCriteria, I18nFeatureEntity.class);
        }
        featureCriteria.add(Restrictions.ilike("name", "%" + query.getSearchTerm() + "%"));
        series.addAll(featureCriteria.list());

        if (hasTranslation(query, I18nProcedureEntity.class)) {
            procedureCriteria = query.addLocaleTo(procedureCriteria, I18nProcedureEntity.class);
        }
        procedureCriteria.add(Restrictions.ilike("name", "%" + query.getSearchTerm() + "%"));
        series.addAll(procedureCriteria.list());

        return series;
    }

    @Override
    public T getInstance(Long key, DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .add(eq("pkid", key));
        addIgnoreNonPublishedSeriesTo(criteria);
        return entityType.cast(criteria.uniqueResult());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = session.createCriteria(entityType, "series");
        addIgnoreNonPublishedSeriesTo(criteria, "series");
        criteria.createCriteria("procedure")
                .add(eq("reference", false));

        DetachedCriteria filter = parameters.createDetachedFilterCriteria("pkid");
        criteria.add(Subqueries.propertyIn("series.pkid", filter));

        parameters.addPagingTo(criteria);

        if (parameters.isPureStationInsituConcept()) {
            criteria
                    .createCriteria("feature", "f", JoinType.INNER_JOIN)
                    .add(Restrictions.eqOrIsNull("f.featureConcept", "stationary/insitu"));
        }

        return (List<T>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<T> getInstancesWith(FeatureEntity feature) {
        Criteria criteria = session.createCriteria(entityType, "s");
        addIgnoreNonPublishedSeriesTo(criteria, "s");
        criteria.createCriteria("feature", LEFT_OUTER_JOIN)
                .add(eq(COLUMN_PKID, feature.getPkid()));
        return (List<T>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<T> getInstancesWith(PlatformEntity platform) {
        Criteria criteria = session.createCriteria(entityType, "s");
        addIgnoreNonPublishedSeriesTo(criteria, "s");
        criteria.createCriteria("procedure", LEFT_OUTER_JOIN)
                .add(eq(COLUMN_PKID, platform.getPkid()));
        return (List<T>) criteria.list();
    }

    @Override
    public int getCount() throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .setProjection(Projections.rowCount());
        return criteria != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
    }

    private Criteria addIgnoreNonPublishedSeriesTo(Criteria criteria) {
        return addIgnoreNonPublishedSeriesTo(criteria, null);
    }

    // TODO
    private Criteria addIgnoreNonPublishedSeriesTo(Criteria criteria, String alias) {
        alias = alias == null ? "" : alias + ".";
        criteria.add(Restrictions.and(
                Restrictions.and(
                        Restrictions.isNotNull(alias + "firstValue"),
                        Restrictions.isNotNull(alias + "lastValue")),
                Restrictions.eq(alias + "published", true)));
        return criteria;
    }

    @Override
    protected Criteria getDefaultCriteria() {
        return session.createCriteria(AbstractSeriesEntity.class);
    }

}
