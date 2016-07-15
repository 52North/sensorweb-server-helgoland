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
package org.n52.io.response.v1.ext;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DatasetTypeTest {
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void when_datasetId_then_extractDatasetType() {
        Assert.assertThat(DatasetType.extractType("text_234"), Matchers.is("text"));
    }

    @Test
    public void when_datasetId_then_extractId() {
        Assert.assertThat(DatasetType.extractId("text_234"), Matchers.is("234"));
    }
    
    @Test
    public void when_createDatasetId_then_typeAndIdGetsConcatenated() {
        Assert.assertThat(DatasetType.createId("myType", "123"), Matchers.is("myType_123"));
    }
    
    @Test
    public void when_createWithNullType_then_returnIdentity() {
        Assert.assertThat(DatasetType.createId(null, "123"), Matchers.is("123"));
    }
    
    @Test
    public void when_createWithEmptyType_then_returnIdentity() {
        Assert.assertThat(DatasetType.createId("", "123"), Matchers.is("123"));
    }
    
    @Test
    public void when_createWithEmptyId_then_throwException() {
        thrown.expect(IllegalArgumentException.class);
        DatasetType.createId("myType", "");
    }
    
    @Test
    public void when_createWithNullId_then_throwException() {
        thrown.expect(NullPointerException.class);
        DatasetType.createId("myType", null);
    }
    
}
