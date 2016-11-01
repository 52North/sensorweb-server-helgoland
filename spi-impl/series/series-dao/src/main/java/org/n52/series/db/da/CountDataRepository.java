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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.n52.io.response.dataset.count.CountData;
import org.n52.io.response.dataset.count.CountDatasetMetadata;
import org.n52.io.response.dataset.count.CountValue;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.CountDatasetEntity;
import org.n52.series.db.dao.DataDao;
import org.n52.series.db.dao.DbQuery;

public class CountDataRepository extends AbstractDataRepository<CountData, CountDatasetEntity, CountDataEntity, CountValue> {

    @Override
    public Class<CountDatasetEntity> getEntityType() {
        return CountDatasetEntity.class;
    }

    @Override
    protected CountData assembleDataWithReferenceValues(CountDatasetEntity timeseries,
                                                            DbQuery dbQuery,
                                                            Session session) throws DataAccessException {
        CountData result = assembleData(timeseries, dbQuery, session);
        Set<CountDatasetEntity> referenceValues = timeseries.getReferenceValues();
        if (referenceValues != null && !referenceValues.isEmpty()) {
            CountDatasetMetadata metadata = new CountDatasetMetadata();
            metadata.setReferenceValues(assembleReferenceSeries(referenceValues, dbQuery, session));
            result.setMetadata(metadata);
        }
        return result;
    }

    private Map<String, CountData> assembleReferenceSeries(Set<CountDatasetEntity> referenceValues,
                                                                 DbQuery query,
                                                                 Session session) throws DataAccessException {
        Map<String, CountData> referenceSeries = new HashMap<>();
        for (CountDatasetEntity referenceSeriesEntity : referenceValues) {
            if (referenceSeriesEntity.isPublished()) {
                CountData referenceSeriesData = assembleData(referenceSeriesEntity, query, session);
                if (haveToExpandReferenceData(referenceSeriesData)) {
                    referenceSeriesData = expandReferenceDataIfNecessary(referenceSeriesEntity, query, session);
                }
                referenceSeries.put(referenceSeriesEntity.getPkid().toString(), referenceSeriesData);
            }
        }
        return referenceSeries;
    }

    private boolean haveToExpandReferenceData(CountData referenceSeriesData) {
        return referenceSeriesData.getValues().size() <= 1;
    }

    private CountData expandReferenceDataIfNecessary(CountDatasetEntity seriesEntity, DbQuery query, Session session) throws DataAccessException {
        CountData result = new CountData();
        DataDao<CountDataEntity> dao = createDataDao(session);
        List<CountDataEntity> observations = dao.getAllInstancesFor(seriesEntity, query);
        if (!hasValidEntriesWithinRequestedTimespan(observations)) {
            CountValue lastValue = getLastValue(seriesEntity, session, query);
            result.addValues(expandToInterval(lastValue.getValue(), seriesEntity, query));
        }

        if (hasSingleValidReferenceValue(observations)) {
            CountDataEntity entity = observations.get(0);
            result.addValues(expandToInterval(entity.getValue(), seriesEntity, query));
        }
        return result;
    }

    @Override
    protected CountData assembleData(CountDatasetEntity seriesEntity, DbQuery query, Session session) throws DataAccessException {
        CountData result = new CountData();
        DataDao<CountDataEntity> dao = createDataDao(session);
        List<CountDataEntity> observations = dao.getAllInstancesFor(seriesEntity, query);
        for (CountDataEntity observation : observations) {
            if (observation != null) {
                result.addValues(createSeriesValueFor(observation, seriesEntity, query));
            }
        }
        return result;
    }

    private CountValue[] expandToInterval(Integer value, CountDatasetEntity series, DbQuery query) {
        CountDataEntity referenceStart = new CountDataEntity();
        CountDataEntity referenceEnd = new CountDataEntity();
        referenceStart.setTimestamp(query.getTimespan().getStart().toDate());
        referenceEnd.setTimestamp(query.getTimespan().getEnd().toDate());
        referenceStart.setValue(value);
        referenceEnd.setValue(value);
        return new CountValue[]{createSeriesValueFor(referenceStart, series, query),
            createSeriesValueFor(referenceEnd, series, query)};

    }

    @Override
    public CountValue createSeriesValueFor(CountDataEntity observation, CountDatasetEntity series, DbQuery query) {
        if (observation == null) {
            // do not fail on empty observations
            return null;
        }

        Integer observationValue = !getServiceInfo().isNoDataValue(observation)
                ? observation.getValue()
                : null;

        CountValue value = new CountValue();
        value.setTimestamp(observation.getTimestamp().getTime());
        value.setValue(observationValue);
        if (query.isExpanded()) {
            addGeometry(observation, value);
            addValidTime(observation, value);
        } else if (series.getPlatform().isMobile()) {
            addGeometry(observation, value);
        }
        return value;
    }

}
