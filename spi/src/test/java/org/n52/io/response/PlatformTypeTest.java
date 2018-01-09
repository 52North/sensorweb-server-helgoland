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

public class PlatformTypeTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void when_mobileInsituString_then_recognizeType() {
        Assert.assertThat(PlatformType.toInstance("mobile_insitu"), Matchers.is(PlatformType.MOBILE_INSITU));
    }

    @Test
    public void when_mobileRemoteString_then_recognizeType() {
        Assert.assertThat(PlatformType.toInstance("mobile_remote"), Matchers.is(PlatformType.MOBILE_REMOTE));
    }

    @Test
    public void when_stationaryInsituString_then_recognizeType() {
        Assert.assertThat(PlatformType.toInstance("stationary_insitu"), Matchers.is(PlatformType.STATIONARY_INSITU));
    }

    @Test
    public void when_stationaryRemoteString_then_recognizeType() {
        Assert.assertThat(PlatformType.toInstance("stationary_remote"), Matchers.is(PlatformType.STATIONARY_REMOTE));
    }

    @Test
    public void when_unknownType_then_throwException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("no type for 'does not exist'.");
        PlatformType.toInstance("does not exist");
    }

    @Test
    public void when_idWithUnknownType_then_throwException() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("no type for 'does_not_exist_42'.");
        PlatformType.extractType("does_not_exist_42");
    }

    @Test
    public void when_extractingMobileInsitu_then_correctPlatformType() {
        Assert.assertThat(PlatformType.extractType("mobile_insitu_foobar"), Matchers.is(PlatformType.MOBILE_INSITU));
    }

    @Test
    public void when_extractingMobileRemote_then_correctPlatformType() {
        Assert.assertThat(PlatformType.extractType("mobile_remote_foobar"), Matchers.is(PlatformType.MOBILE_REMOTE));
    }

    @Test
    public void when_extractingStationaryInsitu_then_correctPlatformType() {
        Assert.assertThat(PlatformType.extractType("stationary_insitu_foobar"),
                          Matchers.is(PlatformType.STATIONARY_INSITU));
    }

    @Test
    public void when_extractingStationaryRemote_then_correctPlatformType() {
        Assert.assertThat(PlatformType.extractType("stationary_remote_foobar"),
                          Matchers.is(PlatformType.STATIONARY_REMOTE));
    }

    @Test
    public void when_extractingId_then_typePrefixGone() {
        Assert.assertThat(PlatformType.extractId("mobile_insitu_foobar"), Matchers.is("foobar"));
    }

    @Test
    public void when_extractingWithInvalidPrefix_then_expectIdentity() {
        Assert.assertThat(PlatformType.extractId("invalid_prefix"), Matchers.is("invalid_prefix"));
    }

    @Test
    public void when_stationaryOnlyPrefix_then_expectIdentity() {
        Assert.assertThat(PlatformType.extractId("stationary"), Matchers.is("stationary"));
    }

    @Test
    public void when_mobileOnlyPrefix_then_expectIdentity() {
        Assert.assertThat(PlatformType.extractId("mobile"), Matchers.is("mobile"));
    }

    @Test
    public void when_idWithStationaryPrefix_then_detectType() {
        Assert.assertTrue(PlatformType.isStationaryId("stationary_remote_something"));
    }

    @Test
    public void when_idWithMobilePrefix_then_detectType() {
        Assert.assertTrue(PlatformType.isMobileId("mobile_insitu_something"));
    }

    @Test
    public void when_idWithInsituSuffix_then_detectType() {
        Assert.assertTrue(PlatformType.isInsitu("mobile_insitu_10"));
    }

}
