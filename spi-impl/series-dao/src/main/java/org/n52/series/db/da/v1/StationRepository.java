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
package org.n52.series.db.da.v1;

import static org.n52.io.crs.CRSUtils.createEpsgForcedXYAxisOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.crs.CRSUtils;
import org.n52.io.request.IoParameters;
import org.n52.io.response.v1.StationOutput;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.sensorweb.spi.search.v1.StationSearchResult;
import org.n52.series.db.da.beans.v1.SeriesEntity;
import org.n52.series.db.da.dao.v1.FeatureDao;
import org.n52.series.db.da.dao.v1.SeriesDao;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.FeatureEntity;
import org.n52.series.db.da.beans.I18nEntity;
import org.n52.web.exception.ResourceNotFoundException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class StationRepository extends ExtendedSessionAwareRepository implements OutputAssembler<StationOutput> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StationRepository.class);

    private CRSUtils crsUtil = createEpsgForcedXYAxisOrder();

    private String dbSrid = "EPSG:4326";

    @Override
    public Collection<SearchResult> searchFor(String searchString, String locale) {
        Session session = getSession();
        try {
            FeatureDao stationDao = new FeatureDao(session);
            DbQuery parameters = DbQuery.createFrom(IoParameters.createDefaults(), locale);
            List<FeatureEntity> found = stationDao.find(searchString, parameters);
            return convertToSearchResults(found, locale);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<SearchResult> convertToSearchResults(List< ? extends DescribableEntity< ? extends I18nEntity>> found,
            String locale) {
        List<SearchResult> results = new ArrayList<SearchResult>();
        for (DescribableEntity< ? extends I18nEntity> searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String label = getLabelFrom(searchResult, locale);
            results.add(new StationSearchResult(pkid, label));
        }
        return results;
    }

    @Override
    public List<StationOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            parameters.setDatabaseAuthorityCode(dbSrid);
            FeatureDao featureDao = new FeatureDao(session);
            List<FeatureEntity> allFeatures = featureDao.getAllInstances(parameters);

            List<StationOutput> results = new ArrayList<StationOutput>();
            for (FeatureEntity featureEntity : allFeatures) {
                results.add(createCondensed(featureEntity, parameters));
            }

            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<StationOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            parameters.setDatabaseAuthorityCode(dbSrid);
            FeatureDao featureDao = new FeatureDao(session);
            List<FeatureEntity> allFeatures = featureDao.getAllInstances(parameters);
            List<StationOutput> results = new ArrayList<StationOutput>();
            for (FeatureEntity featureEntity : allFeatures) {
                results.add(createExpanded(featureEntity, parameters, session));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public StationOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            parameters.setDatabaseAuthorityCode(dbSrid);
            FeatureDao featureDao = new FeatureDao(session);
            FeatureEntity result = featureDao.getInstance(parseId(id), parameters);
            if (result == null) {
                throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
            }
            return createExpanded(result, parameters, session);
        } finally {
            returnSession(session);
        }
    }

    public StationOutput getCondensedInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            parameters.setDatabaseAuthorityCode(dbSrid);
            FeatureDao featureDao = new FeatureDao(session);
            FeatureEntity result = featureDao.getInstance(parseId(id), DbQuery.createFrom(IoParameters.createDefaults()));
            return createCondensed(result, parameters);
        } finally {
            returnSession(session);
        }
    }

    private StationOutput createExpanded(FeatureEntity feature, DbQuery parameters, Session session) throws DataAccessException {
        SeriesDao seriesDao = new SeriesDao(session);
        List<SeriesEntity> series = seriesDao.getInstancesWith(feature);
        StationOutput stationOutput = createCondensed(feature, parameters);
        stationOutput.setTimeseries(createTimeseriesList(series, parameters));
        return stationOutput;
    }

    private StationOutput createCondensed(FeatureEntity entity, DbQuery parameters) {
        StationOutput stationOutput = new StationOutput();
        stationOutput.setGeometry(createPoint(entity));
        stationOutput.setId(Long.toString(entity.getPkid()));
        stationOutput.setLabel(getLabelFrom(entity, parameters.getLocale()));
        return stationOutput;
    }

    private Geometry createPoint(FeatureEntity featureEntity) {
        try {
            if (featureEntity.isSetGeom()) {
                Geometry geometry = featureEntity.getGeom();
                String fromCrs = "EPSG:" + geometry.getSRID();
                Point location = (Point) crsUtil.transformOuterToInner((Point) geometry, fromCrs);
                //return crsUtil.convertToGeojsonFrom(location, DEFAULT_CRS);
                return location;
            }
        } catch (FactoryException e) {
            LOGGER.info("Unable to create CRS factory for station/feature: {}" + featureEntity.getDomainId(), e);
        } catch (TransformException e) {
            LOGGER.info("Unable to transform station/feature: {}" + featureEntity.getDomainId(), e);
        }
        return null;
    }

    public void setDatabaseSrid(String dbSrid) {
        this.dbSrid = dbSrid;
    }

}
