package org.n52.series.ckan.cache;

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
