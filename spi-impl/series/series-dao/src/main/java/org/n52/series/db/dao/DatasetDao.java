/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Restrictions;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.I18nFeatureEntity;
import org.n52.series.db.beans.I18nProcedureEntity;
import org.n52.series.db.beans.PlatformEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SuppressWarnings("rawtypes") // infer entitType runtime
public class DatasetDao<T extends DatasetEntity> extends AbstractDao<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetDao.class);

    private static final String COLUMN_PKID = "pkid";

    private final Class<T> entityType;

    public DatasetDao(Session session, Class<T> clazz) {
        super(session);
        this.entityType = clazz;//(Class<T>) AbstractSeriesEntity.class;
    }

    @SuppressWarnings("unchecked")
    public DatasetDao(Session session) {
        super(session);
        this.entityType = (Class<T>) DatasetEntity.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> find(DbQuery query) {
        LOGGER.debug("find entities: {}", query);

        List<T> series = new ArrayList<>();
        String searchTerm = "%" + query.getSearchTerm() + "%";

        /*
         * Timeseries labels are constructed from labels of related feature
         * and phenomenon. Therefore we have to join both tables and search
         * for given pattern on any of the stored labels.
         */

        Criteria criteria = addIgnoreUnpublishedSeriesTo(getDefaultCriteria("s"), "s");
        Criteria featureCriteria = criteria.createCriteria("feature", LEFT_OUTER_JOIN);
        series.addAll(translate(I18nFeatureEntity.class, featureCriteria, query)
                      .add(Restrictions.ilike("name", searchTerm)).list());

        Criteria procedureCriteria = criteria.createCriteria("procedure", LEFT_OUTER_JOIN);
        series.addAll(translate(I18nProcedureEntity.class, procedureCriteria, query)
                      .add(Restrictions.ilike("name", searchTerm)).list());

        Criteria phenomenonCriteria = criteria.createCriteria("phenomenon", LEFT_OUTER_JOIN);
        series.addAll(translate(I18nProcedureEntity.class, phenomenonCriteria, query)
                      .add(Restrictions.ilike("name", searchTerm)).list());

        return series;
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<T> getAllInstances(DbQuery parameters) throws DataAccessException {
        LOGGER.debug("get all instances: {}", parameters);
        Criteria criteria = getDefaultCriteria("series");
        Criteria procedureCreateria = criteria.createCriteria("procedure");
        procedureCreateria.add(eq("reference", false));
        return (List<T>) parameters.addFilters(criteria, getSeriesProperty()).list();
    }

    @Override
    protected String getSeriesProperty() {
        return "";//COLUMN_PKID;
    }

    @SuppressWarnings("unchecked")
    public List<T> getInstancesWith(FeatureEntity feature) {
        LOGGER.debug("get instance for feature '{}'", feature);
        Criteria criteria = getDefaultCriteria("series");
        criteria.createCriteria("feature", LEFT_OUTER_JOIN)
                .add(eq(COLUMN_PKID, feature.getPkid()));
        return (List<T>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<T> getInstancesWith(PlatformEntity platform) {
        LOGGER.debug("get instance for platform '{}'", platform);
        Criteria criteria = getDefaultCriteria("series");
        criteria.createCriteria("procedure", LEFT_OUTER_JOIN)
                .add(eq(COLUMN_PKID, platform.getPkid()));
        return (List<T>) criteria.list();
    }

    @Override
    protected Class<T> getEntityClass() {
        return entityType;
    }

    @Override
    protected Criteria getDefaultCriteria() {
        return getDefaultCriteria("series");
    }

    @Override
    protected Criteria getDefaultCriteria(String alias) {
       Criteria criteria = entityType != null
            ? super.getDefaultCriteria(alias)
            : session.createCriteria(DatasetEntity.class, alias);
        addIgnoreUnpublishedSeriesTo(criteria, alias);
        return criteria;
    }

    private Criteria addIgnoreUnpublishedSeriesTo(Criteria criteria, String alias) {
        alias = prepareForConcatenation(alias);
        criteria.add(Restrictions.and(
                createNotNullFirstLastValueRestriction(alias),
                createPublishedAndNotDeletedRestriction(alias)));
        return criteria;
    }

    private Criterion createPublishedAndNotDeletedRestriction(String alias) {
        return Restrictions.and(
                Restrictions.eq(alias.concat("published"), true),
                Restrictions.eqOrIsNull(alias.concat("deleted"), false));
    }

    private LogicalExpression createNotNullFirstLastValueRestriction(String alias) {
        return Restrictions.and(
                Restrictions.isNotNull(alias.concat("firstValueAt")),
                Restrictions.isNotNull(alias.concat("lastValueAt")));
    }

    private String prepareForConcatenation(String alias) {
        return (alias == null || alias.isEmpty()) ? "" : alias.concat(".");
    }

}
