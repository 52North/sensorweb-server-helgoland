/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.series.db.da.v2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.geojson.GeoJSONException;
import org.n52.io.geojson.GeoJSONObject;
import org.n52.io.request.IoParameters;
import org.n52.io.response.v2.FeatureOutput;
import org.n52.io.response.v2.FeatureOutputCollection;
import org.n52.io.response.v2.SiteOutput;
import org.n52.io.response.v2.TrackOutput;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.sensorweb.spi.search.FeatureSearchResult;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.DbQuery;
import org.n52.series.db.da.OutputAssembler;
import org.n52.series.db.da.SessionAwareRepository;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.I18nEntity;
import org.n52.series.db.da.beans.ServiceInfo;
import org.n52.series.db.da.beans.v2.SiteEntity;
import org.n52.series.db.da.beans.v2.TrackEntity;
import org.n52.series.db.da.beans.v2.TrackLocationEntity;
import org.n52.series.db.da.dao.v2.SiteDao;
import org.n52.series.db.da.dao.v2.TrackDao;
import org.n52.web.exception.ResourceNotFoundException;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

public class FeatureRepository extends SessionAwareRepository implements OutputAssembler<FeatureOutput>  {

	private static final String SITE_PREFIX = "site_";
	
	private static final String TRACK_PREFIX = "track_";
	
	private static final String TRACK_FEATURE_PREFIX = "trackFeature_";
	
	private enum FeatureType {
		SITE,
		TRACK_FEATURE,
		TRACK_OFFERING,
		NON;
	}
	
	public FeatureRepository(ServiceInfo serviceInfo) {
		super(serviceInfo);
	}

	@Override
	public Collection<SearchResult> searchFor(String queryString, String locale) {
		Session session = getSession();
		try {
			List<DescribableEntity<?>> results = new ArrayList<DescribableEntity<?>>();
			DbQuery parameters = DbQueryV2.createFrom(IoParameters.createDefaults(), locale);
			Collection<SiteEntity> sites = new SiteDao(session).find(queryString, parameters);
			if (sites != null) {
				results.addAll(sites);
			}
			Collection<TrackEntity> tracks =  new TrackDao(session).find(queryString, parameters);
			if (tracks != null) {
				results.addAll(tracks);
			}
			return convertToSearchResults(results, locale);
		} finally {
			returnSession(session);
		}
	}

	@Override
	protected List<SearchResult> convertToSearchResults(List<? extends DescribableEntity<? extends I18nEntity>> found,
			String locale) {
		List<SearchResult> results = new ArrayList<SearchResult>();
        for (DescribableEntity< ? extends I18nEntity> searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String label = getLabelFrom(searchResult, locale);
            results.add(new FeatureSearchResult(pkid, label));
        }
        return results;
	}

	@Override
	public List<FeatureOutput> getAllCondensed(DbQuery parameters) throws DataAccessException {
		Session session = getSession();
		try {
			List<FeatureOutput> results = new ArrayList<FeatureOutput>();
			List<SiteEntity> sites = new SiteDao(session).getAllInstances(parameters);
			if (sites != null) {
				for (SiteEntity entity : sites) {
					results.add(createCondensed(entity, parameters, FeatureType.SITE));
				}
			}
			List<TrackEntity> tracks = new TrackDao(session).getAllInstances(parameters);
			if (tracks != null) {
				for (TrackEntity entity : tracks) {
					results.add(createCondensed(entity, parameters, FeatureType.TRACK_OFFERING, session));
				}
			}
			return results;
		} finally {
			returnSession(session);
		}
	}

	@Override
	public List<FeatureOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
		Session session = getSession();
		try {
			List<FeatureOutput> results = new ArrayList<FeatureOutput>();
			List<SiteEntity> sites = new SiteDao(session).getAllInstances(parameters);
			if (sites != null) {
				for (SiteEntity entity : sites) {
					results.add(createExpanded(entity, parameters, FeatureType.SITE));
				}
			}
			List<TrackEntity> tracks = new TrackDao(session).getAllInstances(parameters);
			if (tracks != null) {
				for (TrackEntity entity : tracks) {
					results.add(createExpanded(entity, parameters, FeatureType.TRACK_OFFERING, session));
				}
			}
			return results;
		} finally {
			returnSession(session);
		}
	}

	@Override
	public FeatureOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
		Session session = getSession();
		try {
			FeatureOutput result = getInstance(id, parameters, session);
			if (result == null) {
	            throw new ResourceNotFoundException("Resource with id '" + id + "' could not be found.");
	        }
			return result;
		} finally {
			returnSession(session);
		}
	}

	public FeatureOutputCollection getSites(DbQuery parameters) throws DataAccessException {
		Session session = getSession();
		try {
			List<FeatureOutput> results = new ArrayList<FeatureOutput>();
			List<SiteEntity> sites = new SiteDao(session).getAllInstances(parameters);
			if (sites != null) {
				for (SiteEntity entity : sites) {
					results.add(createCondensed(entity, parameters, FeatureType.SITE));
				}
			}
			return new FeatureOutputCollection(results);
		} finally {
			returnSession(session);
		}
	}

	public FeatureOutputCollection getTracks(DbQuery parameters) throws DataAccessException {
		Session session = getSession();
		try {
			List<FeatureOutput> results = new ArrayList<FeatureOutput>();
			List<TrackEntity> tracks = new TrackDao(session).getAllInstances(parameters);
			if (tracks != null) {
				for (TrackEntity entity : tracks) {
					results.add(createCondensed(entity, parameters, FeatureType.TRACK_OFFERING, session));
				}
			}
			return new FeatureOutputCollection(results);
		} finally {
			returnSession(session);
		}
	}

	private FeatureOutput getInstance(String id, DbQuery parameters, Session session) throws DataAccessException {
		FeatureType type = getTypeFor(id);
		if (FeatureType.SITE.equals(type) || FeatureType.TRACK_FEATURE.equals(type)) {
			SiteEntity siteEntity = new SiteDao(session).getInstance(parseFeatureId(id, type), parameters);
			return createExpanded(siteEntity, parameters, type);
		} else if (FeatureType.TRACK_OFFERING.equals(type)) {
			TrackEntity trackEntity = new TrackDao(session).getInstance(parseFeatureId(id, type), parameters);
			return createExpanded(trackEntity, parameters, type, session);
		}
		return null;
	}
	
	private FeatureType getTypeFor(String featureId) {
		if (featureId.startsWith(SITE_PREFIX)) {
			return FeatureType.SITE;
		} else if (featureId.startsWith(TRACK_PREFIX)) {
			return FeatureType.TRACK_OFFERING;
		} else if (featureId.startsWith(TRACK_FEATURE_PREFIX)) {
			return FeatureType.TRACK_FEATURE;
		} 
		return FeatureType.NON;
	}
	
	
	public Long parseFeatureId(String id) throws DataAccessException {
		return parseFeatureId(id, getTypeFor(id));
	}
	private Long parseFeatureId(String id, FeatureType type) throws DataAccessException {
		switch (type) {
		case SITE:
			return super.parseId(id.replace(SITE_PREFIX, ""));
		case TRACK_OFFERING:
			return super.parseId(id.replace(TRACK_PREFIX, ""));
		case TRACK_FEATURE:
			return super.parseId(id.replace(TRACK_FEATURE_PREFIX, ""));
		default:
			return super.parseId(id);
		}
	}
	
	private String createUniqueId(Long id, FeatureType type) {
		String stringId = Long.toString(id);
		switch (type) {
		case SITE:
			return SITE_PREFIX + stringId;
		case TRACK_OFFERING:
			return TRACK_PREFIX + stringId;
		case TRACK_FEATURE:
			return TRACK_FEATURE_PREFIX + stringId;
		default:
			return stringId;
		}
	}
	
	private FeatureOutput createExpanded(SiteEntity entity, DbQuery parameters, FeatureType type) throws DataAccessException {
		FeatureOutput result = createCondensed(entity, parameters, type);
        if (result != null) {
        	
        }
        return result;
    }

	private FeatureOutput createCondensed(SiteEntity entity, DbQuery parameters, FeatureType type) throws DataAccessException {
		if (entity.isSetGeom()) {
			try {
				FeatureOutput result = null;
				if (entity.getGeom() instanceof Point) {
					result = new SiteOutput();
				} else {
					// TODO
				}
				if (result != null) {
					result.setId(createUniqueId(entity.getPkid(), type));
					if (entity.isSetName()) {
						result.addProperty(GeoJSONObject.LABEL, entity.getName());
					}
					result.addProperty("platform", entity.getPkid());
					return result;
				}
			} catch (GeoJSONException e) {
				throw new DataAccessException("Error while creating output.", e);
			}
		}
		return null;
	}
    
	private TrackOutput createExpanded(TrackEntity entity, DbQuery parameters, FeatureType type, Session session) throws DataAccessException {
    	TrackOutput result = createCondensed(entity, parameters, type, session);
    	if (result != null && entity.hasTrackLocations()) {
    		List<TrackLocationEntity> trackLocations = entity.getTrackLocations();
			Geometry geom = createGeometryFrom(trackLocations);
			result.setGeometry(geom);
	    	if (entity.isSetName()) {
				result.addProperty(entity.getName(), entity.getName());
			}
	    	List<Long> platform = getPlatformIdForTrack(entity.getPkid(), session);
	    	if (platform != null && !platform.isEmpty()) {
	    		if (platform.size() == 1) {
	    			result.addProperty("platform", platform.iterator().next());
	    		} else {
	    			result.addProperty("platforms", platform.toArray(new Long[platform.size()]));
	    		}
	    	}
    	}
        return result;
    }

    private TrackOutput createCondensed(TrackEntity entity, DbQuery parameters, FeatureType type, Session session) throws DataAccessException {
    	try {
    		if (entity.hasTrackLocations()) {
    			TrackOutput result = new TrackOutput();
    	    	result.setId(createUniqueId(entity.getPkid(), type));
    		}
    	} catch (GeoJSONException e) {
			throw new DataAccessException("Error while creating output.", e);
		}
        return null;
    }
    
    private List<Long> getPlatformIdForTrack(Long pkid, Session session) {
		return new TrackDao(session).getRelatedPlatforms(pkid);
	}

	private Geometry createGeometryFrom(List<TrackLocationEntity> trackLocations) {
    	 List<Coordinate> coordinates = Lists.newLinkedList();
	    int srid = -1;
	    for (TrackLocationEntity trackLocation : trackLocations) {
	    	if (trackLocation.isSetGeom()) {
	    		Geometry geom = trackLocation.getGeom();
	    		if (geom instanceof Point) {
	    			coordinates.add(trackLocation.getGeom().getCoordinate());
	    		} else {
	    			// TODO 
	    		}
	    		if (srid < 0) {
                    srid = geom.getSRID();
                }
	    	}
	    }
	    if (srid < 0) {
	    	srid = 4326;
	    }
	    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid);
	    if (coordinates.size() == 1) {
	        return geometryFactory.createPoint(coordinates.iterator().next());
	    } else {
	        return geometryFactory.createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
	    }
    }
	
	@Override
	protected DbQuery getDbQuery(IoParameters parameters) {
		return DbQueryV2.createFrom(parameters);
	}

	@Override
	protected DbQuery getDbQuery(IoParameters parameters, String locale) {
		return DbQueryV2.createFrom(parameters, locale);
	}
	
}
