/**
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
