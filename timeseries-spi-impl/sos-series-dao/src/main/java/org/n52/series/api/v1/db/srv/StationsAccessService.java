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
package org.n52.series.api.v1.db.srv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.joda.time.DateTime;
import org.n52.io.IoParameters;
import org.n52.io.crs.BoundingBox;
import org.n52.io.crs.CRSUtils;
import org.n52.io.geojson.GeojsonPoint;
import org.n52.io.v1.data.ParameterOutput;
import org.n52.io.v1.data.StationOutput;
import org.n52.io.v1.data.TimeseriesOutput;
import org.n52.sensorweb.v1.spi.ParameterService;
import org.n52.series.api.v1.db.da.DataAccessException;
import org.n52.series.api.v1.db.da.DbQuery;
import org.n52.series.api.v1.db.da.StationRepository;
import org.n52.web.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StationsAccessService extends ServiceInfoAccess implements ParameterService<StationOutput> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StationsAccessService.class);
    
    private List<StationOutput> expandedCache;
    
    private CacheUpdateTask cacheUpdateTask;

    private int cacheUpdateIntervalInMinutes;
    
    private boolean cachingEnabled;

    public StationsAccessService(String dbSrid) {
        this.cacheUpdateTask = new CacheUpdateTask();
        if (dbSrid != null) {
            StationRepository repository = createStationRepository();
            repository.setDatabaseSrid(dbSrid);
        }
    }
    
    public void shutdownCache() {
        if (cacheUpdateTask != null) {
            cacheUpdateTask.cancel();
        }
    }
    
    public void initCache() {
        if (cachingEnabled) {
            Timer timer = new Timer("Caching expanded stations output.");
            timer.schedule(cacheUpdateTask, 0, cacheUpdateIntervalInMinutes * 60 * 1000);
        }
    }
    
    private class CacheUpdateTask extends TimerTask {

        private boolean cacheRunSuccessful;
        
        private boolean running;
        
        @Override
        public void run() {
            if (!running) {
                LOGGER.debug("Start cache update for expanded stations.");
                running = true;
                cacheRunSuccessful = updateCache();
                running = false;
                LOGGER.debug("Cache successfully updated.");
            } else {
                LOGGER.debug("Skip already running cache update.");
            }
            long currentExecutionTime = this.scheduledExecutionTime();
            long nextExecutionTime = currentExecutionTime + getCacheUpdateIntervalInMinutes() * 60 * 1000;
            LOGGER.debug("Next run: " + new DateTime(nextExecutionTime));
        }

        private boolean updateCache() {
            try {
                expandedCache = getExpandedStations(IoParameters.createDefaults());
                return true;
            } catch (DataAccessException e) {
                LOGGER.error("could not update station cache!", e);
                return false;
            }
        }
        
        boolean isCacheRunSuccessful() {
            return cacheRunSuccessful;
        }
    }
    
    @Override
    public StationOutput[] getExpandedParameters(IoParameters query) {
        try {
            if (!(cachingEnabled || cacheUpdateTask.isCacheRunSuccessful())) {
                return toArray(getExpandedStations(query));
            }
            List<StationOutput> cachedResults = expandedCache;
            List<StationOutput> filteredResults = new ArrayList<>();
            for (StationOutput cachedStation : cachedResults) {
                // apply possible query filters on each station
                Object properties = cachedStation.getProperties().get("timeseries");
                Map<String, TimeseriesOutput> series = (Map<String, TimeseriesOutput>) properties;
                if (appliesFilter(series, query) && appliesFilter(cachedStation.getGeometry(), query)) {
                    filteredResults.add(cachedStation);
                }
            }
            return toArray(filteredResults);
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get station data.");
        }
    }

    private boolean appliesFilter(Map<String, TimeseriesOutput> series, IoParameters query) {
        for (Entry<String, TimeseriesOutput> entry : series.entrySet()) {
            TimeseriesOutput value = entry.getValue();
            if (appliesFilterValue(query.getServices(), value.getService())
                    && appliesFilterValue(query.getCategories(), value.getCategory())
                    && appliesFilterValue(query.getProcedures(), value.getProcedure())
                    && appliesFilterValue(query.getPhenomena(), value.getPhenomenon())
                    && appliesFilterValue(query.getOfferings(), value.getOffering())
                    && appliesFilterValue(query.getFeatures(), value.getFeature())) {
                return true;
            }
        }
        return false;
    }

    private boolean appliesFilterValue(Set<String> filter, ParameterOutput value) {
        return filter.isEmpty() || filter.contains(value.getId());
    }

    private boolean appliesFilter(GeojsonPoint point, IoParameters query) {
        CRSUtils crsUtils = query.isForceXY()
                ? CRSUtils.createEpsgForcedXYAxisOrder()
                : CRSUtils.createEpsgStrictAxisOrder();
        BoundingBox spatialFilter = query.getSpatialFilter();
        return spatialFilter == null || spatialFilter.contains(crsUtils.convertToPointFrom(point));
    }

    private List<StationOutput> getExpandedStations(IoParameters query) throws DataAccessException {
        DbQuery dbQuery = DbQuery.createFrom(query);
        StationRepository repository = createStationRepository();
        return repository.getAllExpanded(dbQuery);
    }

    @Override
    public StationOutput[] getCondensedParameters(IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            StationRepository repository = createStationRepository();
            List<StationOutput> results = repository.getAllCondensed(dbQuery);
            return toArray(results);
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get station data.");
        }
    }

    @Override
    public StationOutput[] getParameters(String[] stationsIds) {
        return getParameters(stationsIds, IoParameters.createDefaults());
    }

    @Override
    public StationOutput[] getParameters(String[] stationIds, IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            StationRepository repository = createStationRepository();
            List<StationOutput> results = new ArrayList<StationOutput>();
            for (String stationId : stationIds) {
                results.add(repository.getInstance(stationId, dbQuery));
            }
            return toArray(results);
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get station data.");
        }
    }

    @Override
    public StationOutput getParameter(String stationsId) {
        return getParameter(stationsId, IoParameters.createDefaults());
    }

    @Override
    public StationOutput getParameter(String stationId, IoParameters query) {
        try {
            DbQuery dbQuery = DbQuery.createFrom(query);
            StationRepository repository = createStationRepository();
            return repository.getInstance(stationId, dbQuery);
        }
        catch (DataAccessException e) {
            throw new InternalServerException("Could not get station data.");
        }
    }

    private StationRepository createStationRepository() {
        return new StationRepository(getServiceInfo());
    }

    private StationOutput[] toArray(List<StationOutput> results) {
        return results.toArray(new StationOutput[0]);
    }

    public boolean isCachingEnabled() {
        return cachingEnabled;
    }

    public void setCachingEnabled(boolean cachingEnabled) {
        this.cachingEnabled = cachingEnabled;
    }

    public int getCacheUpdateIntervalInMinutes() {
        return cacheUpdateIntervalInMinutes;
    }

    public void setCacheUpdateIntervalInMinutes(int cacheUpdateIntervalInMinutes) {
        this.cacheUpdateIntervalInMinutes = cacheUpdateIntervalInMinutes;
    }
}
