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

import static org.n52.io.request.Parameters.FEATURES;
import static org.n52.io.response.v1.ext.GeometryType.PLATFORM_SITE;
import static org.n52.io.response.v1.ext.GeometryType.PLATFORM_TRACK;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.v1.ext.GeometryInfo;
import org.n52.io.response.v1.ext.GeometryType;
import org.n52.io.response.v1.ext.PlatformItemOutput;
import org.n52.io.response.v1.ext.PlatformOutput;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.FeatureEntity;
import org.n52.series.db.da.beans.ext.GeometryEntity;
import org.n52.series.db.da.dao.v1.FeatureDao;
import org.n52.series.db.da.dao.v1.ext.SamplingGeometriesDao;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class GeometriesRepository extends ExtendedSessionAwareRepository implements OutputAssembler<GeometryInfo> {

    @Autowired
    private PlatformRepository platformRepository;

    @Override
    public boolean exists(String id) throws DataAccessException {
        Session session = getSession();
        try {
            if (GeometryType.isPlatformLocation(id)) {
                id = GeometryType.extractId(id);
                // XXX must be FALSE if 'site/2' matches an id of a feature from a mobile platform
                return new FeatureDao(session).hasInstance(parseId(id), FeatureEntity.class);
            }
            else if (GeometryType.isObservedGeometry(id)) {
                id = GeometryType.extractId(id);
                // TODO class of observed geometries
//                return new FeatureDao(session).hasInstance(parseId(id), clazz);
            }

            return false;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<GeometryInfo> getAllCondensed(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            return getAllInstances(parameters, session, false);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<GeometryInfo> getAllExpanded(DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            return getAllInstances(parameters, session, true);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public GeometryInfo getInstance(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            parameters.setDatabaseAuthorityCode(getDatabaseSrid());
            if (GeometryType.isPlatformLocation(id)) {
                return getPlatformLocationGeometry(id, parameters, session);
            } else {
                // TODO observed Geometry tpyes
                return null;
            }
        } finally {
            returnSession(session);
        }
    }

    @Override
    public Collection<SearchResult> searchFor(IoParameters parameters) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<SearchResult> convertToSearchResults(List<? extends DescribableEntity> found, String locale) {
        // TODO Auto-generated method stub
        return null;
    }

    private List<GeometryInfo> getAllInstances(DbQuery parameters, Session session, boolean expanded) throws DataAccessException {
        List<GeometryInfo> geometries = new ArrayList<>();
        parameters.setDatabaseAuthorityCode(getDatabaseSrid());
        if (shallIncludePlatformLocationsSites(parameters)) {
            geometries.addAll(getAllPlatformLocationsSites(parameters, session, expanded));
        }
        if (shallIncludePlatformLocationsTracks(parameters)) {
            geometries.addAll(getAllPlatformLocationsTracks(parameters, session, expanded));
        }
        if (shallIncludeObservedGeometriesStatic(parameters)) {
            geometries.addAll(getAllObservedGeometriesStatic(parameters, session, expanded));
        }
        if (shallIncludeObservedGeometriesDynamic(parameters)) {
            geometries.addAll(getAllObservedGeometriesDynamic(parameters, session, expanded));
        }
        return geometries;
    }

    private GeometryInfo getPlatformLocationGeometry(String id, DbQuery parameters, Session session) throws DataAccessException {
        String geometryId = GeometryType.extractId(id);
        FeatureEntity featureEntity = getFeatureEntity(geometryId, parameters, session);
        if (featureEntity != null) {
            if (GeometryType.isSiteId(id)) {
                return createSite(featureEntity, parameters, true);
            } else if (GeometryType.isTrackId(id)) {
                return createTrack(featureEntity, parameters, true, session);
            }
        }
        return null;
    }

    private FeatureEntity getFeatureEntity(String id, DbQuery parameters, Session session) throws DataAccessException {
        FeatureDao dao = new FeatureDao(session);
        long geometryId = Long.parseLong(GeometryType.extractId(id));
        return dao.getInstance(geometryId, parameters);
    }


    private List<GeometryInfo> getAllPlatformLocationsSites(DbQuery parameters, Session session, boolean expanded) throws DataAccessException {
        List<GeometryInfo> geometryInfoList = new ArrayList<>();
        FeatureDao dao = new FeatureDao(session);
        for (FeatureEntity featureEntity : dao.getAllStations(parameters)) {
            if (featureEntity.isSetGeometry()) {
                GeometryInfo geometryInfo = createSite(featureEntity, parameters, expanded);
                if (geometryInfo != null) {
                    geometryInfoList.add(geometryInfo);
                }
            }
        }
        return geometryInfoList;
    }

    private GeometryInfo createSite(FeatureEntity featureEntity, DbQuery parameters, boolean expanded)
            throws DataAccessException {
        Geometry geometry = featureEntity.getGeometry(getDatabaseSrid());
        if (geometry != null) {
            GeometryInfo geometryInfo = addCondensedValues(new GeometryInfo(PLATFORM_SITE), featureEntity, parameters);
            if (expanded) {
                geometryInfo.setGeometry(geometry);
            }
            return geometryInfo;
        }
        return null;
    }

    private Collection<GeometryInfo> getAllPlatformLocationsTracks(DbQuery parameters, Session session, boolean expanded) throws DataAccessException {
        List<GeometryInfo> geometryInfoList = new ArrayList<>();
        FeatureDao featureDao = new FeatureDao(session);
        for (FeatureEntity featureEntity : featureDao.getAllMobileInsitu(parameters)) {
            geometryInfoList.add(createTrack(featureEntity, parameters, expanded, session));
        }
        return geometryInfoList;
    }

    private GeometryInfo createTrack(FeatureEntity featureEntity, DbQuery parameters, boolean expanded, Session session) throws DataAccessException {
        GeometryInfo geometryInfo = addCondensedValues(new GeometryInfo(PLATFORM_TRACK), featureEntity, parameters);
        if (expanded) {
            if (featureEntity.isSetGeometry()) {
                geometryInfo.setGeometry(featureEntity.getGeometry(getDatabaseSrid()));
                return geometryInfo;
            } else {
                // TODO better solution for adding a parameter
                RequestSimpleParameterSet simpleParameterSet = parameters.getParameters().toSimpleParameterSet();
                simpleParameterSet.addParameter(FEATURES, IoParameters.getJsonNodeFrom(featureEntity.getPkid()));

                // XXX find or getInstances + filter
                List<GeometryEntity> samplingGeometries = new SamplingGeometriesDao(session).find(DbQuery.createFrom(IoParameters.createFromQuery(simpleParameterSet)));
                geometryInfo.setGeometry(createLineString(samplingGeometries));
                return geometryInfo;
            }
        }
        return geometryInfo;
    }

    private Collection<GeometryInfo> getAllObservedGeometriesStatic(DbQuery parameters, Session session, boolean expanded) {
        // TODO implement
        return new ArrayList<>();
    }

    private Collection<GeometryInfo> getAllObservedGeometriesDynamic(DbQuery parameters, Session session, boolean expanded) {
        // TODO implement
        return new ArrayList<>();
    }

    private List<GeometryEntity> getAllObservedGeometries(DbQuery parameters, Session session) {
        // TODO Auto-generated method stub
        return null;
    }

    private Collection<? extends GeometryEntity> convertAllMobileInsitu(List<FeatureEntity> allMobileInsitu) {
        // TODO Auto-generated method stub
        return null;
    }

    private static boolean shallIncludeAll(DbQuery parameters) {
        return parameters.isAllGeomentries();
    }

    private static boolean shallIncludeAllPlatformLocations(DbQuery parameters) {
        return parameters.isAllPlatformLocations() || shallIncludeAll(parameters);
    }

    private boolean shallIncludePlatformLocationsSites(DbQuery parameters) {
        return parameters.isSites() || shallIncludeAllPlatformLocations(parameters);
    }

    private boolean shallIncludePlatformLocationsTracks(DbQuery parameters) {
        return parameters.isTracks() || shallIncludeAllPlatformLocations(parameters);
    }

    private boolean shallIncludeObservedGeometries(DbQuery parameters) {
        return parameters.isAllObservedGeometries() || shallIncludeAll(parameters);
    }

    private boolean shallIncludeObservedGeometriesDynamic(DbQuery parameters) {
        return parameters.isStaticObservedGeometries() || shallIncludeObservedGeometries(parameters);
    }

    private boolean shallIncludeObservedGeometriesStatic(DbQuery parameters) {
        return parameters.isDynamicObservedGeometries() || shallIncludeObservedGeometries(parameters);
    }

    private GeometryInfo addCondensedValues(GeometryInfo geometryInfo, FeatureEntity featureEntity,
            DbQuery parameters) throws DataAccessException {
        geometryInfo.setId(Long.toString(featureEntity.getPkid()));
        geometryInfo.setHrefBase(urHelper.getGeometriesHrefBaseUrl(parameters.getHrefBase()));
        geometryInfo.setPlatform(getPlatfom(featureEntity.getPkid(), parameters));
        return geometryInfo;
    }

    private PlatformItemOutput getPlatfom(Long id, DbQuery parameters) throws DataAccessException {
        RequestSimpleParameterSet simpleParameterSet = parameters.getParameters().toSimpleParameterSet();
        simpleParameterSet.addParameter(Parameters.FEATURES, IoParameters.getJsonNodeFrom(id));
        simpleParameterSet.addParameter(Parameters.INCLUDE_ALL, IoParameters.getJsonNodeFrom(true));
        List<PlatformOutput> platforms = platformRepository.getAllCondensed(DbQuery.createFrom(IoParameters.createFromQuery(simpleParameterSet)));
        return platforms.iterator().next();
    }

    private Geometry createLineString(List<GeometryEntity> samplingGeometries) {
        List<Coordinate> coordinates = new ArrayList<Coordinate>();
        for (GeometryEntity geometryEntity : samplingGeometries) {
            Point geometry = (Point) geometryEntity.getGeometry(getDatabaseSrid());
            coordinates.add(geometry.getCoordinate());
        }
        return getCrsUtils().createLineString(coordinates.toArray(new Coordinate[0]), getDatabaseSrid());
    }

}
