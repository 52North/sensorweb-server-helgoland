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
package org.n52.series.db.da;

import org.n52.series.db.dao.DbQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.joda.time.Interval;
import org.n52.io.response.series.count.CountObservationData;
import org.n52.io.response.series.count.CountObservationDataMetadata;
import org.n52.io.response.series.count.CountObservationValue;
import org.n52.io.response.v1.ext.ObservationType;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.CountDatasetEntity;
import org.n52.series.db.dao.ObservationDao;
import org.n52.series.db.dao.SeriesDao;

public class CountDataRepository extends AbstractDataRepository<CountObservationData, CountDatasetEntity> {

    @Override
    public Class<CountDatasetEntity> getEntityType() {
        return CountDatasetEntity.class;
    }

    @Override
    public CountObservationData getData(String seriesId, DbQuery dbQuery) throws DataAccessException {
        Session session = getSession();
        try {
            SeriesDao<CountDatasetEntity> seriesDao = new SeriesDao<>(session, CountDatasetEntity.class);
            String id = ObservationType.extractId(seriesId);
            CountDatasetEntity series = seriesDao.getInstance(parseId(id), dbQuery);
            return dbQuery.isExpanded()
                ? assembleDataWithReferenceValues(series, dbQuery, session)
                : assembleData(series, dbQuery, session);
        }
        finally {
            returnSession(session);
        }
    }

    private CountObservationData assembleDataWithReferenceValues(CountDatasetEntity timeseries,
                                                            DbQuery dbQuery,
                                                            Session session) throws DataAccessException {
        CountObservationData result = assembleData(timeseries, dbQuery, session);
        Set<CountDatasetEntity> referenceValues = timeseries.getReferenceValues();
        if (referenceValues != null && !referenceValues.isEmpty()) {
            CountObservationDataMetadata metadata = new CountObservationDataMetadata();
            metadata.setReferenceValues(assembleReferenceSeries(referenceValues, dbQuery, session));
            result.setMetadata(metadata);
        }
        return result;
    }

    private Map<String, CountObservationData> assembleReferenceSeries(Set<CountDatasetEntity> referenceValues,
                                                                 DbQuery query,
                                                                 Session session) throws DataAccessException {
        Map<String, CountObservationData> referenceSeries = new HashMap<>();
        for (CountDatasetEntity referenceSeriesEntity : referenceValues) {
            if (referenceSeriesEntity.isPublished()) {
                CountObservationData referenceSeriesData = assembleData(referenceSeriesEntity, query, session);
                if (haveToExpandReferenceData(referenceSeriesData)) {
                    referenceSeriesData = expandReferenceDataIfNecessary(referenceSeriesEntity, query, session);
                }
                referenceSeries.put(referenceSeriesEntity.getPkid().toString(), referenceSeriesData);
            }
        }
        return referenceSeries;
    }

    private boolean haveToExpandReferenceData(CountObservationData referenceSeriesData) {
        return referenceSeriesData.getValues().length <= 1;
    }

    private CountObservationData expandReferenceDataIfNecessary(CountDatasetEntity seriesEntity, DbQuery query, Session session) throws DataAccessException {
        CountObservationData result = new CountObservationData();
        ObservationDao<CountDataEntity> dao = new ObservationDao<>(session);
        List<CountDataEntity> observations = dao.getAllInstancesFor(seriesEntity, query);
        if (!hasValidEntriesWithinRequestedTimespan(observations)) {
            CountDataEntity lastValidEntity = seriesEntity.getLastValue();
            result.addValues(expandToInterval(query.getTimespan(), lastValidEntity, seriesEntity));
        }

        if (hasSingleValidReferenceValue(observations)) {
            CountDataEntity entity = observations.get(0);
            result.addValues(expandToInterval(query.getTimespan(), entity, seriesEntity));
        }
        return result;
    }

    private CountObservationData assembleData(CountDatasetEntity seriesEntity, DbQuery query, Session session) throws DataAccessException {
        CountObservationData result = new CountObservationData();
        ObservationDao<CountDataEntity> dao = new ObservationDao<>(session);
        List<CountDataEntity> observations = dao.getAllInstancesFor(seriesEntity, query);
        for (CountDataEntity observation : observations) {
            if (observation != null) {
                result.addValues(createSeriesValueFor(observation, seriesEntity));
            }
        }
        return result;
    }

    private CountObservationValue[] expandToInterval(Interval interval, CountDataEntity entity, CountDatasetEntity series) {
        CountDataEntity referenceStart = new CountDataEntity();
        CountDataEntity referenceEnd = new CountDataEntity();
        referenceStart.setTimestamp(interval.getStart().toDate());
        referenceEnd.setTimestamp(interval.getEnd().toDate());
        referenceStart.setValue(entity.getValue());
        referenceEnd.setValue(entity.getValue());
        return new CountObservationValue[]{createSeriesValueFor(referenceStart, series),
            createSeriesValueFor(referenceEnd, series)};

    }

    CountObservationValue createSeriesValueFor(CountDataEntity observation, CountDatasetEntity series) {
        if (observation == null) {
            // do not fail on empty observations
            return null;
        }
        CountObservationValue value = new CountObservationValue();
        value.setTimestamp(observation.getTimestamp().getTime());
        value.setValue(observation.getValue());
        addGeometry(observation, value);
        addValidTime(observation, value);
        return value;
    }


}
