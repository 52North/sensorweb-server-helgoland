package org.n52.series.ckan.cache;
/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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

import eu.trentorise.opendata.jackan.model.CkanDataset;
import org.n52.series.ckan.beans.CsvObservationsCollection;
import org.n52.series.ckan.io.SosInsertionStrategy;
import org.n52.series.ckan.io.SosModelMapper;
import org.n52.sos.ds.hibernate.InsertObservationDAO;
import org.n52.sos.ds.hibernate.InsertSensorDAO;

public class SosDatabaseCache implements CkanDataSink {
    
    private InsertSensorDAO insertSensorDao;
    
    private InsertObservationDAO insertObservationDao;
    
    // TODO ckanSosSyncDao

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void insertOrUpdate(CkanDataset dataset, CsvObservationsCollection csvObservationsCollection) {
        SosModelMapper modelMapper = SosModelMapper.create()
                .withData(csvObservationsCollection)
                .setInsertSensorDao(insertSensorDao)
                .setInsertObservationDao(insertObservationDao);
        SosInsertionStrategy insertionStrategy = modelMapper.createInsertionStrategy();
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    @Override
//    public Iterable<InMemoryCkanDataCache.Entry<CkanDataset, CsvObservationsCollection>> getCollections() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }

    public InsertSensorDAO getInsertSensorDao() {
        return insertSensorDao;
    }

    public void setInsertSensorDao(InsertSensorDAO insertSensorDao) {
        this.insertSensorDao = insertSensorDao;
    }

    public InsertObservationDAO getInsertObservationDao() {
        return insertObservationDao;
    }

    public void setInsertObservationDao(InsertObservationDAO insertObservationDao) {
        this.insertObservationDao = insertObservationDao;
    }
    
    
}
