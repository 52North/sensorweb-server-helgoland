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
package org.n52.series.db.da.dao.v1;

import org.n52.series.db.dao.DbQuery;
import org.junit.Assert;
import org.junit.Test;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;

public class DbQueryTest {

    @Test
    public void when_createWithNull_then_defaults() {
        Assert.assertNotNull(DbQuery.createFrom(null));
    }

    @Test
    public void when_defaults_then_insituFilterActive() {
        DbQuery query = DbQuery.createFrom(null);
        Assert.assertTrue(query.shallIncludeInsituPlatformTypes());
    }

    @Test
    public void when_defaults_then_stationaryFilterActive() {
        DbQuery query = DbQuery.createFrom(null);
        Assert.assertTrue(query.shallIncludeStationaryPlatformTypes());
    }

    @Test
    public void when_defaults_then_remoteFilterInactive() {
        DbQuery query = DbQuery.createFrom(null);
        Assert.assertFalse(query.shallIncludeRemotePlatformTypes());
    }

    @Test
    public void when_defaults_then_mobileFilterInactive() {
        DbQuery query = DbQuery.createFrom(null);
        Assert.assertFalse(query.shallIncludeMobilePlatformTypes());
    }

    @Test
    public void when_setMobile_then_mobileFilterActive() {
        final IoParameters defaults = IoParameters.createDefaults();
        DbQuery query = DbQuery.createFrom(defaults
                .extendWith(Parameters.PLATFORM_TYPES, "mobile")
        );
        Assert.assertTrue(query.shallIncludeMobilePlatformTypes());
    }

    @Test
    public void when_setRemote_then_remoteFilterActive() {
        final IoParameters defaults = IoParameters.createDefaults();
        DbQuery query = DbQuery.createFrom(defaults
                .extendWith(Parameters.PLATFORM_TYPES, "remote")
        );
        Assert.assertTrue(query.shallIncludeRemotePlatformTypes());
    }

    @Test
    public void when_setAll_then_insituFilterActive() {
        final IoParameters defaults = IoParameters.createDefaults();
        DbQuery query = DbQuery.createFrom(defaults
                .extendWith(Parameters.PLATFORM_TYPES, "all")
        );
        Assert.assertTrue(query.shallIncludeInsituPlatformTypes());
    }

    @Test
    public void when_setAll_then_stationaryFilterActive() {
        final IoParameters defaults = IoParameters.createDefaults();
        DbQuery query = DbQuery.createFrom(defaults
                .extendWith(Parameters.PLATFORM_TYPES, "all")
        );
        Assert.assertTrue(query.shallIncludeStationaryPlatformTypes());
    }

    @Test
    public void when_setAll_then_remoteFilterActive() {
        final IoParameters defaults = IoParameters.createDefaults();
        DbQuery query = DbQuery.createFrom(defaults
                .extendWith(Parameters.PLATFORM_TYPES, "all")
        );
        Assert.assertTrue(query.shallIncludeRemotePlatformTypes());
    }


    @Test
    public void when_setAll_then_mobileFilterActive() {
        final IoParameters defaults = IoParameters.createDefaults();
        DbQuery query = DbQuery.createFrom(defaults
                .extendWith(Parameters.PLATFORM_TYPES, "all")
        );
        Assert.assertTrue(query.shallIncludeMobilePlatformTypes());
    }

}
