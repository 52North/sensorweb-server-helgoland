/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.io.response;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.n52.io.response.GeometryType;

public class GeometryTypeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void when_extractingId_then_typePrefixGone() {
        Assert.assertThat(GeometryType.extractId("observed_static_foobar"), Matchers.is("foobar"));
    }

    @Test
    public void when_extractingWithInvalidPrefix_then_expectIdentity() {
        Assert.assertThat(GeometryType.extractId("invalid_prefix"), Matchers.is("invalid_prefix"));
    }

    @Test
    public void when_observedGeometryOnlyPrefix_then_expectIdentity() {
        Assert.assertThat(GeometryType.extractId("observed_static"), Matchers.is("observed_static"));
    }

    @Test
    public void when_observedOnlyPrefix_then_expectIdentity() {
        Assert.assertThat(GeometryType.extractId("observed"), Matchers.is("observed"));
    }

    @Test
    public void when_idWithObservedPrefix_then_detectType() {
        Assert.assertTrue(GeometryType.isObservedGeometryId("observed_static"));
    }

    @Test
    public void when_idWithPlatformPrefix_then_detectType() {
        Assert.assertTrue(GeometryType.isPlatformGeometryId("platform_site"));
    }

    @Test
    public void when_idWithSiteSuffix_then_detectType() {
        Assert.assertTrue(GeometryType.isSiteId("platform_site_10"));
    }

    @Test
    public void when_nullId_then_handledWhenDetectPlatform() {
        Assert.assertFalse(GeometryType.isPlatformGeometryId(null));
    }

    @Test
    public void when_nullId_then_handledWhenDetectSite() {
        Assert.assertFalse(GeometryType.isSiteId(null));
    }

}
