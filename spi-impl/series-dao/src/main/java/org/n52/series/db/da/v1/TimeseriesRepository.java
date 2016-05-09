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

import static java.math.RoundingMode.HALF_UP;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.joda.time.Interval;
import org.n52.io.request.IoParameters;
import org.n52.io.response.ReferenceValueOutput;
import org.n52.io.response.TimeseriesData;
import org.n52.io.response.TimeseriesValue;
import org.n52.io.response.v1.StationOutput;
import org.n52.io.response.TimeseriesDataMetadata;
import org.n52.io.response.TimeseriesMetadataOutput;
import org.n52.io.response.v1.SeriesMetadataV1Output;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.sensorweb.spi.search.v1.TimeseriesSearchResult;
import org.n52.series.db.da.dao.v1.SeriesDao;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.FeatureEntity;
import org.n52.series.db.da.beans.I18nEntity;
import org.n52.series.db.da.beans.ProcedureEntity;
import org.n52.series.db.da.beans.ext.MeasurementEntity;
import org.n52.series.db.da.beans.ext.MeasurementSeriesEntity;
import org.n52.series.db.da.dao.v1.ObservationDao;
import org.n52.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 * @deprecated since 2.0.0
 */
@Deprecated
public class TimeseriesRepository extends ExtendedSessionAwareRepository implements OutputAssembler<TimeseriesMetadataOutput> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeseriesRepository.class);

    @Autowired
    @Qualifier(value = "stationRepository")
    private OutputAssembler<StationOutput> stationRepository;

    @Override
    public Collection<SearchResult> searchFor(String searchString, String locale) {
        Session session = getSession();
        try {
            SeriesDao<MeasurementSeriesEntity> seriesDao = new SeriesDao<>(session);
            DbQuery parameters = DbQuery.createFrom(IoParameters.createDefaults(), locale);
            List<MeasurementSeriesEntity> found = seriesDao.find(searchString, parameters);
            return convertToResults(found, locale);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<SearchResult> convertToSearchResults(List< ? extends DescribableEntity< ? extends I18nEntity>> found,
            String locale) {
        // not needed, use #convertToResults() instead
        return new ArrayList<>();
    }

    private List<SearchResult> convertToResults(List<MeasurementSeriesEntity> found, String locale) {
        List<SearchResult> results = new ArrayList<>();
        for (MeasurementSeriesEntity searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String phenomenonLabel = getLabelFrom(searchResult.getPhenomenon(), locale);
            String procedureLabel = getLabelFrom(searchResult.getProcedure(), locale);
            String stationLabel = getLabelFrom(searchResult.getFeature(), locale);
            String label = createTimeseriesLabel(phenomenonLabel, procedureLabel, stationLabel);
            results.add(new TimeseriesSearchResult(pkid, label));
        }
        return results;
    }

    @Override
    public List<TimeseriesMetadataOutput> getAllCondensed(DbQuery query) throws DataAccessException {
        Session session = getSession();
        try {
            List<TimeseriesMetadataOutput> results = new ArrayList<>();
            SeriesDao<MeasurementSeriesEntity> seriesDao = new SeriesDao<>(session);
            for (MeasurementSeriesEntity timeseries : seriesDao.getAllInstances(query)) {
                /*
                 *  ATM, the SWC REST API only supports numeric types
                 *  We check for a unit to check for them
                 */
                if (timeseries.hasUnit()) {
                    results.add(createCondensed(timeseries, query));
                }
            }
            return results;

        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<TimeseriesMetadataOutput> getAllExpanded(DbQuery query) throws DataAccessException {
        Session session = getSession();
        try {
            List<TimeseriesMetadataOutput> results = new ArrayList<>();
            SeriesDao<MeasurementSeriesEntity> seriesDao = new SeriesDao<>(session);
            for (MeasurementSeriesEntity timeseries : seriesDao.getAllInstances(query)) {
                /*
                 *  ATM, the SWC REST API only supports numeric types
                 *  We check for a unit to check for them
                 */
                if (timeseries.hasUnit()) {
                    results.add(createExpanded(session, timeseries, query));
                } else {
                    LOGGER.debug("Series entry '{}' without UOM will be ignored!", timeseries.getPkid());
                }
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public TimeseriesMetadataOutput getInstance(String timeseriesId, DbQuery dbQuery) throws DataAccessException {
        Session session = getSession();
        try {
            SeriesDao<MeasurementSeriesEntity> seriesDao = new SeriesDao<>(session);
            MeasurementSeriesEntity result = seriesDao.getInstance(parseId(timeseriesId), dbQuery);
            /*
             *  ATM, the SWC REST API only supports numeric types
             *  We check for a unit to check for them
             */
            if (result == null || !result.hasUnit()) {
                LOGGER.debug("Series entry '{}' without UOM will be ignored!", timeseriesId);
                throw new ResourceNotFoundException("Resource with id '" + timeseriesId + "' could not be found.");
            }
            return createExpanded(session, result, dbQuery);
        } finally {
            returnSession(session);
        }
    }

    public TimeseriesData getData(String timeseriesId, DbQuery dbQuery) throws DataAccessException {
        Session session = getSession();
        try {
            SeriesDao<MeasurementSeriesEntity> seriesDao = new SeriesDao<>(session);
            MeasurementSeriesEntity timeseries = seriesDao.getInstance(parseId(timeseriesId), dbQuery);
            return createTimeseriesData(timeseries, dbQuery, session);
        } finally {
            returnSession(session);
        }
    }

    public TimeseriesData getDataWithReferenceValues(String timeseriesId, DbQuery dbQuery) throws DataAccessException {
        Session session = getSession();
        try {
            SeriesDao<MeasurementSeriesEntity> seriesDao = new SeriesDao<>(session);
            MeasurementSeriesEntity timeseries = seriesDao.getInstance(parseId(timeseriesId), dbQuery);
            TimeseriesData result = createTimeseriesData(timeseries, dbQuery, session);
            Set<MeasurementSeriesEntity> referenceValues = timeseries.getReferenceValues();
            if (referenceValues != null && !referenceValues.isEmpty()) {
                TimeseriesDataMetadata metadata = new TimeseriesDataMetadata();
                metadata.setReferenceValues(assembleReferenceSeries(referenceValues, dbQuery, session));
                result.setMetadata(metadata);
            }
            return result;
        } finally {
            returnSession(session);
        }
    }

    private TimeseriesMetadataOutput createExpanded(Session session, MeasurementSeriesEntity series, DbQuery query) throws DataAccessException {
        TimeseriesMetadataOutput output = createCondensed(series, query);
        output.setParameters(createTimeseriesOutput(series, query));
        output.setReferenceValues(createReferenceValueOutputs(series, query));
        output.setFirstValue(createTimeseriesValueFor(series.getFirstValue(), series));
        output.setLastValue(createTimeseriesValueFor(series.getLastValue(), series));
        return output;
    }

    private ReferenceValueOutput[] createReferenceValueOutputs(MeasurementSeriesEntity series,
            DbQuery query) throws DataAccessException {
        List<ReferenceValueOutput> outputs = new ArrayList<>();
        Set<MeasurementSeriesEntity> referenceValues = series.getReferenceValues();
        for (MeasurementSeriesEntity referenceSeriesEntity : referenceValues) {
            if (referenceSeriesEntity.isPublished()) {
                ReferenceValueOutput refenceValueOutput = new ReferenceValueOutput();
                ProcedureEntity procedure = referenceSeriesEntity.getProcedure();
                refenceValueOutput.setLabel(procedure.getNameI18n(query.getLocale()));
                refenceValueOutput.setReferenceValueId(referenceSeriesEntity.getPkid().toString());

                MeasurementEntity lastValue = series.getLastValue();
                refenceValueOutput.setLastValue(createTimeseriesValueFor(lastValue, series));
                outputs.add(refenceValueOutput);
            }
        }
        return outputs.toArray(new ReferenceValueOutput[0]);
    }

    private TimeseriesMetadataOutput createCondensed(MeasurementSeriesEntity entity, DbQuery query) throws DataAccessException {
        SeriesMetadataV1Output output = new SeriesMetadataV1Output();
        String locale = query.getLocale();
        String stationLabel = getLabelFrom(entity.getFeature(), locale);
        String procedureLabel = getLabelFrom(entity.getProcedure(), locale);
        String phenomenonLabel = getLabelFrom(entity.getPhenomenon(), locale);
        output.setLabel(createTimeseriesLabel(phenomenonLabel, procedureLabel, stationLabel));
        output.setId(entity.getPkid().toString());
        output.setUom(entity.getUnitI18nName(locale));
        output.setStation(createCondensedStation(entity, query));
        return output;
    }

    private String createTimeseriesLabel(String phenomenon, String procedure, String station) {
        StringBuilder sb = new StringBuilder();
        sb.append(phenomenon).append(" ");
        sb.append(procedure).append(", ");
        return sb.append(station).toString();
    }

    private StationOutput createCondensedStation(MeasurementSeriesEntity entity, DbQuery query) throws DataAccessException {
        FeatureEntity feature = entity.getFeature();
        String featurePkid = feature.getPkid().toString();

        // XXX explicit cast here
        return ((StationRepository) stationRepository).getCondensedInstance(featurePkid, query);
    }

    private Map<String, TimeseriesData> assembleReferenceSeries(Set<MeasurementSeriesEntity> referenceValues,
            DbQuery query,
            Session session) throws DataAccessException {
        Map<String, TimeseriesData> referenceSeries = new HashMap<>();
        for (MeasurementSeriesEntity referenceSeriesEntity : referenceValues) {
            if (referenceSeriesEntity.isPublished()) {
                TimeseriesData referenceSeriesData = createTimeseriesData(referenceSeriesEntity, query, session);
                if (haveToExpandReferenceData(referenceSeriesData)) {
                    referenceSeriesData = expandReferenceDataIfNecessary(referenceSeriesEntity, query, session);
                }
                referenceSeries.put(referenceSeriesEntity.getPkid().toString(), referenceSeriesData);
            }
        }
        return referenceSeries;
    }

    private boolean haveToExpandReferenceData(TimeseriesData referenceSeriesData) {
        return referenceSeriesData.getValues().length <= 1;
    }

    private TimeseriesData expandReferenceDataIfNecessary(MeasurementSeriesEntity seriesEntity, DbQuery query, Session session) throws DataAccessException {
        TimeseriesData result = new TimeseriesData();
        ObservationDao<MeasurementEntity> dao = new ObservationDao(session);
        List<MeasurementEntity> observations = dao.getObservationsFor(seriesEntity, query);
        if (!hasValidEntriesWithinRequestedTimespan(observations)) {
            MeasurementEntity lastValidEntity = seriesEntity.getLastValue();
            result.addValues(expandToInterval(query.getTimespan(), lastValidEntity, seriesEntity));
        }

        if (hasSingleValidReferenceValue(observations)) {
            MeasurementEntity entity = observations.get(0);
            result.addValues(expandToInterval(query.getTimespan(), entity, seriesEntity));
        }
        return result;
    }

    private boolean hasValidEntriesWithinRequestedTimespan(List<MeasurementEntity> observations) {
        return observations.size() > 0;
    }

    private boolean hasSingleValidReferenceValue(List<MeasurementEntity> observations) {
        return observations.size() == 1;
    }

    private TimeseriesData createTimeseriesData(MeasurementSeriesEntity seriesEntity, DbQuery query, Session session) throws DataAccessException {
        TimeseriesData result = new TimeseriesData();
        ObservationDao<MeasurementEntity> dao = new ObservationDao(session);
        List<MeasurementEntity> observations = dao.getAllInstancesFor(seriesEntity, query);
        for (MeasurementEntity observation : observations) {
            if (observation != null) {
                result.addValues(createTimeseriesValueFor(observation, seriesEntity));
            }
        }
        return result;
    }

    private TimeseriesValue[] expandToInterval(Interval interval, MeasurementEntity entity, MeasurementSeriesEntity series) {
        MeasurementEntity referenceStart = new MeasurementEntity();
        MeasurementEntity referenceEnd = new MeasurementEntity();
        referenceStart.setTimestamp(interval.getStart().toDate());
        referenceEnd.setTimestamp(interval.getEnd().toDate());
        referenceStart.setValue(entity.getValue());
        referenceEnd.setValue(entity.getValue());
        return new TimeseriesValue[]{createTimeseriesValueFor(referenceStart, series),
            createTimeseriesValueFor(referenceEnd, series)};

    }

    private TimeseriesValue createTimeseriesValueFor(MeasurementEntity observation, MeasurementSeriesEntity series) {
        if (observation == null) {
            // do not fail on empty observations
            return null;
        }
        TimeseriesValue value = new TimeseriesValue();
        value.setTimestamp(observation.getTimestamp().getTime());
        Double observationValue = !getServiceInfo().isNoDataValue(observation)
                ? formatDecimal(observation.getValue(), series)
                : Double.NaN;
        value.setValue(observationValue);
        return value;
    }

    private Double formatDecimal(Double value, MeasurementSeriesEntity series) {
        int scale = series.getNumberOfDecimals();
        return new BigDecimal(value)
                .setScale(scale, HALF_UP)
                .doubleValue();
    }

}
