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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PlatformOutputTest {

    @Test
    public void when_createdMobileRemote_then_hrefIncludesPrefix() {
        PlatformOutput platform = new PlatformOutput();
        platform.setPlatformType(OptionalOutput.of(PlatformType.MOBILE_REMOTE));
        platform.setHrefBase(OptionalOutput.of("http://localhost/context"));
        platform.setId("12");

        assertThat(platform.getHref(), is("http://localhost/context/mobile_remote_12"));
    }

    @Test
    public void when_createdStationaryRemote_then_hrefIncludesPrefix() {
        PlatformOutput platform = new PlatformOutput();
        platform.setPlatformType(OptionalOutput.of(PlatformType.STATIONARY_REMOTE));
        platform.setHrefBase(OptionalOutput.of("http://localhost/context"));
        platform.setId("12");

        assertThat(platform.getHref(), is("http://localhost/context/stationary_remote_12"));
    }

    @Test
    public void when_createdStationaryInsitu_then_hrefIncludesPrefix() {
        PlatformOutput platform = new PlatformOutput();
        platform.setPlatformType(OptionalOutput.of(PlatformType.STATIONARY_INSITU));
        platform.setHrefBase(OptionalOutput.of("http://localhost/context"));
        platform.setId("12");

        assertThat(platform.getHref(), is("http://localhost/context/stationary_insitu_12"));
    }

    @Test
    public void when_createdMobileInsitu_then_hrefIncludesPrefix() {
        PlatformOutput platform = new PlatformOutput();
        platform.setPlatformType(OptionalOutput.of(PlatformType.MOBILE_INSITU));
        platform.setHrefBase(OptionalOutput.of("http://localhost/context"));
        platform.setId("12");

        assertThat(platform.getHref(), is("http://localhost/context/mobile_insitu_12"));
    }

    @Test
    public void when_havingExplicitHref_then_hrefNotIncludingHrefBase() {
        PlatformOutput platform = new PlatformOutput();
        platform.setPlatformType(OptionalOutput.of(PlatformType.MOBILE_INSITU));
        platform.setHref(OptionalOutput.of("http://localhost/otherContext/12"));
        platform.setHrefBase(OptionalOutput.of("http://localhost/context"));
        platform.setId("12");

        assertThat(platform.getHref(), is("http://localhost/otherContext/12"));
    }
}
