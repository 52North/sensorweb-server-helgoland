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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.hibernate.Session;
import org.n52.io.DatasetFactoryException;
import org.n52.io.request.IoParameters;
import org.n52.io.response.StationOutput;
import org.n52.io.response.TimeseriesMetadataOutput;
import org.n52.io.response.dataset.measurement.MeasurementReferenceValueOutput;
import org.n52.series.db.DataAccessException;
import org.n52.series.db.beans.DescribableEntity;
import org.n52.series.db.beans.FeatureEntity;
import org.n52.series.db.beans.MeasurementDataEntity;
import org.n52.series.db.beans.MeasurementDatasetEntity;
import org.n52.series.db.beans.ProcedureEntity;
import org.n52.series.db.dao.DatasetDao;
import org.n52.series.db.dao.DbQuery;
import org.n52.series.spi.search.SearchResult;
import org.n52.series.spi.search.TimeseriesSearchResult;
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
public class TimeseriesRepository extends SessionAwareRepository implements OutputAssembler<TimeseriesMetadataOutput> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeseriesRepository.class);

    @Autowired
    @Qualifier(value = "stationRepository")
    private OutputAssembler<StationOutput> stationRepository;

    @Autowired
    private IDataRepositoryFactory factory;

    @Override
    public boolean exists(String id, DbQuery parameters) throws DataAccessException {
        Session session = getSession();
        try {
            DatasetDao<MeasurementDatasetEntity> dao = createDao(session);
            return dao.hasInstance(parseId(id), parameters, MeasurementDatasetEntity.class);
        } finally {
            returnSession(session);
        }
    }

    private DatasetDao<MeasurementDatasetEntity> createDao(Session session) {
        return new DatasetDao<>(session, MeasurementDatasetEntity.class);
    }

    @Override
    public Collection<SearchResult> searchFor(IoParameters parameters) {
        Session session = getSession();
        try {
            DatasetDao<MeasurementDatasetEntity> seriesDao = createDao(session);
            DbQuery query = dbQueryFactory.createFrom(parameters);
            List<MeasurementDatasetEntity> found = seriesDao.find(query);
            return convertToResults(found, query.getLocale());
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<SearchResult> convertToSearchResults(List< ? extends DescribableEntity> found,
            DbQuery query) {
        // not needed, use #convertToResults() instead

        // TODO fix interface here

        return Collections.emptyList();
    }

    private List<SearchResult> convertToResults(List<MeasurementDatasetEntity> found, String locale) {
        List<SearchResult> results = new ArrayList<>();
        for (MeasurementDatasetEntity searchResult : found) {
            String pkid = searchResult.getPkid().toString();
            String phenomenonLabel = searchResult.getPhenomenon().getLabelFrom(locale);
            String procedureLabel = searchResult.getProcedure().getLabelFrom(locale);
            String stationLabel = searchResult.getFeature().getLabelFrom(locale);
            String offeringLabel = searchResult.getOffering().getLabelFrom(locale);
            String label = createTimeseriesLabel(phenomenonLabel, procedureLabel, stationLabel, offeringLabel);
            results.add(new TimeseriesSearchResult(pkid, label));
        }
        return results;
    }

    @Override
    public List<TimeseriesMetadataOutput> getAllCondensed(DbQuery query) throws DataAccessException {
        Session session = getSession();
        try {
            return getAllCondensed(query, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<TimeseriesMetadataOutput> getAllCondensed(DbQuery query, Session session) throws DataAccessException {
        List<TimeseriesMetadataOutput> results = new ArrayList<>();
        DatasetDao<MeasurementDatasetEntity> seriesDao = createDao(session);
        for (MeasurementDatasetEntity timeseries : seriesDao.getAllInstances(query)) {
            if (timeseries.hasUnit()) {
                results.add(createCondensed(timeseries, query, session));
            }
        }
        return results;
    }

    @Override
    public List<TimeseriesMetadataOutput> getAllExpanded(DbQuery query) throws DataAccessException {
        Session session = getSession();
        try {
            return getAllExpanded(query, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<TimeseriesMetadataOutput> getAllExpanded(DbQuery query, Session session) throws DataAccessException {
        List<TimeseriesMetadataOutput> results = new ArrayList<>();
        DatasetDao<MeasurementDatasetEntity> seriesDao = createDao(session);
        for (MeasurementDatasetEntity timeseries : seriesDao.getAllInstances(query)) {
            if (timeseries.hasUnit()) {
                results.add(createExpanded(timeseries, query, session));
            } else {
                LOGGER.debug("Series entry '{}' without UOM will be ignored!", timeseries.getPkid());
            }
        }
        return results;
    }

    @Override
    public TimeseriesMetadataOutput getInstance(String timeseriesId, DbQuery dbQuery) throws DataAccessException {
        Session session = getSession();
        try {
            return getInstance(timeseriesId, dbQuery, session);
        } finally {
            returnSession(session);
        }
    }

    @Override
    public TimeseriesMetadataOutput getInstance(String timeseriesId, DbQuery dbQuery, Session session) throws DataAccessException {
        DatasetDao<MeasurementDatasetEntity> seriesDao = createDao(session);
        MeasurementDatasetEntity result = seriesDao.getInstance(parseId(timeseriesId), dbQuery);
        if (result == null || !result.hasUnit()) {
            LOGGER.debug("Series entry '{}' without UOM will be ignored!", timeseriesId);
            throw new ResourceNotFoundException("Resource with id '" + timeseriesId + "' could not be found.");
        }
        return createExpanded(result, dbQuery, session);
    }

    protected TimeseriesMetadataOutput createExpanded(MeasurementDatasetEntity series, DbQuery query, Session session) throws DataAccessException {
        TimeseriesMetadataOutput output = createCondensed(series, query, session);
        output.setSeriesParameters(createTimeseriesOutput(series, query));
        MeasurementDataRepository repository = createRepository("measurement");

        output.setReferenceValues(createReferenceValueOutputs(series, query, repository));
        output.setFirstValue(repository.getFirstValue(series, session, query));
        output.setLastValue(repository.getLastValue(series, session, query));
        return output;
    }

    private MeasurementDataRepository createRepository(String datasetType) throws DataAccessException {
        if ( !"measurement".equalsIgnoreCase(datasetType)) {
            throw new ResourceNotFoundException("unknown dataset type: " + datasetType);
        }
        try {
            return (MeasurementDataRepository) factory.create("measurement");
        } catch (DatasetFactoryException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
    private MeasurementReferenceValueOutput[] createReferenceValueOutputs(MeasurementDatasetEntity series,
            DbQuery query, MeasurementDataRepository repository) throws DataAccessException {
        List<MeasurementReferenceValueOutput> outputs = new ArrayList<>();
        Set<MeasurementDatasetEntity> referenceValues = series.getReferenceValues();
        for (MeasurementDatasetEntity referenceSeriesEntity : referenceValues) {
            if (referenceSeriesEntity.isPublished()) {
                MeasurementReferenceValueOutput refenceValueOutput = new MeasurementReferenceValueOutput();
                ProcedureEntity procedure = referenceSeriesEntity.getProcedure();
                refenceValueOutput.setLabel(procedure.getNameI18n(query.getLocale()));
                refenceValueOutput.setReferenceValueId(referenceSeriesEntity.getPkid().toString());

                MeasurementDataEntity lastValue = referenceSeriesEntity.getLastValue();
                refenceValueOutput.setLastValue(repository.createSeriesValueFor(lastValue, referenceSeriesEntity, query));
                outputs.add(refenceValueOutput);
            }
        }
        return outputs.toArray(new MeasurementReferenceValueOutput[0]);
    }

    private TimeseriesMetadataOutput createCondensed(MeasurementDatasetEntity entity, DbQuery query, Session session) throws DataAccessException {
        TimeseriesMetadataOutput output = new TimeseriesMetadataOutput() ;
        String locale = query.getLocale();
        String phenomenonLabel = entity.getPhenomenon().getLabelFrom(locale);
        String procedureLabel = entity.getProcedure().getLabelFrom(locale);
        String stationLabel = entity.getFeature().getLabelFrom(locale);
        String offeringLabel = entity.getOffering().getLabelFrom(locale);
        output.setLabel(createTimeseriesLabel(phenomenonLabel, procedureLabel, stationLabel, offeringLabel));
        output.setId(entity.getPkid().toString());
        output.setUom(entity.getUnitI18nName(locale));
        output.setStation(createCondensedStation(entity, query, session));
        return output;
    }

    private String createTimeseriesLabel(String phenomenon, String procedure, String station, String offering) {
        StringBuilder sb = new StringBuilder();
        sb.append(phenomenon).append(" ");
        sb.append(procedure).append(", ");
        sb.append(station).append(", ");
        return sb.append(offering).toString();
    }

    private StationOutput createCondensedStation(MeasurementDatasetEntity entity, DbQuery query, Session session) throws DataAccessException {
        FeatureEntity feature = entity.getFeature();
        String featurePkid = feature.getPkid().toString();

        // XXX explicit cast here
        return ((StationRepository) stationRepository).getCondensedInstance(featurePkid, query, session);
    }

    public OutputAssembler<StationOutput> getStationRepository() {
        return stationRepository;
    }

    public void setStationRepository(OutputAssembler<StationOutput> stationRepository) {
        this.stationRepository = stationRepository;
    }

}
