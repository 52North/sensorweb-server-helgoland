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
package org.n52.io.response.dataset;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.n52.io.response.dataset.DatasetType;

public class DatasetTypeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void when_extractingIdWithoutUnderscore_then_extractIdentity() {
        assertThat(DatasetType.extractId("1"), is("1"));
    }

    @Test
    public void when_nullDatasetId_then_extractDefaultFallback() {
        Assert.assertThat(DatasetType.extractType(null), Matchers.is("measurement"));
    }

    @Test
    public void when_emptyDatasetIdAndEmptyFallback_then_extractDefaultFallback() {
        Assert.assertThat(DatasetType.extractType("", ""), Matchers.is("measurement"));
    }

    @Test
    public void when_datasetIdWithoutTypePrefix_then_extractDefaultFallback() {
        Assert.assertThat(DatasetType.extractType("text234"), Matchers.is("measurement"));
    }

    @Test
    public void when_datasetIdAndFallback_then_extractDatasetType() {
        Assert.assertThat(DatasetType.extractType("text_234", "count"), Matchers.is("text"));
    }

    @Test
    public void when_datasetIdWithoutTypePrefixAndFallback_then_extractFallback() {
        Assert.assertThat(DatasetType.extractType("http://foobar/234", "count"), Matchers.is("count"));
    }

    @Test
    public void when_datasetId_then_extractDatasetType() {
        Assert.assertThat(DatasetType.extractType("text_234"), Matchers.is("text"));
    }

    @Test
    public void when_datasetId_then_extractId() {
        Assert.assertThat(DatasetType.extractId("text_234"), Matchers.is("234"));
    }

    @Test
    public void when_createIdDatasetId_then_typeAndIdGetsConcatenated() {
        Assert.assertThat(DatasetType.createId("mytype", "123"), Matchers.is("mytype_123"));
    }

    @Test
    public void when_createIdWithCamelCasedType_then_typeGetsLowercased() {
        Assert.assertThat(DatasetType.createId("myType", "123"), Matchers.is("mytype_123"));
    }

    @Test
    public void when_createIdWithCamelCasedId_then_idKeepsCamelCased() {
        Assert.assertThat(DatasetType.createId("mytype", "camelCasedId"), Matchers.is("mytype_camelCasedId"));
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
