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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.v1.ext.GeometryCategory;
import org.n52.io.response.v1.ext.GeometryInfo;
import org.n52.io.response.v1.ext.PlatformItemOutput;
import org.n52.io.response.v1.ext.PlatformOutput;
import org.n52.io.response.v1.ext.PlatformType;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.FeatureEntity;
import org.n52.series.db.da.beans.ext.GeometryEntity;
import org.n52.series.db.da.beans.ext.PlatformEntity;
import org.n52.series.db.da.dao.v1.FeatureDao;
import org.n52.series.db.da.dao.v1.ObservationDao;
import org.n52.series.db.da.dao.v1.ext.SamplingGeometriesDao;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class GeometriesRepository extends ExtendedSessionAwareRepository implements OutputAssembler<GeometryInfo> {

    @Autowired
    private PlatformRepository platformRepository;

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
            if (GeometryCategory.isSiteId(id) || GeometryCategory.isTrackId(id)) {
                return getPlatformLocationGeometry(id, parameters, session);
            } else {
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
        FeatureEntity featureEntity = new FeatureDao(session).getInstance(Long.parseLong(GeometryCategory.extractId(id)), parameters);
        if (featureEntity != null) {
            if (GeometryCategory.isSiteId(id)) {
                return createSite(featureEntity, parameters, true);
            } else if (GeometryCategory.isTrackId(id)) {
                return createTrack(featureEntity, parameters, true, session);
            }
        }
        return null;
    }


    private List<GeometryInfo> getAllPlatformLocationsSites(DbQuery parameters, Session session, boolean expanded) throws DataAccessException {
        List<GeometryInfo> geometryInfoList = new ArrayList<>();
        for (FeatureEntity featureEntity : new FeatureDao(session).getAllStations(parameters)) {
            if (featureEntity.isSetGeometry()) {
                GeometryInfo geometryInfo = createSite(featureEntity, parameters, expanded);
                if (geometryInfo != null) {
                    geometryInfoList.add(geometryInfo);
                }
            }
        }
        return geometryInfoList;
    }

    private GeometryInfo createSite(FeatureEntity featureEntity, DbQuery parameters, boolean expanded) throws DataAccessException {
        if (featureEntity.isSetGeometry()) {
            GeometryInfo geometryInfo = addCondensedValues(new GeometryInfo(GeometryCategory.PLATFORM_SITE), featureEntity, parameters);
            if (expanded) {
                if (featureEntity.getGeometry().isSetGeometry()) {
                    geometryInfo.setGeometry(featureEntity.getGeometry().getGeometry());
                } else if (featureEntity.getGeometry().isSetLonLat()) {
                    // TODO
//                    geometryInfo.setGeometry(featureEntity.getGeometry());
                }
            }
            return geometryInfo;
        }
        return null;
    }

    private Collection<GeometryInfo> getAllPlatformLocationsTracks(DbQuery parameters, Session session, boolean expanded) throws DataAccessException {
        List<GeometryInfo> geometryInfoList = new ArrayList<>();
        for (FeatureEntity featureEntity : new FeatureDao(session).getAllMobileInsitu(parameters)) {
            geometryInfoList.add(createTrack(featureEntity, parameters, expanded, session));
        }
        return geometryInfoList;
    }

    private GeometryInfo createTrack(FeatureEntity featureEntity, DbQuery parameters, boolean expanded, Session session) throws DataAccessException {
        GeometryInfo geometryInfo = addCondensedValues(new GeometryInfo(GeometryCategory.PLATFORM_TRACK), featureEntity, parameters);
        if (expanded) {
            if (featureEntity.isSetGeometry()) {
                if (featureEntity.getGeometry().isSetGeometry()) {
                    geometryInfo.setGeometry(featureEntity.getGeometry().getGeometry());
                    return geometryInfo;
                }

            } else {
                List<GeometryEntity> samplingGeometries = new SamplingGeometriesDao(session).find(parameters);
                geometryInfo.setGeometry(createGeometry(samplingGeometries));
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
        simpleParameterSet.addParameter(Parameters.PLATFORMS_INCLUDE_ALL, IoParameters.getJsonNodeFrom(true));
        List<PlatformOutput> platforms = platformRepository.getAllCondensed(DbQuery.createFrom(IoParameters.createFromQuery(simpleParameterSet)));
        return platforms.iterator().next();
    }

    private Geometry createGeometry(List<GeometryEntity> samplingGeometries) {
        List<Coordinate> coordinates = new ArrayList<Coordinate>();
        int srid = 4326;
        for (GeometryEntity geometryEntity : samplingGeometries) {
            if (geometryEntity.isSetLonLat()) {
                coordinates.add(new Coordinate(geometryEntity.getLon(), geometryEntity.getLat()));
            } else if (geometryEntity.isSetGeometry()) {
                Geometry geometry = geometryEntity.getGeometry();
                coordinates.addAll(Arrays.asList(geometryEntity.getGeometry().getCoordinates()));
                if (geometry.getSRID() != srid) {
                    srid = geometry.getSRID();
                 }
            }
        }
        Geometry geom = null;
        if (coordinates.size() == 1) {
            geom = new GeometryFactory().createPoint(coordinates.iterator().next());
        } else {
            geom = new GeometryFactory().createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
        }
        geom.setSRID(srid);
        return geom;
    }
}
