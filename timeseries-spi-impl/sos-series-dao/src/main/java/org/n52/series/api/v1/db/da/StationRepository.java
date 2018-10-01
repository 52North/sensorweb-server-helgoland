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
package org.n52.series.api.v1.db.da;

import static org.n52.io.crs.CRSUtils.createEpsgForcedXYAxisOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.crs.CRSUtils;
import org.n52.io.v1.data.StationOutput;
import org.n52.sensorweb.v1.spi.search.SearchResult;
import org.n52.sensorweb.v1.spi.search.StationSearchResult;
import org.n52.series.api.v1.db.da.beans.DescribableEntity;
import org.n52.series.api.v1.db.da.beans.FeatureEntity;
import org.n52.series.api.v1.db.da.beans.I18nEntity;
import org.n52.series.api.v1.db.da.beans.SeriesEntity;
import org.n52.series.api.v1.db.da.beans.ServiceInfo;
import org.n52.series.api.v1.db.da.dao.FeatureDao;
import org.n52.series.api.v1.db.da.dao.SeriesDao;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class StationRepository extends SessionAwareRepository implements OutputAssembler<StationOutput> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StationRepository.class);

    private final CRSUtils crsUtil = createEpsgForcedXYAxisOrder();

    public StationRepository(ServiceInfo serviceInfo) {
        super(serviceInfo);
    }

    @Override
    public Collection<SearchResult> searchFor(String searchString, String locale) {
        Session session = getSession();
        try {
            FeatureDao stationDao = new FeatureDao(session);
            DbQuery parameters = createDefaultsWithLocale(locale);
            List<FeatureEntity> found = stationDao.find(searchString, parameters);
            return convertToSearchResults(found, locale);
        }
        finally {
            returnSession(session);
        }
    }

    @Override
    protected List<SearchResult> convertToSearchResults(List< ? extends DescribableEntity< ? extends I18nEntity>> found,
                                                        String locale) {
        List<SearchResult> results = new ArrayList<>();
        for (DescribableEntity< ? extends I18nEntity> searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String label = getLabelFrom(searchResult,locale);
            results.add(new StationSearchResult(pkid, label));
        }
        return results;
    }

    @Override
    public List<StationOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            return getAllCondensed(parameters, session);
        }
        finally {
            returnSession(session);
        }
    }

    @Override
    public List<StationOutput> getAllCondensed(DbQuery parameters, Session session) throws DataAccessException {
        FeatureDao featureDao = new FeatureDao(session);
        List<FeatureEntity> allFeatures = featureDao.getAllInstances(parameters);
        List<StationOutput> results = new ArrayList<>();
        for (FeatureEntity featureEntity : allFeatures) {
            results.add(createCondensed(featureEntity, parameters));
        }
        return results;
    }

    @Override
    public List<StationOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            return getAllExpanded(parameters, session);
        }
        finally {
            returnSession(session);
        }
    }

    @Override
    public List<StationOutput> getAllExpanded(DbQuery parameters, Session session) throws DataAccessException {
        FeatureDao featureDao = new FeatureDao(session);
        List<FeatureEntity> allFeatures = featureDao.getAllInstances(parameters);
        List<StationOutput> results = new ArrayList<>();
        for (FeatureEntity featureEntity : allFeatures) {
            results.add(createExpanded(featureEntity, parameters, session));
        }
        return results;
    }

    @Override
    public StationOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            return getInstance(id, parameters, session);
        }
        finally {
            returnSession(session);
        }
    }

    @Override
    public StationOutput getInstance(String id, DbQuery parameters, Session session) throws DataAccessException {
        FeatureDao featureDao = new FeatureDao(session);
        FeatureEntity result = featureDao.getInstance(parseId(id), parameters);
        return result != null
                ? createExpanded(result, parameters, session)
                : null;
    }

    StationOutput getCondensedInstance(String id, DbQuery parameters, Session session) throws DataAccessException {
        FeatureDao featureDao = new FeatureDao(session);
        FeatureEntity result = featureDao.getInstance(parseId(id));
        return createCondensed(result, parameters);
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
        stationOutput.setId(entity.getPkid() + "");
        stationOutput.setLabel(getLabelFrom(entity, parameters.getLocale()));
        return stationOutput;
    }

    private Geometry createPoint(FeatureEntity featureEntity) {
        if (featureEntity.isSetGeom()) {
            try {
                Geometry geometry = featureEntity.getGeom();
                return transformGeometry(featureEntity, geometry);
            } catch (Exception e) {
                LOGGER.info("Unable to transform station/feature: {}" + featureEntity.getDomainId());
                return null;
            }
        }
        return null;
    }

    private Geometry transformGeometry(FeatureEntity featureEntity, Geometry geometry) throws TransformException, FactoryException {
        try {
            String fromCrs = "EPSG:" + geometry.getSRID();
            return crsUtil.transformOuterToInner(geometry, fromCrs);
        }
        catch (FactoryException e) {
            LOGGER.info("Unable to create CRS factory for station/feature: {}" + featureEntity.getDomainId());
            return crsUtil.transformOuterToInner(geometry, CRSUtils.DEFAULT_CRS);
        }
    }

}
