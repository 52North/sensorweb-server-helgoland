/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;
import org.n52.io.response.dataset.record.RecordData;
import org.n52.io.response.dataset.record.RecordDatasetMetadata;
import org.n52.io.response.dataset.record.RecordValue;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.RecordDataEntity;
import org.n52.series.db.beans.RecordDatasetEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.dao.DataDao;
import org.n52.series.db.dao.DbQuery;

public class RecordDataRepository extends AbstractDataRepository<RecordData, RecordDatasetEntity, RecordDataEntity, RecordValue> {

    @Override
    public Class<RecordDatasetEntity> getEntityType() {
        return RecordDatasetEntity.class;
    }

    @Override
    protected RecordData assembleDataWithReferenceValues(RecordDatasetEntity timeseries,
                                                       DbQuery dbQuery,
                                                       Session session)
            throws DataAccessException {
        RecordData result = assembleData(timeseries, dbQuery, session);
        Set<RecordDatasetEntity> referenceValues = timeseries.getReferenceValues();
        if (referenceValues != null && !referenceValues.isEmpty()) {
            RecordDatasetMetadata metadata = new RecordDatasetMetadata();
            metadata.setReferenceValues(assembleReferenceSeries(referenceValues, dbQuery, session));
            result.setMetadata(metadata);
        }
        return result;
    }

    private Map<String, RecordData> assembleReferenceSeries(Set<RecordDatasetEntity> referenceValues,
                                                          DbQuery query,
                                                          Session session)
            throws DataAccessException {
        Map<String, RecordData> referenceSeries = new HashMap<>();
        for (RecordDatasetEntity referenceSeriesEntity : referenceValues) {
            if (referenceSeriesEntity.isPublished()) {
                RecordData referenceSeriesData = assembleData(referenceSeriesEntity, query, session);
                if (haveToExpandReferenceData(referenceSeriesData)) {
                    referenceSeriesData = expandReferenceDataIfNecessary(referenceSeriesEntity, query, session);
                }
                referenceSeries.put(referenceSeriesEntity.getPkid().toString(), referenceSeriesData);
            }
        }
        return referenceSeries;
    }

    private boolean haveToExpandReferenceData(RecordData referenceSeriesData) {
        return referenceSeriesData.getValues().size() <= 1;
    }

    private RecordData expandReferenceDataIfNecessary(RecordDatasetEntity seriesEntity, DbQuery query, Session session)
            throws DataAccessException {
        RecordData result = new RecordData();
        DataDao<RecordDataEntity> dao = new DataDao<>(session);
        List<RecordDataEntity> observations = dao.getAllInstancesFor(seriesEntity, query);
        if ( !hasValidEntriesWithinRequestedTimespan(observations)) {
            RecordValue lastValidValue = getLastValue(seriesEntity, session, query);
            result.addValues(expandToInterval(lastValidValue.getValue(), seriesEntity, query));
        }

        if (hasSingleValidReferenceValue(observations)) {
            RecordDataEntity entity = observations.get(0);
            result.addValues(expandToInterval(entity.getValue(), seriesEntity, query));
        }
        return result;
    }

    @Override
    protected RecordData assembleData(RecordDatasetEntity seriesEntity, DbQuery query, Session session)
            throws DataAccessException {
        RecordData result = new RecordData();
        DataDao<RecordDataEntity> dao = new DataDao<>(session);
        List<RecordDataEntity> observations = dao.getAllInstancesFor(seriesEntity, query);
        for (RecordDataEntity observation : observations) { // XXX n times same object?
            if (observation != null) {
                result.addValues(createSeriesValueFor(observation, seriesEntity, query));
            }
        }
        return result;
    }

    // XXX
    private RecordValue[] expandToInterval(Map<String, Object> value, RecordDatasetEntity series, DbQuery query) {
        RecordDataEntity referenceStart = new RecordDataEntity();
        RecordDataEntity referenceEnd = new RecordDataEntity();
        referenceStart.setTimestamp(query.getTimespan().getStart().toDate());
        referenceEnd.setTimestamp(query.getTimespan().getEnd().toDate());
        referenceStart.setValue(value);
        referenceEnd.setValue(value);
        return new RecordValue[] {
                                createSeriesValueFor(referenceStart, series, query),
                                createSeriesValueFor(referenceEnd, series, query)
        };

    }

    @Override
    public RecordValue createSeriesValueFor(RecordDataEntity observation, RecordDatasetEntity series, DbQuery query) {
        if (observation == null) {
            // do not fail on empty observations
            return null;
        }

        long timeend = observation.getTimeend().getTime();
        long timestart = observation.getTimestart().getTime();
        Map<String, Object> observationValue = !series.getService().isNoDataValue(observation)
                ? observation.getValue()
                : null;
        RecordValue value = query.getParameters().isShowTimeIntervals()
                ? new RecordValue(timestart, timeend, observationValue)
                : new RecordValue(timeend, observationValue);

        if (query.isExpanded()) {
            addGeometry(observation, value);
            addValidTime(observation, value);
            addParameters(observation, value);
        } else if (series.getPlatform().isMobile()) {
            addGeometry(observation, value);
        }
        return value;
    }

}
