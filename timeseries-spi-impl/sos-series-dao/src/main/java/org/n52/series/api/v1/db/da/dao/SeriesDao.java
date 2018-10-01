/**
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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
import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.n52.io.IoParameters;
import org.n52.series.api.v1.db.da.DataAccessException;
import org.n52.series.api.v1.db.da.DbQuery;
import org.n52.series.api.v1.db.da.beans.FeatureEntity;
import org.n52.series.api.v1.db.da.beans.I18nFeatureEntity;
import org.n52.series.api.v1.db.da.beans.I18nOfferingEntity;
import org.n52.series.api.v1.db.da.beans.I18nProcedureEntity;
import org.n52.series.api.v1.db.da.beans.ObservationEntity;
import org.n52.series.api.v1.db.da.beans.SeriesEntity;

public class SeriesDao extends AbstractDao<SeriesEntity> {

    private static final String COLUMN_PKID = "pkid";

    public SeriesDao(Session session) {
        super(session);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SeriesEntity> find(String search, DbQuery query) {

        /*
         * Timeseries labels are constructed from labels of related feature
         * and phenomenon. Therefore we have to join both tables and search
         * for given pattern on any of the stored labels.
         */

        List<SeriesEntity> series = new ArrayList<>();
        Criteria criteria = getDefaultCriteria("series");
        Criteria featureCriteria = criteria.createCriteria("feature", LEFT_OUTER_JOIN);

        if (hasTranslation(query, I18nFeatureEntity.class)) {
            featureCriteria = query.addLocaleTo(featureCriteria, I18nFeatureEntity.class);
        }
        featureCriteria.add(Restrictions.ilike("name", "%" + search + "%"));
        series.addAll(featureCriteria.list());

        // reset criteria
        criteria = getDefaultCriteria("series");
        Criteria procedureCriteria = criteria.createCriteria("procedure", LEFT_OUTER_JOIN);
        if (hasTranslation(query, I18nProcedureEntity.class)) {
            procedureCriteria = query.addLocaleTo(procedureCriteria, I18nProcedureEntity.class);
        }
        procedureCriteria.add(Restrictions.ilike("name", "%" + search + "%"));
        series.addAll(procedureCriteria.list());

        // reset criteria
        criteria = getDefaultCriteria("series");
        Criteria offeringCriteria = criteria.createCriteria("offering", LEFT_OUTER_JOIN);
        if (hasTranslation(query, I18nOfferingEntity.class)) {
            offeringCriteria = query.addLocaleTo(offeringCriteria, I18nOfferingEntity.class);
        }
        offeringCriteria.add(Restrictions.ilike("name", "%" + search + "%"));
        series.addAll(offeringCriteria.list());
        return series;
    }

    @Override
    public SeriesEntity getInstance(Long key) throws DataAccessException {
        return getInstance(key, DbQuery.createFrom(IoParameters.createDefaults()));
    }

    @Override
    public SeriesEntity getInstance(Long key, DbQuery query) throws DataAccessException {
        return (SeriesEntity) getDefaultCriteria("series", false, query)
                .add(eq(COLUMN_PKID, key))
                .uniqueResult();
    }

    @Override
    public List<SeriesEntity> getAllInstances() throws DataAccessException {
        return getAllInstances(DbQuery.createFrom(IoParameters.createDefaults()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SeriesEntity> getAllInstances(DbQuery query) throws DataAccessException {
        Criteria criteria = getDefaultCriteria("series", query);
        criteria = query.addDetachedFilters("", criteria);
        query.addPagingTo(criteria);
        return criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<SeriesEntity> getInstancesWith(FeatureEntity feature) {
        Criteria criteria = getDefaultCriteria("series")
                .createAlias("feature", "f")
                .add(Restrictions.eq("f.pkid", feature.getPkid()));
//        criteria.add(eq("feature.pkid", feature.getPkid()));
        return criteria.list();
    }


    public ObservationEntity getClosestOuterPreviousValue(final SeriesEntity dataset, final DateTime lowerBound, final DbQuery query) {
        String column = "timestamp";
        final Order order = Order.desc(column);
        final Criteria criteria = createDataCriteria(dataset, query, order);
        return (ObservationEntity) criteria.add(Restrictions.lt(column, lowerBound.toDate()))
                           .setMaxResults(1)
                           .uniqueResult();
    }

    public ObservationEntity getClosestOuterNextValue(final SeriesEntity dataset, final DateTime upperBound, final DbQuery query) {
        String column = "timestamp";
        final Order order = Order.asc(column);
        final Criteria criteria = createDataCriteria(dataset, query, order);
        return (ObservationEntity) criteria.add(Restrictions.gt(column, upperBound.toDate()))
                           .setMaxResults(1)
                           .uniqueResult();
    }

    private Criteria createDataCriteria(SeriesEntity dataset, DbQuery query, Order order) {
        return session.createCriteria(ObservationEntity.class)
                      .add(Restrictions.eq("seriesPkid", dataset.getPkid()))
                      .addOrder(order);
    }

    @Override
    public int getCount() throws DataAccessException {
        Criteria criteria = getDefaultCriteria("series")
                .setProjection(Projections.rowCount());
        return criteria != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
    }

    @Override
    protected String getDefaultAlias() {
        return "series";
    }

    @Override
    protected Class<?> getEntityClass() {
        return SeriesEntity.class;
    }

    @Override
    protected Criteria getDefaultCriteria(String alias, DbQuery query) {
        return getDefaultCriteria(alias, true, query);
    }

    private Criteria getDefaultCriteria(String alias, boolean ignoreReferenceSeries, DbQuery query) {
        alias = alias != null ? alias : getDefaultAlias();
        Criteria criteria = session.createCriteria(getEntityClass(), alias)
                .createAlias("procedure", "p");
        if (ignoreReferenceSeries) {
            criteria.add(eq("p.reference", Boolean.FALSE));
        }
        addIgnoreNonPublishedSeriesTo(criteria, alias);
        return criteria;
    }

    private Criteria addIgnoreNonPublishedSeriesTo(Criteria criteria, String alias) {
        alias = alias == null ? "" : alias + ".";
        criteria.add(Restrictions.and(
                Restrictions.and(
                        Restrictions.isNotNull(alias + "firstValue"),
                        Restrictions.isNotNull(alias + "lastValue")),
                        Restrictions.eq(alias + "published", true),
                        Restrictions.eqOrIsNull(alias + "deleted", false)));
        return criteria;
    }

}
