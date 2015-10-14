package org.n52.series.api.b1.db.da.beans;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.n52.series.api.v1.db.da.beans.ObservationEntity;
import org.n52.series.api.v1.db.da.beans.ServiceInfo;

public class ServiceInfoTest {
    
    private ServiceInfo serviceInfo;
    
    @Before
    public void setUp() {
        serviceInfo = new ServiceInfo();
    }
    
    @Test
    public void shouldNotFailWhenSettingInvalidNoDataValues() {
        serviceInfo.setNoDataValues("4.3,9,invalid");
        MatcherAssert.assertThat(serviceInfo.getNoDataValues().split(",").length, Is.is(2));
    }
    
    @Test
    public void shouldTreatNullAsNoDataValue() {
        ObservationEntity entity = new ObservationEntity();
        entity.setValue(null);
        MatcherAssert.assertThat(serviceInfo.hasNoDataValue(entity), Is.is(true));
    }
    
    @Test
    public void shouldTreatNaNAsNoDataValue() {
        ObservationEntity entity = new ObservationEntity();
        entity.setValue(Double.NaN);
        MatcherAssert.assertThat(serviceInfo.hasNoDataValue(entity), Is.is(true));
    }
    
    @Test
    public void shouldHandleDoubleValues() {
        serviceInfo.setNoDataValues("4.3,9,invalid");
        ObservationEntity entity = new ObservationEntity();
        entity.setValue(new Double(9));
        MatcherAssert.assertThat(serviceInfo.hasNoDataValue(entity), Is.is(true));
        
        entity.setValue(4.30);
        MatcherAssert.assertThat(serviceInfo.hasNoDataValue(entity), Is.is(true));
    }
}
