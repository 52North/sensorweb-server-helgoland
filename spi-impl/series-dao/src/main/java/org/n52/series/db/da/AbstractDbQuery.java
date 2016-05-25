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

package org.n52.series.db.da;

import static org.hibernate.criterion.Restrictions.between;
import static org.hibernate.criterion.Restrictions.isNull;
import static org.hibernate.criterion.Restrictions.like;
import static org.hibernate.criterion.Restrictions.or;
import static org.n52.series.db.da.beans.DataModelUtil.isEntitySupported;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.spatial.criterion.SpatialRestrictions;
import org.hibernate.sql.JoinType;
import org.joda.time.Interval;
import org.n52.io.crs.BoundingBox;
import org.n52.io.crs.CRSUtils;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.v1.ext.ObservationType;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

public abstract class AbstractDbQuery {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDbQuery.class);

    protected static final String COLUMN_KEY = "pkid";

    private static final String COLUMN_LOCALE = "locale";

    private static final String COLUMN_TIMESTAMP = "timestamp";

    private IoParameters parameters = IoParameters.createDefaults();

    private String sridAuthorityCode = "EPSG:4326"; // default

    protected AbstractDbQuery(IoParameters parameters) {
        if (parameters != null) {
            this.parameters = parameters;
        }
    }

    public abstract DetachedCriteria createDetachedFilterCriteria(String propertyName);

    public void setDatabaseAuthorityCode(String code) {
        this.sridAuthorityCode = code;
    }

    public String getHrefBase() {
        return parameters.getAsString(Parameters.HREF_BASE);
    }

    public String getLocale() {
        return parameters.getLocale();
    }

    public String getSearchTerm() {
        return parameters.getAsString(Parameters.SEARCH_TERM);
    }

    public Interval getTimespan() {
        return parameters.getTimespan().toInterval();
    }

    public BoundingBox getSpatialFilter() {
        return parameters.getSpatialFilter();
    }

    public boolean checkTranslationForLocale(Criteria criteria) {
        return !criteria.add(Restrictions.like(COLUMN_LOCALE, getCountryCode())).list().isEmpty();
    }

    public Criteria addLocaleTo(Criteria criteria, Class< ? > clazz) {
        if (getLocale() != null && isEntitySupported(clazz, criteria)) {
            Criteria translations = criteria.createCriteria("translations", JoinType.LEFT_OUTER_JOIN);
            criteria = translations.add(or(like(COLUMN_LOCALE, getCountryCode()), isNull(COLUMN_LOCALE)));
        }
        return criteria;
    }

    private String getCountryCode() {
        return getLocale().split("_")[0];
    }

    public Criteria addPagingTo(Criteria criteria) {
        if (parameters.getLimit() > 0) {
            criteria.setMaxResults(parameters.getLimit());
        }
        if (parameters.getOffset() > 0) {
            criteria.setFirstResult(parameters.getOffset());
        }
        return criteria;
    }

    public Criteria addTimespanTo(Criteria criteria) {
        if (parameters.getTimespan() != null) {
            Interval interval = parameters.getTimespan().toInterval();
            Date start = interval.getStart().toDate();
            Date end = interval.getEnd().toDate();
            criteria.add(between(COLUMN_TIMESTAMP, start, end));
        }
        return criteria;
    }

    public Criteria backwardCompatibleWithPureStationConcept(Criteria criteria, String parameter) {
        if (isPureStationInsituConcept()) {
            filterMobileInsitu(parameter, criteria, false, true);
        }
        return criteria;
    }

    public void filterMobileInsitu(String parameter, Criteria criteria, boolean mobile, boolean insitu) {
        DetachedCriteria c = DetachedCriteria.forClass(AbstractSeriesEntity.class, "series")
                .createCriteria("procedure", "p")
                .add(Restrictions.and(Restrictions.eq("p.mobile",mobile),
                                      Restrictions.eq("p.insitu",insitu)));
        c.setProjection(Projections.projectionList()
                        .add(Projections.property(String.format("series.%s.pkid", parameter))));
        criteria.add(Subqueries.propertyIn(String.format("%s.pkid", parameter), c));
    }

    public boolean isPureStationInsituConcept() {
        return parameters.containsParameter(Parameters.PURE_STATION_INSITU_CONCEPT)
                && parameters.getAsBoolean(Parameters.PURE_STATION_INSITU_CONCEPT);
    }

    public boolean isMobileConcept() {
        return isAllConcepts()
                || parameters.containsParameter(Parameters.PLATFORMS_INCLUDE_MOBILE)
                && parameters.getAsBoolean(Parameters.PLATFORMS_INCLUDE_MOBILE);
    }

    public boolean isStationaryConcept() {
        return isAllConcepts()
                || parameters.containsParameter(Parameters.PLATFORMS_INCLUDE_STATIONARY)
                && parameters.getAsBoolean(Parameters.PLATFORMS_INCLUDE_STATIONARY);
    }

    public boolean isInsituConcept() {
        return isAllConcepts()
                || parameters.containsParameter(Parameters.PLATFORMS_INCLUDE_INSITU)
                && parameters.getAsBoolean(Parameters.PLATFORMS_INCLUDE_INSITU);
    }

    public boolean isRemoteConcept() {
        return isAllConcepts()
                || parameters.containsParameter(Parameters.PLATFORMS_INCLUDE_REMOTE)
                && parameters.getAsBoolean(Parameters.PLATFORMS_INCLUDE_REMOTE);
    }

    public boolean isAllConcepts() {
        return parameters.containsParameter(Parameters.PLATFORMS_INCLUDE_ALL)
                && parameters.getAsBoolean(Parameters.PLATFORMS_INCLUDE_ALL);
    }
    
    public boolean hasObservationType() {
        return parameters.containsParameter(Parameters.OBSERVATION_TYPE);
    }
    
    public ObservationType getObservationType() {
        String observationType = parameters.containsParameter(Parameters.OBSERVATION_TYPE)
            ? parameters.getAsString(Parameters.OBSERVATION_TYPE)
            : null;
        try {
            return observationType != null
                ? ObservationType.toInstance(observationType)
                : ObservationType.MEASUREMENT;
        }
        catch (IllegalArgumentException e) {
            LOGGER.debug("unknown observation type: {}", observationType);
            throw new ResourceNotFoundException("Could not find resource under observation type '" + observationType + "'.");
        }
    }

    /**
     * @param id
     *        the id string to parse.
     * @return the long value of given string or {@link Long#MIN_VALUE} if string could not be parsed to type
     *         long.
     */
    public Long parseToId(String id) {
        try {
            return Long.parseLong(id);
        }
        catch (NumberFormatException e) {
            return Long.MIN_VALUE;
        }
    }

    public Set<Long> parseToIds(Set<String> ids) {
        Set<Long> parsedIds = new HashSet<>(ids.size());
        for (String id : ids) {
            parsedIds.add(parseToId(id));
        }
        return parsedIds;
    }

    public Criteria addSpatialFilterTo(Criteria criteria, AbstractDbQuery parameters) {
        BoundingBox spatialFilter = parameters.getSpatialFilter();
        if (spatialFilter != null) {
            try {
                CRSUtils crsUtils = CRSUtils.createEpsgForcedXYAxisOrder();
                int databaseSrid = crsUtils.getSrsIdFrom(sridAuthorityCode);
                Point ll = (Point) crsUtils.transformInnerToOuter(spatialFilter.getLowerLeft(), sridAuthorityCode);
                Point ur = (Point) crsUtils.transformInnerToOuter(spatialFilter.getUpperRight(), sridAuthorityCode);
                Envelope envelope = new Envelope(ll.getCoordinate(), ur.getCoordinate());
                criteria.add(SpatialRestrictions.filter("geom", envelope, databaseSrid));
            }
            catch (FactoryException e) {
                LOGGER.error("Could not create transformation facilities.", e);
            }
            catch (TransformException e) {
                LOGGER.error("Could not perform transformation.", e);
            }
        }
        return criteria;
    }

    public IoParameters getParameters() {
        return parameters;
    }

}
