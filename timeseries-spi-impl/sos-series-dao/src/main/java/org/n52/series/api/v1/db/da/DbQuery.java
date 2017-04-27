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
package org.n52.series.api.v1.db.da;

import static org.hibernate.criterion.Restrictions.between;
import static org.hibernate.criterion.Restrictions.isNull;
import static org.hibernate.criterion.Restrictions.like;
import static org.hibernate.criterion.Restrictions.or;
import static org.hibernate.criterion.Subqueries.propertyIn;
import static org.n52.series.api.v1.db.da.beans.DataModelUtil.isEntitySupported;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.spatial.criterion.SpatialRestrictions;
import org.hibernate.sql.JoinType;
import org.joda.time.Interval;
import org.n52.io.IoParameters;
import org.n52.io.crs.BoundingBox;
import org.n52.io.crs.CRSUtils;
import org.n52.series.api.v1.db.da.beans.SeriesEntity;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;

public class DbQuery {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbQuery.class);

    private static final String COLUMN_KEY = "pkid";

    private static final String COLUMN_LOCALE = "locale";

    private static final String COLUMN_TIMESTAMP = "timestamp";

    private IoParameters parameters = IoParameters.createDefaults();

    private String sridAuthorityCode = "EPSG:4326"; // default

    private DbQuery(IoParameters parameters) {
        if (parameters != null) {
            this.parameters = parameters;
        }
    }

    public IoParameters getParameters() {
        return parameters;
    }

    public void setDatabaseAuthorityCode(String code) {
        this.sridAuthorityCode = code;
    }

    public String getLocale() {
        return parameters.getLocale();
    }

    public Interval getTimespan() {
        return parameters.getTimespan().toInterval();
    }

    public BoundingBox getSpatialFilter() {
        return parameters.getSpatialFilter();
    }

    public boolean checkTranslationForLocale(Criteria criteria) {
        return criteria.add(Restrictions.like(COLUMN_LOCALE, getCountryCode())).list().size() != 0;
    }

    public Criteria addLocaleTo(Criteria criteria, Class< ? > clazz) {
        if (getLocale() != null && isEntitySupported(clazz, criteria)) {
            criteria = criteria.createCriteria("translations", JoinType.LEFT_OUTER_JOIN)
                    .add(or(like(COLUMN_LOCALE, getCountryCode()),
                            isNull(COLUMN_LOCALE)));
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
    
    public Criteria addDetachedFilters(String propertyName, Criteria criteria) {
        
        if ( !parameters.hasFilterParameters()) {
            return criteria;
        }
        
        String projectionProperty = propertyName != null && !propertyName.isEmpty()
                        ? propertyName
                        : "pkid";
        DetachedCriteria filter = DetachedCriteria.forClass(SeriesEntity.class)
                .setProjection(Property.forName(projectionProperty));

        filterWithSingularParameters(filter); // stay backwards compatible
        addFilterRestriction(parameters.getPhenomena(), "phenomenon", filter);
        addFilterRestriction(parameters.getProcedures(), "procedure", filter);
        addFilterRestriction(parameters.getOfferings(), "offering", filter);
        addFilterRestriction(parameters.getFeatures(), "feature", filter);
        addFilterRestriction(parameters.getStations(), "feature", filter);
        addFilterRestriction(parameters.getCategories(), "category", filter);
        addFilterRestriction(parameters.getTimeseries(), filter);

        String filterProperty = propertyName != null && !propertyName.isEmpty()
                ? propertyName + ".pkid"
                : "pkid";
        criteria.add(propertyIn(filterProperty, filter));
        return criteria;
    }
    
    private DetachedCriteria addFilterRestriction(Set<String> values, DetachedCriteria filter) {
        return addFilterRestriction(values, null, filter);
    }
    
    private DetachedCriteria addFilterRestriction(Set<String> values, String entity, DetachedCriteria filter) {
        if (hasValues(values)) {
            Criterion restriction = createIdCriterion(values);
            if (entity == null || entity.isEmpty()) {
                return filter.add(restriction);
            } else {
                // return subquery for further chaining
                return filter.createCriteria(entity).add(restriction);
            }
        }
        return filter;
    }
    
    private boolean hasValues(Set<String> values) {
        return values != null && !values.isEmpty();
    }
    
    private Criterion createIdCriterion(Set<String> values) {
        return createIdCriterion(values, null);
    }

    private Criterion createIdCriterion(Set<String> values, String alias) {
        return createIdFilter(values, alias);
    }
    
    private Criterion createIdFilter(Set<String> filterValues, String alias) {
        String column = alias != null
                ? alias + "." + COLUMN_KEY
                : COLUMN_KEY;
        return Restrictions.in(column, parseToIds(filterValues));
    }
    
    public Set<Long> parseToIds(Set<String> ids) {
        Set<Long> parsedIds = new HashSet<>();
        for (String id : ids) {
            parsedIds.add(parseToId(id));
        }
        return parsedIds;
    }
    
    @Deprecated
    public void filterWithSingularParameters(DetachedCriteria filter) {
        // old query parameter to stay backward compatible
        if (parameters.getPhenomenon() != null) {
            filter.createCriteria("phenomenon")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(parameters.getPhenomenon())));
        }
        if (parameters.getProcedure() != null) {
            filter.createCriteria("procedure")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(parameters.getProcedure())));
        }
        if (parameters.getOffering() != null) {
            filter.createCriteria("offering")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(parameters.getOffering())));
        }
        if (parameters.getFeature() != null) {
            filter.createCriteria("feature")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(parameters.getFeature())));
        }
        if (parameters.getStation() != null) {
            // here feature == station
            filter.createCriteria("feature")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(parameters.getStation())));
        }
        if (parameters.getCategory() != null) {
            filter.createCriteria("category")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(parameters.getCategory())));
        }
    }

    /**
     * @param id
     *        the id string to parse.
     * @return the long value of given string or {@link Long#MIN_VALUE} if string could not be parsed to type
     *         long.
     */
    private Long parseToId(String id) {
        try {
            return Long.parseLong(id);
        }
        catch (NumberFormatException e) {
            return Long.MIN_VALUE;
        }
    }

    public Criteria addSpatialFilterTo(Criteria criteria, DbQuery parameters) {
        BoundingBox spatialFilter = parameters.getSpatialFilter();
        if (spatialFilter != null) {
            try {
                CRSUtils crsUtils = CRSUtils.createEpsgForcedXYAxisOrder();
                int databaseSrid = crsUtils.getSrsIdFrom(sridAuthorityCode);
                Point ll = crsUtils.transformInnerToOuter(spatialFilter.getLowerLeft(), sridAuthorityCode);
                Point ur = crsUtils.transformInnerToOuter(spatialFilter.getUpperRight(), sridAuthorityCode);
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parameters == null)
                ? 0
                : parameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DbQuery other = (DbQuery) obj;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        return true;
    }

    public static DbQuery createFrom(IoParameters parameters) {
        return new DbQuery(parameters);
    }

}
