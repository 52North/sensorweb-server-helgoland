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

import java.math.BigDecimal;
import static java.math.RoundingMode.HALF_UP;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.Session;
import org.n52.io.response.dataset.measurement.MeasurementData;
import org.n52.io.response.dataset.measurement.MeasurementDatasetMetadata;
import org.n52.io.response.dataset.measurement.MeasurementValue;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.MeasurementDataEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.series.db.dao.DataDao;
import org.n52.series.db.dao.DbQuery;

public class MeasurementDataRepository extends AbstractDataRepository<MeasurementData, MeasurementDatasetEntity, MeasurementDataEntity, MeasurementValue> {

    @Override
    public Class<MeasurementDatasetEntity> getEntityType() {
        return MeasurementDatasetEntity.class;
    }

    @Override
    protected MeasurementData assembleDataWithReferenceValues(MeasurementDatasetEntity timeseries,
                                                            DbQuery dbQuery,
                                                            Session session) throws DataAccessException {
        MeasurementData result = assembleData(timeseries, dbQuery, session);
        Set<MeasurementDatasetEntity> referenceValues = timeseries.getReferenceValues();
        if (referenceValues != null && !referenceValues.isEmpty()) {
            MeasurementDatasetMetadata metadata = new MeasurementDatasetMetadata();
            metadata.setReferenceValues(assembleReferenceSeries(referenceValues, dbQuery, session));
            result.setMetadata(metadata);
        }
        return result;
    }

    private Map<String, MeasurementData> assembleReferenceSeries(Set<MeasurementDatasetEntity> referenceValues,
                                                                 DbQuery query,
                                                                 Session session) throws DataAccessException {
        Map<String, MeasurementData> referenceSeries = new HashMap<>();
        for (MeasurementDatasetEntity referenceSeriesEntity : referenceValues) {
            if (referenceSeriesEntity.isPublished()) {
                MeasurementData referenceSeriesData = assembleData(referenceSeriesEntity, query, session);
                if (haveToExpandReferenceData(referenceSeriesData)) {
                    referenceSeriesData = expandReferenceDataIfNecessary(referenceSeriesEntity, query, session);
                }
                referenceSeries.put(referenceSeriesEntity.getPkid().toString(), referenceSeriesData);
            }
        }
        return referenceSeries;
    }

    private boolean haveToExpandReferenceData(MeasurementData referenceSeriesData) {
        return referenceSeriesData.getValues().size() <= 1;
    }

    private MeasurementData expandReferenceDataIfNecessary(MeasurementDatasetEntity seriesEntity, DbQuery query, Session session) throws DataAccessException {
        MeasurementData result = new MeasurementData();
        DataDao<MeasurementDataEntity> dao = createDataDao(session);
        List<MeasurementDataEntity> observations = dao.getAllInstancesFor(seriesEntity, query);
        if (!hasValidEntriesWithinRequestedTimespan(observations)) {
            MeasurementValue lastValue = getLastValue(seriesEntity, session, query);
            result.addValues(expandToInterval(lastValue.getValue(), seriesEntity, query));
        }

        if (hasSingleValidReferenceValue(observations)) {
            MeasurementDataEntity entity = observations.get(0);
            result.addValues(expandToInterval(entity.getValue(), seriesEntity, query));
        }
        return result;
    }

    @Override
    protected MeasurementData assembleData(MeasurementDatasetEntity seriesEntity, DbQuery query, Session session) throws DataAccessException {
        MeasurementData result = new MeasurementData();
        DataDao<MeasurementDataEntity> dao = createDataDao(session);
        List<MeasurementDataEntity> observations = dao.getAllInstancesFor(seriesEntity, query);
        for (MeasurementDataEntity observation : observations) {
            if (observation != null) {
                result.addValues(createSeriesValueFor(observation, seriesEntity, query));
            }
        }
        return result;
    }

    private MeasurementValue[] expandToInterval(Double value, MeasurementDatasetEntity series, DbQuery query) {
        MeasurementDataEntity referenceStart = new MeasurementDataEntity();
        MeasurementDataEntity referenceEnd = new MeasurementDataEntity();
        referenceStart.setTimestamp(query.getTimespan().getStart().toDate());
        referenceEnd.setTimestamp(query.getTimespan().getEnd().toDate());
        referenceStart.setValue(value);
        referenceEnd.setValue(value);
        return new MeasurementValue[]{createSeriesValueFor(referenceStart, series, query),
            createSeriesValueFor(referenceEnd, series, query)};

    }

    @Override
    public MeasurementValue createSeriesValueFor(MeasurementDataEntity observation, MeasurementDatasetEntity series, DbQuery query) {
        if (observation == null) {
            // do not fail on empty observations
            return null;
        }

        long timeend = observation.getTimeend().getTime();
        long timestart = observation.getTimestart().getTime();
        Double observationValue = !series.getService().isNoDataValue(observation)
                ? format(observation, series)
                : null;
        MeasurementValue value = query.getParameters().isShowTimeIntervals()
                ? new MeasurementValue(timestart, timeend, observationValue)
                : new MeasurementValue(timeend, observationValue);

        if (query.isExpanded()) {
            addGeometry(observation, value);
            addValidTime(observation, value);
            addParameters(observation, value);
        } else if (series.getPlatform().isMobile()) {
            addGeometry(observation, value);
        }
        return value;
    }

    private Double format(MeasurementDataEntity observation, MeasurementDatasetEntity series) {
        if (observation.getValue() == null) {
            return observation.getValue();
        }
        int scale = series.getNumberOfDecimals();
        return new BigDecimal(observation.getValue())
                .setScale(scale, HALF_UP)
                .doubleValue();
    }

}
