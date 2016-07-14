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
import org.n52.io.response.series.text.TextObservationData;
import org.n52.io.response.series.text.TextObservationDataMetadata;
import org.n52.io.response.series.text.TextObservationValue;
import org.n52.io.response.v1.ext.ObservationType;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.series.db.beans.TextDatasetEntity;
import org.n52.series.db.dao.ObservationDao;
import org.n52.series.db.dao.SeriesDao;
import org.n52.web.exception.ResourceNotFoundException;

public class TextDataRepository extends AbstractDataRepository<TextObservationData, TextDataEntity> {

    @Override
    public Class<TextDataEntity> getEntityType() {
        return TextDataEntity.class;
    }

    @Override
    public TextObservationData getData(String seriesId, DbQuery dbQuery) throws DataAccessException {
        Session session = getSession();
        try {
            SeriesDao<TextDatasetEntity> seriesDao = new SeriesDao<>(session, TextDatasetEntity.class);
            String id = ObservationType.extractId(seriesId);
            TextDatasetEntity series = seriesDao.getInstance(parseId(id), dbQuery);
            if (series == null) {
                throw new ResourceNotFoundException("Resource with id '" + seriesId + "' could not be found.");
            }
            return dbQuery.isExpanded()
                ? assembleDataWithReferenceValues(series, dbQuery, session)
                : assembleData(series, dbQuery, session);
        }
        finally {
            returnSession(session);
        }
    }

    private TextObservationData assembleDataWithReferenceValues(TextDatasetEntity timeseries,
                                                            DbQuery dbQuery,
                                                            Session session) throws DataAccessException {
        TextObservationData result = assembleData(timeseries, dbQuery, session);
        Set<TextDatasetEntity> referenceValues = timeseries.getReferenceValues();
        if (referenceValues != null && !referenceValues.isEmpty()) {
            TextObservationDataMetadata metadata = new TextObservationDataMetadata();
            metadata.setReferenceValues(assembleReferenceSeries(referenceValues, dbQuery, session));
            result.setMetadata(metadata);
        }
        return result;
    }

    private Map<String, TextObservationData> assembleReferenceSeries(Set<TextDatasetEntity> referenceValues,
                                                                 DbQuery query,
                                                                 Session session) throws DataAccessException {
        Map<String, TextObservationData> referenceSeries = new HashMap<>();
        for (TextDatasetEntity referenceSeriesEntity : referenceValues) {
            if (referenceSeriesEntity.isPublished()) {
                TextObservationData referenceSeriesData = assembleData(referenceSeriesEntity, query, session);
                if (haveToExpandReferenceData(referenceSeriesData)) {
                    referenceSeriesData = expandReferenceDataIfNecessary(referenceSeriesEntity, query, session);
                }
                referenceSeries.put(referenceSeriesEntity.getPkid().toString(), referenceSeriesData);
            }
        }
        return referenceSeries;
    }

    private boolean haveToExpandReferenceData(TextObservationData referenceSeriesData) {
        return referenceSeriesData.getValues().length <= 1;
    }

    private TextObservationData expandReferenceDataIfNecessary(TextDatasetEntity seriesEntity, DbQuery query, Session session) throws DataAccessException {
        TextObservationData result = new TextObservationData();
        ObservationDao<TextDataEntity> dao = new ObservationDao<>(session);
        List<TextDataEntity> observations = dao.getAllInstancesFor(seriesEntity, query);
        if (!hasValidEntriesWithinRequestedTimespan(observations)) {
            TextDataEntity lastValidEntity = seriesEntity.getLastValue();
            result.addValues(expandToInterval(query.getTimespan(), lastValidEntity, seriesEntity));
        }

        if (hasSingleValidReferenceValue(observations)) {
            TextDataEntity entity = observations.get(0);
            result.addValues(expandToInterval(query.getTimespan(), entity, seriesEntity));
        }
        return result;
    }

    private TextObservationData assembleData(TextDatasetEntity seriesEntity, DbQuery query, Session session) throws DataAccessException {
        TextObservationData result = new TextObservationData();
        ObservationDao<TextDataEntity> dao = new ObservationDao<>(session);
        List<TextDataEntity> observations = dao.getAllInstancesFor(seriesEntity, query);
        for (TextDataEntity observation : observations) {
            if (observation != null) {
                result.addValues(createSeriesValueFor(observation, seriesEntity));
            }
        }
        return result;
    }

    private TextObservationValue[] expandToInterval(Interval interval, TextDataEntity entity, TextDatasetEntity series) {
        TextDataEntity referenceStart = new TextDataEntity();
        TextDataEntity referenceEnd = new TextDataEntity();
        referenceStart.setTimestamp(interval.getStart().toDate());
        referenceEnd.setTimestamp(interval.getEnd().toDate());
        referenceStart.setValue(entity.getValue());
        referenceEnd.setValue(entity.getValue());
        return new TextObservationValue[]{createSeriesValueFor(referenceStart, series),
            createSeriesValueFor(referenceEnd, series)};

    }

    TextObservationValue createSeriesValueFor(TextDataEntity observation, TextDatasetEntity series) {
        if (observation == null) {
            // do not fail on empty observations
            return null;
        }
        TextObservationValue value = new TextObservationValue();
        value.setTimestamp(observation.getTimestamp().getTime());
        value.setValue(observation.getValue());
        addGeometry(observation, value);
        addValidTime(observation, value);
        return value;
    }

}
