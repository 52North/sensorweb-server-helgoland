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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;
import org.n52.io.request.IoParameters;
import org.n52.io.response.v1.SeriesMetadataV1Output;
import org.n52.io.response.v1.ext.MeasurementSeriesOutput;
import org.n52.io.response.v1.ext.SeriesMetadataOutput;
import org.n52.sensorweb.spi.SearchResult;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.ext.AbstractObservationEntity;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;
import org.n52.series.db.da.beans.ext.MeasurementSeriesEntity;
import org.n52.series.db.da.dao.v1.ext.SeriesDao;

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
            SeriesDao seriesDao = new SeriesDao(session);
            for (AbstractSeriesEntity<AbstractObservationEntity> series : seriesDao.getAllInstances(query)) {
                /*
                 *  ATM, the SWC REST API only supports numeric types
                 *  We check for a unit to check for them
                 */
//                if (timeseries.hasUnit()) {
                    results.add(createCondensed(series, query));
//                }
            }
            return results;

        } finally {
            returnSession(session);
        }
    }

    @Override
    public List<SeriesMetadataOutput> getAllExpanded(DbQuery parameters) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SeriesMetadataOutput getInstance(String id, DbQuery parameters) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
            SeriesMetadataOutput output = new MeasurementSeriesOutput();
            MeasurementSeriesEntity measurementSeries = (MeasurementSeriesEntity) series;
            String locale = query.getLocale();
            String stationLabel = getLabelFrom(series.getFeature(), locale);
            String procedureLabel = getLabelFrom(series.getProcedure(), locale);
            String phenomenonLabel = getLabelFrom(series.getPhenomenon(), locale);
//            output.setLabel(createTimeseriesLabel(phenomenonLabel, procedureLabel, stationLabel));
            output.setId(series.getPkid().toString());
//            output.setUom(measurementSeries.getUnitI18nName(locale));
//            output.setStation(createCondensedStation(series, query));
            return output;
        }
        return null;
    }

}
