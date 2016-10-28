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
import static org.hibernate.sql.JoinType.LEFT_OUTER_JOIN;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DatasetTEntity;
import org.n52.series.db.beans.FeatureTEntity;
import org.n52.series.db.beans.I18nFeatureEntity;
import org.n52.series.db.beans.I18nProcedureEntity;
import org.n52.series.db.beans.PlatformTEntity;
import org.n52.series.db.beans.ServiceTEntity;
import org.n52.series.db.beans.UnitTEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SuppressWarnings("rawtypes") // infer entitType runtime
public class DatasetDao<T extends DatasetTEntity> extends AbstractDao<T> implements InsertDao<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetDao.class);

    private static final String COLUMN_PKID = "pkid";
    private static final String COLUMN_SERVICE_PKID = "service.pkid";
    private static final String COLUMN_CATEGORY_PKID = "category.pkid";
    private static final String COLUMN_FEATURE_PKID = "feature.pkid";
    private static final String COLUMN_PROCEDURE_PKID = "procedure.pkid";
    private static final String COLUMN_PHENOMENON_PKID = "phenomenon.pkid";
    private static final String COLUMN_UNIT_PKID = "unit.pkid";

    private final Class<T> entityType;

    public DatasetDao(Session session, Class<T> clazz) {
        super(session);
        this.entityType = clazz;//(Class<T>) AbstractSeriesEntity.class;
    }

    @SuppressWarnings("unchecked")
    public DatasetDao(Session session) {
        super(session);
        this.entityType = (Class<T>) DatasetTEntity.class;
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
        Criteria criteria = addIgnoreNonPublishedSeriesTo(getDefaultCriteria("s"), "s");
        Criteria featureCriteria = criteria.createCriteria("feature", LEFT_OUTER_JOIN);
        series.addAll(translate(I18nFeatureEntity.class, featureCriteria, query)
                .add(Restrictions.ilike("name", searchTerm)).list());

        Criteria procedureCriteria = criteria.createCriteria("procedure", LEFT_OUTER_JOIN);
        series.addAll(translate(I18nProcedureEntity.class, procedureCriteria, query)
                .add(Restrictions.ilike("name", searchTerm)).list());

        return series;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> getAllInstances(DbQuery parameters) throws DataAccessException {
        LOGGER.debug("get all instances: {}", parameters);
        Criteria criteria = session.createCriteria(getEntityClass());
        return (List<T>) addFilters(criteria, parameters).list();
    }

    @Override
    protected String getSeriesProperty() {
        return "";//COLUMN_PKID;
    }

    @SuppressWarnings("unchecked")
    public List<T> getInstancesWith(FeatureTEntity feature) {
        LOGGER.debug("get instance for feature '{}'", feature);
        Criteria criteria = getDefaultCriteria("series");
        criteria.createCriteria("feature", LEFT_OUTER_JOIN)
                .add(eq(COLUMN_PKID, feature.getPkid()));
        return (List<T>) criteria.list();
    }

    @SuppressWarnings("unchecked")
    public List<T> getInstancesWith(PlatformTEntity platform) {
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
                : session.createCriteria(DatasetTEntity.class, alias);
        // addIgnoreNonPublishedSeriesTo(criteria, alias);
        return criteria;
    }

    private Criteria addIgnoreNonPublishedSeriesTo(Criteria criteria, String alias) {
        alias = prepareForConcatenation(alias);
        criteria.add(Restrictions.and(
                Restrictions.and(
                        Restrictions.isNotNull(alias.concat("firstValueAt")),
                        Restrictions.isNotNull(alias.concat("lastValueAt"))),
                Restrictions.eq(alias.concat("published"), true)));
        return criteria;
    }

    private String prepareForConcatenation(String alias) {
        return (alias == null || alias.isEmpty()) ? "" : alias.concat(".");
    }

    @Override
    public DatasetTEntity getOrInsertInstance(DatasetTEntity dataset) {
        if (dataset.getUnit() != null) {
            dataset.setUnit(getOrInsertUnit(dataset.getUnit()));
        }
        DatasetTEntity instance = getInstance(dataset);
        if (instance == null) {
            session.save(dataset);
            LOGGER.info("Save dataset: " + dataset);
            session.flush();
            session.refresh(dataset);
        } else {
            instance.setDeleted(Boolean.FALSE);
            session.update(instance);
            LOGGER.info("Mark dataset as undeleted: " + instance);
        }
        return dataset;
    }

    public UnitTEntity getOrInsertUnit(UnitTEntity unit) {
        UnitTEntity instance = getUnit(unit);
        if (instance == null) {
            this.session.save(unit);
            instance = unit;
        }
        return instance;
    }

    public void markAsDeletedForService(ServiceTEntity service) {
        List<T> datasets = getDatasetsForService(service);
        datasets.stream().map((dataset) -> {
            dataset.setDeleted(Boolean.TRUE);
            return dataset;
        }).forEach((dataset) -> {
            session.saveOrUpdate(dataset);
            LOGGER.info("Mark dataset as deleted: " + dataset);
        });
    }

    public void removeDeletedForService(ServiceTEntity service) {
        List<T> datasets = getDeletedMarkDatasets(service);
        datasets.forEach((dataset) -> {
            session.delete(dataset);
            LOGGER.info("Delete dataset: " + dataset);
        });
        session.flush();
    }

    private UnitTEntity getUnit(UnitTEntity unit) {
        Criteria criteria = session.createCriteria(UnitTEntity.class)
                .add(Restrictions.eq("name", unit.getName()))
                .add(Restrictions.eq(COLUMN_SERVICE_PKID, unit.getService().getPkid()));
        return (UnitTEntity) criteria.uniqueResult();
    }

    private DatasetTEntity getInstance(DatasetTEntity dataset) {
        Criteria criteria = getDefaultCriteria()
                .add(Restrictions.eq("datasetType", dataset.getDatasetType()))
                .add(Restrictions.eq(COLUMN_CATEGORY_PKID, dataset.getCategory().getPkid()))
                .add(Restrictions.eq(COLUMN_FEATURE_PKID, dataset.getFeature().getPkid()))
                .add(Restrictions.eq(COLUMN_PROCEDURE_PKID, dataset.getProcedure().getPkid()))
                .add(Restrictions.eq(COLUMN_PHENOMENON_PKID, dataset.getPhenomenon().getPkid()))
                .add(Restrictions.eq(COLUMN_SERVICE_PKID, dataset.getService().getPkid()));
        if (dataset.getUnit() != null) {
            criteria.add(Restrictions.eq(COLUMN_UNIT_PKID, dataset.getUnit().getPkid()));
        }
        return (T) criteria.uniqueResult();
    }

    private List<T> getDatasetsForService(ServiceTEntity service) {
        Criteria criteria = getDefaultCriteria()
                .add(Restrictions.eq(COLUMN_SERVICE_PKID, service.getPkid()));
        return criteria.list();
    }

    private List<T> getDeletedMarkDatasets(ServiceTEntity service) {
        Criteria criteria = getDefaultCriteria()
                .add(Restrictions.eq(COLUMN_SERVICE_PKID, service.getPkid()))
                .add(Restrictions.eq("deleted", Boolean.TRUE));
        return criteria.list();
    }

}
