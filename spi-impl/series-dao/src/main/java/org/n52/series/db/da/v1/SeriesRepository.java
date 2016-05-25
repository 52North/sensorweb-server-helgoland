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
import java.util.List;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.TimeseriesValue;
import org.n52.io.response.v1.ext.MeasurementSeriesOutput;
import org.n52.io.response.v1.ext.ObservationType;
import org.n52.io.response.v1.ext.SeriesMetadataOutput;
import org.n52.io.response.v1.ext.SeriesParameters;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.ext.AbstractObservationEntity;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;
import org.n52.series.db.da.beans.ext.MeasurementEntity;
import org.n52.series.db.da.beans.ext.MeasurementSeriesEntity;
import org.n52.series.db.da.dao.v1.ObservationDao;
import org.n52.series.db.da.dao.v1.SeriesDao;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class SeriesRepository extends ExtendedSessionAwareRepository implements OutputAssembler<SeriesMetadataOutput> {

    @Override
    public List<SeriesMetadataOutput> getAllCondensed(DbQuery query) throws DataAccessException {
        Session session = getSession();
        try {
            List<SeriesMetadataOutput> results = new ArrayList<>();
            // TODO backward compatibility (only MeasurementSeriesEntity)
            SeriesDao<AbstractSeriesEntity<AbstractObservationEntity>> seriesDao = new SeriesDao(session);
            for (AbstractSeriesEntity series : seriesDao.getAllInstances(query)) {
                results.add(createCondensed(series, query));
            }
            return results;

        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<SeriesMetadataOutput> getAllExpanded(DbQuery query) throws DataAccessException {
        Session session = getSession();
        try {
            List<SeriesMetadataOutput> results = new ArrayList<>();
            // TODO backward compatibility (only MeasurementSeriesEntity)
            SeriesDao<AbstractSeriesEntity<AbstractObservationEntity>>  seriesDao = new SeriesDao(session);
            for (AbstractSeriesEntity<AbstractObservationEntity> series : seriesDao.getAllInstances(query)) {
                results.add(createExpanded(series, query, session));
            }
            return results;
        } finally {
            returnSession(session);
        }
    }

    @Override
    public SeriesMetadataOutput getInstance(String id, DbQuery query) throws DataAccessException {
        Session session = getSession();
        try {
            List<SeriesMetadataOutput> results = new ArrayList<>();
            SeriesDao seriesDao = new SeriesDao(session);
            String seriesId = ObservationType.extractId(id);
            AbstractSeriesEntity<AbstractObservationEntity> series = seriesDao.getInstance(Long.parseLong(seriesId), query);
            return createExpanded(series, query, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public Collection<SearchResult> searchFor(IoParameters paramters) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SearchResult> convertToSearchResults(List<? extends DescribableEntity> found, String locale) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private SeriesMetadataOutput createCondensed(AbstractSeriesEntity<?> series, DbQuery query) throws DataAccessException {
        if (series instanceof MeasurementSeriesEntity) {
            MeasurementSeriesOutput output = new MeasurementSeriesOutput();
            MeasurementSeriesEntity measurementSeries = (MeasurementSeriesEntity) series;
            output.setLabel(createSeriesLabel(series, query.getLocale()));
            output.setId(series.getPkid().toString());
            output.setHrefBase(urHelper.getSeriesHrefBaseUrl(query.getHrefBase()));
            return output;
        }
        return null;
    }

    private SeriesMetadataOutput createExpanded(AbstractSeriesEntity<?> series, DbQuery query, Session session) throws DataAccessException {
        SeriesMetadataOutput result = createCondensed(series, query);
        result.setSeriesParameters(getParameters(series, query));
        if (series instanceof MeasurementSeriesEntity && result instanceof MeasurementSeriesOutput) {
            MeasurementSeriesEntity measurementSeries = (MeasurementSeriesEntity) series;
            MeasurementSeriesOutput output = (MeasurementSeriesOutput) result;
            output.setUom(measurementSeries.getUnitI18nName(query.getLocale()));
            output.setFirstValue(createTimeseriesValueFor(measurementSeries.getFirstValue(), measurementSeries));
            output.setLastValue(createTimeseriesValueFor(measurementSeries.getLastValue(), measurementSeries));
        }
        return result;
    }

    private SeriesParameters getParameters(AbstractSeriesEntity<?> series, DbQuery query) throws DataAccessException {
        return createSeriesParameters(series, query);
    }

    private String createSeriesLabel(AbstractSeriesEntity<?> series, String locale) {
        String station = getLabelFrom(series.getFeature(), locale);
        String procedure = getLabelFrom(series.getProcedure(), locale);
        String phenomenon = getLabelFrom(series.getPhenomenon(), locale);
        StringBuilder sb = new StringBuilder();
        sb.append(phenomenon).append(" ");
        sb.append(procedure).append(", ");
        return sb.append(station).toString();
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

    /**
     * Query observations for timestamp
     * @param observation
     * @param series
     * @param query
     * @return
     */
    private TimeseriesValue queryObservationFor(AbstractObservationEntity observation, AbstractSeriesEntity series, DbQuery query) {
        if (observation == null) {
            // do not fail on empty observations
            return null;
        }
        List<AbstractObservationEntity> observations = new ObservationDao(getSession()).getInstancesFor(observation.getTimestamp(), series, query);
        if (observations != null && !observations.isEmpty()) {
            if (series instanceof MeasurementSeriesEntity) {
                return createTimeseriesValueFor((MeasurementEntity)observations.iterator().next(), (MeasurementSeriesEntity)series);
            }
        }
        return null;
    }

    private Double formatDecimal(Double value, MeasurementSeriesEntity series) {
        int scale = series.getNumberOfDecimals();
        return new BigDecimal(value)
                .setScale(scale, HALF_UP)
                .doubleValue();
    }
}
