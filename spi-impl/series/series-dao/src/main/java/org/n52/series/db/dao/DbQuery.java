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

import static java.lang.String.format;
import static org.hibernate.criterion.DetachedCriteria.forClass;
import static org.hibernate.criterion.Projections.projectionList;
import static org.hibernate.criterion.Projections.property;
import static org.hibernate.criterion.Restrictions.between;
import static org.hibernate.criterion.Restrictions.isNull;
import static org.hibernate.criterion.Restrictions.like;
import static org.hibernate.criterion.Restrictions.or;
import static org.hibernate.criterion.Subqueries.propertyIn;
import static org.n52.io.request.Parameters.HANDLE_AS_DATASET_TYPE;
import static org.n52.series.db.DataModelUtil.isEntitySupported;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Restrictions;
import org.hibernate.spatial.criterion.SpatialRestrictions;
import org.hibernate.sql.JoinType;
import org.joda.time.Interval;
import org.n52.io.crs.BoundingBox;
import org.n52.io.crs.CRSUtils;
import org.n52.io.request.FilterResolver;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.PlatformType;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.PlatformEntity;
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

    private static final String COLUMN_DOMAIN_ID = "domainId";

    private static final String COLUMN_TIMESTAMP = "timestamp";

    private IoParameters parameters = IoParameters.createDefaults();

    private String sridAuthorityCode = "EPSG:4326"; // default

    protected DbQuery(IoParameters parameters) {
        if (parameters != null) {
            this.parameters = parameters;
        }
    }

    public static DbQuery createFrom(IoParameters parameters) {
        return new DbQuery(parameters);
    }

    public void setDatabaseAuthorityCode(String code) {
        this.sridAuthorityCode = code;
    }

    public String getHrefBase() {
        return parameters.getHrefBase();
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

    public boolean isExpanded() {
        return parameters.isExpanded();
    }

    public Set<String> getDatasetTypes() {
        return parameters.getDatasetTypes();
    }

    public boolean isSetDatasetTypeFilter() {
        return !parameters.getDatasetTypes().isEmpty();
    }

    public String getHandleAsDatasetTypeFallback() {
        return parameters.containsParameter(HANDLE_AS_DATASET_TYPE)
                ? parameters.getAsString(HANDLE_AS_DATASET_TYPE)
                : "measurement";
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

    public Criteria addTimespanTo(Criteria criteria) {
        if (parameters.getTimespan() != null) {
            Interval interval = parameters.getTimespan().toInterval();
            Date start = interval.getStart().toDate();
            Date end = interval.getEnd().toDate();
            criteria.add(between(COLUMN_TIMESTAMP, start, end));
        }
        return criteria;
    }

    /**
     * Adds an platform type filter to the query.
     *
     * @param parameter the parameter to filter on.
     * @param criteria the criteria to add the filter to.
     * @return the criteria to chain.
     */
    Criteria addPlatformTypeFilter(String parameter, Criteria criteria) {
        FilterResolver filterResolver = getFilterResolver();
        if ( !filterResolver.shallIncludeAllPlatformTypes()) {
            if (parameter == null || parameter.isEmpty()) {
                // series table itself
                criteria.createCriteria("platform")
                        .add(createMobileExpression(filterResolver))
                        .add(createInsituExpression(filterResolver));
            } else {
                // join parameter table via series table
                DetachedCriteria c = forClass(DatasetEntity.class, "series")
                        .createCriteria("procedure")
                        .add(createMobileExpression(filterResolver))
                        .add(createInsituExpression(filterResolver))
                        .setProjection(onPkidProjection(parameter));
                criteria.add(propertyIn(format("%s.pkid", parameter), c));
            }
        }
        return criteria;
    }

    Criteria addDatasetTypeFilter(String parameter, Criteria criteria) {
        Set<String> datasetTypes = getParameters().getDatasetTypes();
        if ( !datasetTypes.isEmpty()) {
            FilterResolver filterResolver = getFilterResolver();
            if (filterResolver.shallBehaveBackwardsCompatible() || !filterResolver.shallIncludeAllDatasetTypes()) {
                if (parameter == null || parameter.isEmpty()) {
                    // series table itself
                    criteria.add(Restrictions.in("datasetType", datasetTypes));
                } else {
                    // join parameter table with series table
                    DetachedCriteria filteredPkids = forClass(DatasetEntity.class, "series")
                            .add(Restrictions.in("datasetType", datasetTypes))
                            .setProjection(onPkidProjection(parameter));
                    criteria.add(propertyIn(format("%s.pkid", parameter), filteredPkids));
                }
            }
        }
        return criteria;
    }

    Criteria addLimitAndOffsetFilter(Criteria criteria) {
        if (getParameters().containsParameter(Parameters.OFFSET)) {
            criteria.setFirstResult(getParameters().getOffset());
        }
        if (getParameters().containsParameter(Parameters.LIMIT)) {
            criteria.setMaxResults(getParameters().getLimit());
        }
        return criteria;
    }

    private LogicalExpression createMobileExpression(FilterResolver filterResolver) {
        boolean includeStationary = filterResolver.shallIncludeStationaryPlatformTypes();
        boolean includeMobile = filterResolver.shallIncludeMobilePlatformTypes();
        return Restrictions.or(
                 Restrictions.eq(PlatformEntity.MOBILE, !includeStationary), // inverse to match filter
                 Restrictions.eq(PlatformEntity.MOBILE, includeMobile));
    }


    private LogicalExpression createInsituExpression(FilterResolver filterResolver) {
        boolean includeInsitu = filterResolver.shallIncludeInsituPlatformTypes();
        boolean includeRemote = filterResolver.shallIncludeRemotePlatformTypes();
        return Restrictions.or(
                 Restrictions.eq(PlatformEntity.INSITU, includeInsitu),
                 Restrictions.eq(PlatformEntity.INSITU, !includeRemote)); // inverse to match filter
    }

    private ProjectionList onPkidProjection(String parameter) {
        final String filterProperty = format("series.%s.pkid", parameter);
        return projectionList().add(property(filterProperty));
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

    public Set<Long> parseToIds(Set<String> ids) {
        Set<Long> parsedIds = new HashSet<>(ids.size());
        for (String id : ids) {
            parsedIds.add(parseToId(id));
        }
        return parsedIds;
    }

    public Criteria addSpatialFilterTo(Criteria criteria) {
        BoundingBox spatialFilter = parameters.getSpatialFilter();
        if (spatialFilter != null) {
            try {
                CRSUtils crsUtils = CRSUtils.createEpsgForcedXYAxisOrder();
                int databaseSrid = crsUtils.getSrsIdFrom(sridAuthorityCode);
                Point ll = (Point) crsUtils.transformInnerToOuter(spatialFilter.getLowerLeft(), sridAuthorityCode);
                Point ur = (Point) crsUtils.transformInnerToOuter(spatialFilter.getUpperRight(), sridAuthorityCode);
                Envelope envelope = new Envelope(ll.getCoordinate(), ur.getCoordinate());
                criteria.add(SpatialRestrictions.filter("geometry.geometry", envelope, databaseSrid));

                // TODO intersect with linestring

                // XXX do sampling filter only on generated line strings stored in FOI table,
                // otherwise we would have to check each observation row

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

    public DetachedCriteria createDetachedFilterCriteria(String propertyName) {
        DetachedCriteria filter = DetachedCriteria.forClass(DatasetEntity.class);

        filterWithSingularParmameters(filter); // stay backwards compatible

        addFilterRestriction(parameters.getPhenomena(), "phenomenon", filter);
        addFilterRestriction(parameters.getProcedures(), "procedure", filter);
        addFilterRestriction(parameters.getOfferings(), "procedure", filter); // here procedure == offering
        addFilterRestriction(parameters.getFeatures(), "feature", filter);
        addFilterRestriction(parameters.getCategories(), "category", filter);
        addFilterRestriction(parameters.getDatasets(), filter);
        addFilterRestriction(parameters.getSeries(), filter);

        if (hasValues(parameters.getPlatforms())) {
            Set<String> stationaryIds = getStationaryIds(parameters.getPlatforms());
            Set<String> mobileIds = getMobileIds(parameters.getPlatforms());
            if ( !stationaryIds.isEmpty()) {
                addFilterRestriction(stationaryIds, "feature", filter);
            }
            if ( !mobileIds.isEmpty()) {
                addFilterRestriction(mobileIds, "platform", filter);
            }
        }

        propertyName = propertyName != null
                && !propertyName.isEmpty()
                    ? propertyName
                    : "pkid";
        return filter.setProjection(projectionList().add(property(propertyName)));
    }

    private DetachedCriteria addFilterRestriction(Set<String> values, DetachedCriteria filter) {
        return addFilterRestriction(values, null, filter);
    }

    private DetachedCriteria addFilterRestriction(Set<String> values, String entity, DetachedCriteria filter) {
        if (hasValues(values)) {
            Criterion restriction = parameters.isMatchDomainIds()
                    ? createDomainIdFilter(values)
                    : createIdFilter(values);
            if (entity == null || entity.isEmpty()) {
                filter.add(restriction);
            } else {
                filter.createCriteria(entity).add(restriction);
            }
        }
        return filter;
    }

    private Criterion createDomainIdFilter(Set<String> filterValues) {
        return Restrictions.in(COLUMN_DOMAIN_ID, filterValues);
    }

    private Criterion createIdFilter(Set<String> filterValues) {
        return Restrictions.in(COLUMN_KEY, parseToIds(filterValues));
    }

    private boolean hasValues(Set<String> values) {
        return values != null && !values.isEmpty();
    }

    private Set<String> getStationaryIds(Set<String> platforms) {
        Set<String> set = new HashSet<>();
        for (String platform : platforms) {
            if (PlatformType.isStationaryId(platform)) {
                set.add(PlatformType.extractId(platform));
            }
        }
        return set;
    }

    private Set<String> getMobileIds(Set<String> platforms) {
        Set<String> set = new HashSet<>();
        for (String platform : platforms) {
            if (!PlatformType.isStationaryId(platform)) {
                set.add(PlatformType.extractId(platform));
            }
        }
        return set;
    }

    @Deprecated
    private void filterWithSingularParmameters(DetachedCriteria filter) {
        // old query parameter to stay backward compatible
        if (getParameters().getPhenomenon() != null) {
            filter.createCriteria("phenomenon")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getPhenomenon())));
        }
        if (getParameters().getProcedure() != null) {
            filter.createCriteria("procedure")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getProcedure())));
        }
        if (getParameters().getOffering() != null) {
            // here procedure == offering
            filter.createCriteria("procedure")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getOffering())));
        }
        if (getParameters().getFeature() != null) {
            filter.createCriteria("feature")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getFeature())));
        }
        if (getParameters().getStation() != null) {
            // here feature == station
            filter.createCriteria("feature")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getStation())));
        }
        if (getParameters().getCategory() != null) {
            filter.createCriteria("category")
                    .add(Restrictions.eq(COLUMN_KEY, parseToId(getParameters().getCategory())));
        }
    }

    public IoParameters getParameters() {
        return parameters;
    }

    public FilterResolver getFilterResolver() {
        return parameters.getFilterResolver();
    }

    @Override
    public String toString() {
        return "DbQuery{ parameters=" + getParameters().toString() + "'}'";
    }


}
