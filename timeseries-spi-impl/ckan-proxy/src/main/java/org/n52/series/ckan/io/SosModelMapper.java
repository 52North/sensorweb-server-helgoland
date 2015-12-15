package org.n52.series.ckan.io;

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
