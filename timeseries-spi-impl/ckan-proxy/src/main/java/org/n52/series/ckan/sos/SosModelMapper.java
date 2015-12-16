package org.n52.series.ckan.io;
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

import java.util.Map;
import org.n52.series.ckan.beans.CsvObservationsCollection;
import org.n52.series.ckan.beans.DataFile;
import org.n52.series.ckan.beans.ResourceMember;
import org.n52.series.ckan.da.CkanConstants;
import org.n52.sos.ds.hibernate.InsertObservationDAO;
import org.n52.sos.ds.hibernate.InsertSensorDAO;

public class SosModelMapper {
    
    private CsvObservationsCollection dataCollection;
    
    private InsertSensorDAO insertSensorDao;
    
    private InsertObservationDAO insertObservationDao;
    
    private SosModelMapper() {
        
    }
    
    public static SosModelMapper create() {
        return new SosModelMapper();
    }
    
    
    public SosInsertionStrategy createInsertionStrategy() {
        Map<ResourceMember, DataFile> platformData = getDataOfType(CkanConstants.RESOURCE_TYPE_PLATFORMS);
        Map<ResourceMember, DataFile> observationData = getDataOfType(CkanConstants.RESOURCE_TYPE_OBSERVATIONS);
        
        // TODO
        
        return new DefaultSosInsertionStrategy(dataCollection);
    }

    private Map<ResourceMember, DataFile> getDataOfType(String type) {
        return dataCollection.getDataCollectionsOfType(type);
    }
    
    public SosModelMapper withData(CsvObservationsCollection dataCollection) {
        this.dataCollection = dataCollection;
        return this;
    }

    public SosModelMapper setInsertSensorDao(InsertSensorDAO insertSensorDao) {
        this.insertSensorDao = insertSensorDao;
        return this;
    }

    public SosModelMapper setInsertObservationDao(InsertObservationDAO insertObservationDao) {
        this.insertObservationDao = insertObservationDao;
        return this;
    }
}
