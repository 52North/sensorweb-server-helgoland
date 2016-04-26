/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.series.db.da.dao.v2;

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
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.I18nFeatureEntity;
import org.n52.series.db.da.beans.I18nProcedureEntity;
import org.n52.series.db.da.beans.v2.SeriesEntityV2;
import org.n52.series.db.da.v2.DbQuery;

public class SeriesDao extends AbstractDao<SeriesEntityV2> {

    private static final String COLUMN_PKID = "pkid";

    public SeriesDao(Session session) {
        super(session);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SeriesEntityV2> find(String search, DbQuery query) {

        /*
         * Timeseries labels are constructed from labels of related feature
         * and phenomenon. Therefore we have to join both tables and search
         * for given pattern on any of the stored labels.
         */

        List<SeriesEntityV2> series = new ArrayList<SeriesEntityV2>();
        Criteria criteria = addIgnoreNonPublishedSeriesTo(getDefaultCriteria());
        Criteria featureCriteria = criteria.createCriteria("feature", LEFT_OUTER_JOIN);
        Criteria procedureCriteria = criteria.createCriteria("procedure", LEFT_OUTER_JOIN);

        if (hasTranslation(query, I18nFeatureEntity.class)) {
            featureCriteria = query.addLocaleTo(featureCriteria, I18nFeatureEntity.class);
        }
        featureCriteria.add(Restrictions.ilike("name", "%" + search + "%"));
        series.addAll(featureCriteria.list());

        if (hasTranslation(query, I18nProcedureEntity.class)) {
            procedureCriteria = query.addLocaleTo(procedureCriteria, I18nProcedureEntity.class);
        }
        procedureCriteria.add(Restrictions.ilike("name", "%" + search + "%"));
        series.addAll(procedureCriteria.list());

        return series;
    }

//    @Override
//    public SeriesEntityV2 getInstance(Long key) throws DataAccessException {
//        return getInstance(key, DbQueryV1.createFrom(IoParameters.createDefaults()));
//    }

    @Override
    public SeriesEntityV2 getInstance(Long key, DbQuery parameters) throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .add(eq("pkid", key));
        addIgnoreNonPublishedSeriesTo(criteria);
        
        DetachedCriteria filter = parameters.createDetachedFilterCriteria("pkid");
        criteria.add(Subqueries.propertyIn(COLUMN_PKID, filter));
        
        return (SeriesEntityV2) criteria.uniqueResult();
    }
    
//    @Override
//    public List<SeriesEntityV2> getAllInstances() throws DataAccessException {
//        return getAllInstances(DbQueryV1.createFrom(IoParameters.createDefaults()));
//    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SeriesEntityV2> getAllInstances(DbQuery parameters) throws DataAccessException {
        Criteria criteria = session.createCriteria(SeriesEntityV2.class, "s");
        addIgnoreNonPublishedSeriesTo(criteria, "s");
        criteria.createCriteria("procedure")
                .add(eq("reference", false));

        DetachedCriteria filter = parameters.createDetachedFilterCriteria("pkid");
        criteria.add(Subqueries.propertyIn(COLUMN_PKID, filter));

        parameters.addPagingTo(criteria);
        return (List<SeriesEntityV2>) criteria.list();
    }

//    @SuppressWarnings("unchecked")
//    public List<SeriesEntityV2> getInstancesWith(SiteEntity feature) {
//        Criteria criteria = session.createCriteria(SeriesEntityV2.class, "s");
//        addIgnoreNonPublishedSeriesTo(criteria, "s");
//        criteria.createCriteria("feature", LEFT_OUTER_JOIN)
//                .add(eq(COLUMN_PKID, feature.getPkid()));
//        return (List<SeriesEntityV2>) criteria.list();
//    }
//    
//    @SuppressWarnings("unchecked")
//    public List<SeriesEntityV2> getInstancesWith(TrackEntity feature) {
////        Criteria criteria = session.createCriteria(SeriesEntityV2.class, "s");
////        addIgnoreNonPublishedSeriesTo(criteria, "s");
////        criteria.createCriteria("feature", LEFT_OUTER_JOIN)
////                .add(eq(COLUMN_PKID, feature.getPkid()));
////        return (List<SeriesEntityV2>) criteria.list();
//    	// TODO
//        return null;
//    }

    @Override
    public int getCount() throws DataAccessException {
        Criteria criteria = getDefaultCriteria()
                .setProjection(Projections.rowCount());
        return criteria != null ? ((Long) criteria.uniqueResult()).intValue() : 0;
    }

    private Criteria addIgnoreNonPublishedSeriesTo(Criteria criteria) {
        return addIgnoreNonPublishedSeriesTo(criteria, null);
    }

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
    	return session.createCriteria(SeriesEntityV2.class);
    }

}
