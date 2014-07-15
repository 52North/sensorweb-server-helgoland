/**
 * Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
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

import static java.math.RoundingMode.HALF_UP;
import static org.n52.io.crs.CRSUtils.DEFAULT_CRS;
import static org.n52.io.crs.CRSUtils.createEpsgForcedXYAxisOrder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.n52.io.crs.CRSUtils;
import org.n52.io.geojson.GeojsonPoint;
import org.joda.time.Interval;
import org.n52.io.v1.data.ReferenceValueOutput;
import org.n52.io.v1.data.StationOutput;
import org.n52.io.v1.data.TimeseriesOutput;
import org.n52.io.v1.data.TimeseriesData;
import org.n52.io.v1.data.TimeseriesDataMetadata;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.n52.io.v1.data.TimeseriesValue;
import org.n52.series.api.v1.db.da.beans.DescribableEntity;
import org.n52.series.api.v1.db.da.beans.FeatureEntity;
import org.n52.series.api.v1.db.da.beans.I18nEntity;
import org.n52.series.api.v1.db.da.beans.ObservationEntity;
import org.n52.series.api.v1.db.da.beans.ProcedureEntity;
import org.n52.series.api.v1.db.da.beans.SeriesEntity;
import org.n52.series.api.v1.db.da.beans.ServiceInfo;
import org.n52.series.api.v1.db.da.dao.ObservationDao;
import org.n52.series.api.v1.db.da.dao.SeriesDao;
import org.n52.web.ResourceNotFoundException;
import org.n52.sensorweb.v1.spi.search.SearchResult;
import org.n52.sensorweb.v1.spi.search.TimeseriesSearchResult;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class TimeseriesRepository extends SessionAwareRepository implements OutputAssembler<TimeseriesMetadataOutput> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TimeseriesRepository.class);
	
	private CRSUtils crsUtil = createEpsgForcedXYAxisOrder();
	
    public TimeseriesRepository(ServiceInfo serviceInfo) {
        super(serviceInfo);
    }

    @Override
    public Collection<SearchResult> searchFor(String searchString, String locale) {
        Session session = getSession();
        try {
            SeriesDao seriesDao = new SeriesDao(session);
            DbQuery parameters = createDefaultsWithLocale(locale);
            List<SeriesEntity> found = seriesDao.find(searchString, parameters);
            return convertToResults(found, locale);
        }
        finally {
            returnSession(session);
        }
    }

    @Override
    protected List<SearchResult> convertToSearchResults(List< ? extends DescribableEntity< ? extends I18nEntity>> found,
                                                        String locale) {
        // not needed, use #convertToResults() instead
        return new ArrayList<SearchResult>();
    }

    private List<SearchResult> convertToResults(List<SeriesEntity> found, String locale) {
        List<SearchResult> results = new ArrayList<SearchResult>();
        for (SeriesEntity searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String phenomenonLabel = searchResult.getPhenomenon().getNameI18n(locale);
            String procedureLabel = searchResult.getProcedure().getNameI18n(locale);
            String stationLabel = searchResult.getFeature().getNameI18n(locale);
            String label = createTimeseriesLabel(phenomenonLabel, procedureLabel, stationLabel);
            results.add(new TimeseriesSearchResult(pkid, label));
        }
        return results;
    }


    @Override
    public List<TimeseriesMetadataOutput> getAllCondensed(DbQuery query) throws DataAccessException {
        Session session = getSession();
        try {
            SeriesDao seriesDao = new SeriesDao(session);
            List<TimeseriesMetadataOutput> results = new ArrayList<TimeseriesMetadataOutput>();
            for (SeriesEntity timeseries : seriesDao.getAllInstances(query)) {
                results.add(createCondensed(timeseries, query));
            }
            return results;

        }
        finally {
            returnSession(session);
        }
    }

    @Override
    public List<TimeseriesMetadataOutput> getAllExpanded(DbQuery query) throws DataAccessException {
        Session session = getSession();
        try {
            SeriesDao seriesDao = new SeriesDao(session);
            List<TimeseriesMetadataOutput> results = new ArrayList<TimeseriesMetadataOutput>();
            for (SeriesEntity timeseries : seriesDao.getAllInstances(query)) {
                results.add(createExpanded(session, timeseries, query));
            }
            return results;
        }
        finally {
            returnSession(session);
        }
    }

    @Override
    public TimeseriesMetadataOutput getInstance(String timeseriesId, DbQuery dbQuery) throws DataAccessException {
        Session session = getSession();
        try {
            SeriesDao seriesDao = new SeriesDao(session);
            SeriesEntity result = seriesDao.getInstance(parseId(timeseriesId), dbQuery);
            if (result == null) {
                throw new ResourceNotFoundException("Resource with id '" + timeseriesId + "' could not be found.");
            }
            return createExpanded(session, result, dbQuery);
        }
        finally {
            returnSession(session);
        }
    }

    public TimeseriesData getData(String timeseriesId, DbQuery dbQuery) throws DataAccessException {
        Session session = getSession();
        try {
            SeriesDao seriesDao = new SeriesDao(session);
            SeriesEntity timeseries = seriesDao.getInstance(parseId(timeseriesId), dbQuery);
            return createTimeseriesData(timeseries, dbQuery, session);
        }
        finally {
            returnSession(session);
        }
    }

    public TimeseriesData getDataWithReferenceValues(String timeseriesId, DbQuery dbQuery) throws DataAccessException {
        Session session = getSession();
        try {
            SeriesDao seriesDao = new SeriesDao(session);
            SeriesEntity timeseries = seriesDao.getInstance(parseId(timeseriesId), dbQuery);
            TimeseriesData result = createTimeseriesData(timeseries, dbQuery, session);
            Set<SeriesEntity> referenceValues = timeseries.getReferenceValues();
            if (referenceValues != null && !referenceValues.isEmpty()) {
                TimeseriesDataMetadata metadata = new TimeseriesDataMetadata();
                metadata.setReferenceValues(assembleReferenceSeries(referenceValues, dbQuery, session));
                result.setMetadata(metadata);
            }
            return result;
        }
        finally {
            returnSession(session);
        }
    }

    private TimeseriesMetadataOutput createExpanded(Session session, SeriesEntity series, DbQuery query) throws DataAccessException {
        TimeseriesMetadataOutput output = createCondensed(series, query);
        output.setParameters(createTimeseriesOutput(series, query));
        output.setReferenceValues(createReferenceValueOutputs(series, query));
        output.setFirstValue(createTimeseriesValueFor(series.getFirstValue(), series));
        output.setLastValue(createTimeseriesValueFor(series.getLastValue(), series));
        return output;
    }

    private ReferenceValueOutput[] createReferenceValueOutputs(SeriesEntity series,
                                                               DbQuery query) throws DataAccessException {
        Set<SeriesEntity> referenceValues = series.getReferenceValues();
        List<ReferenceValueOutput> outputs = new ArrayList<ReferenceValueOutput>();
        for (SeriesEntity referenceSeriesEntity : referenceValues) {
            ReferenceValueOutput refenceValueOutput = new ReferenceValueOutput();
            ProcedureEntity procedure = referenceSeriesEntity.getProcedure();
            refenceValueOutput.setLabel(procedure.getNameI18n(query.getLocale()));
            refenceValueOutput.setReferenceValueId(referenceSeriesEntity.getPkid().toString());

            ObservationEntity lastValue = series.getLastValue();
            refenceValueOutput.setLastValue(createTimeseriesValueFor(lastValue, series));
            outputs.add(refenceValueOutput);
        }
        return outputs.toArray(new ReferenceValueOutput[0]);
    }

    private TimeseriesMetadataOutput createCondensed(SeriesEntity entity, DbQuery query) throws DataAccessException {
        TimeseriesMetadataOutput output = new TimeseriesMetadataOutput();
        String locale = query.getLocale();
        String stationLabel = entity.getFeature().getNameI18n(locale);
        String procedureLabel = entity.getProcedure().getNameI18n(locale);
        String phenomenonLabel = entity.getPhenomenon().getNameI18n(locale);
        output.setLabel(createTimeseriesLabel(phenomenonLabel, procedureLabel, stationLabel));
        output.setId(entity.getPkid().toString());
        output.setUom(entity.getUnit().getNameI18n(locale));
        output.setStation(createCondensedStation(entity, query));
        return output;
    }

    private String createTimeseriesLabel(String phenomenon, String procedure, String station) {
        StringBuilder sb = new StringBuilder();
        sb.append(phenomenon).append(" ");
        sb.append(procedure).append(", ");
        return sb.append(station).toString();
    }

    private StationOutput createCondensedStation(SeriesEntity entity, DbQuery query) throws DataAccessException {
        FeatureEntity feature = entity.getFeature();
        String featurePkid = feature.getPkid().toString();
        StationRepository stationRepository = new StationRepository(getServiceInfo());
        return stationRepository.getCondensedInstance(featurePkid, query);
    }

    private Map<String, TimeseriesData> assembleReferenceSeries(Set<SeriesEntity> referenceValues,
                                                                DbQuery query,
                                                                Session session) throws DataAccessException {
        Map<String, TimeseriesData> referenceSeries = new HashMap<String, TimeseriesData>();
        for (SeriesEntity referenceSeriesEntity : referenceValues) {
            TimeseriesData referenceSeriesData = createTimeseriesData(referenceSeriesEntity, query, session);
            if (haveToExpandReferenceData(referenceSeriesData)) {
                referenceSeriesData = expandReferenceDataIfNecessary(referenceSeriesEntity, query, session);
            }
            referenceSeries.put(referenceSeriesEntity.getPkid().toString(), referenceSeriesData);
        }
        return referenceSeries;
    }

    private boolean haveToExpandReferenceData(TimeseriesData referenceSeriesData) {
        return referenceSeriesData.getValues().length <= 1;
    }

    private TimeseriesData expandReferenceDataIfNecessary(SeriesEntity seriesEntity, DbQuery query, Session session) throws DataAccessException {
        TimeseriesData result = new TimeseriesData();
        ObservationDao dao = new ObservationDao(session);
        List<ObservationEntity> observations = dao.getObservationsFor(seriesEntity, query);
        if ( !hasValidEntriesWithinRequestedTimespan(observations)) {
            ObservationEntity lastValidEntity = seriesEntity.getLastValue();
            result.addValues(expandToInterval(query.getTimespan(), lastValidEntity, seriesEntity));
        }

        if (hasSingleValidReferenceValue(observations)) {
            ObservationEntity entity = observations.get(0);
            result.addValues(expandToInterval(query.getTimespan(), entity, seriesEntity));
        }
        return result;
    }

    private boolean hasValidEntriesWithinRequestedTimespan(List<ObservationEntity> observations) {
        return observations.size() > 0;
    }

    private boolean hasSingleValidReferenceValue(List<ObservationEntity> observations) {
        return observations.size() == 1;
    }

    private TimeseriesData createTimeseriesData(SeriesEntity seriesEntity, DbQuery query, Session session) throws DataAccessException {
        TimeseriesData result = new TimeseriesData();
        ObservationDao dao = new ObservationDao(session);
        List<ObservationEntity> observations = dao.getAllInstancesFor(seriesEntity, query);
        for (ObservationEntity observation : observations) {
            if (observation != null) {
                result.addValues(createTimeseriesValueFor(observation, seriesEntity));
            }
        }
        return result;
    }

    private TimeseriesValue[] expandToInterval(Interval interval, ObservationEntity entity, SeriesEntity series) {
        ObservationEntity referenceStart = new ObservationEntity();
        ObservationEntity referenceEnd = new ObservationEntity();
        referenceStart.setTimestamp(interval.getStart().toDate());
        referenceEnd.setTimestamp(interval.getEnd().toDate());
        referenceStart.setValue(entity.getValue());
        referenceEnd.setValue(entity.getValue());
        return new TimeseriesValue[] {createTimeseriesValueFor(referenceStart, series),
                                      createTimeseriesValueFor(referenceEnd, series)};

    }

    private TimeseriesValue createTimeseriesValueFor(ObservationEntity observation, SeriesEntity series) {
        TimeseriesValue value = new TimeseriesValue();
        TimeseriesValue geom = new TimeseriesValue();
        value.setTimestamp(observation.getTimestamp().getTime());
        geom.setTimestamp(observation.getTimestamp().getTime());
        geom.setGeom(createPoint(observation));
        value.setValue(formatDecimal(observation.getValue(), series));
        return value, geom;
    }

    private GeojsonPoint createPoint(ObservationEntity observationEntity) {
        try {
            Geometry geometry = observationEntity.getGeom();
            String fromCrs = "EPSG:" + geometry.getSRID();
            Point location = crsUtil.transformOuterToInner((Point) geometry, fromCrs);
            return crsUtil.convertToGeojsonFrom(location, DEFAULT_CRS);
        }
        catch (FactoryException e) {
            LOGGER.info("Unable to create CRS factory for station/feature: {}" + observationEntity.getPkid());
        }
        catch (TransformException e) {
            LOGGER.info("Unable to transform station/feature: {}" + observationEntity.getPkid());
        } 
        return null;
    }
    
    private Double formatDecimal(Double value, SeriesEntity series) {
        int scale = series.getNumberOfDecimals();
        return new BigDecimal(value)
            .setScale(scale, HALF_UP)
            .doubleValue();
    }

}
