package org.n52.series.ckan.io;

import java.util.Map;
import org.n52.series.ckan.beans.CsvObservationsCollection;
import org.n52.series.ckan.beans.DataFile;
import org.n52.series.ckan.beans.ResourceMember;
import org.n52.series.ckan.da.CkanConstants;

public class SosModelMapper {
    
    private final CsvObservationsCollection dataCollection;
    
    public SosModelMapper(CsvObservationsCollection dataCollection) {
        this.dataCollection = dataCollection;
    }
    
    
    public SosInsertionStrategy createInsertionStrategy(CsvObservationsCollection dataCollection) {
        Map<ResourceMember, DataFile> platformData = getDataOfType(CkanConstants.RESOURCE_TYPE_PLATFORMS);
        Map<ResourceMember, DataFile> observationData = getDataOfType(CkanConstants.RESOURCE_TYPE_OBSERVATIONS);
        
        // TODO
        
        return new DefaultSosInsertionStrategy(dataCollection);
    }

    private Map<ResourceMember, DataFile> getDataOfType(String type) {
        return dataCollection.getDataCollectionOfType(type);
    }
}
