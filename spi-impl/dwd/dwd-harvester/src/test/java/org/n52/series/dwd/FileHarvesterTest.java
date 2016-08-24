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
package org.n52.series.dwd;

import java.io.File;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.n52.series.dwd.store.AlertStore;
import org.n52.series.dwd.store.InMemoryAlertStore;

public class FileHarvesterTest {

    private AlertStore store;

    private DwdHarvester harvester;

    @Before
    public void setUp() {
        this.store = new InMemoryAlertStore();
        File file = new File("/empty-example.json");
        this.harvester = FileHarvester
                .aHarvester(store)
                .withFile(file)
                .build();
    }

    @Test
    public void when_notYetHarvested_then_emptyStore() {
        Assert.assertTrue(store.isEmpty());
    }

    @Test
    public void when_beforeHarvesting_then_lastHarvestDateIsNull() {
        Assert.assertNull(harvester.getLastHarvestedAt());
    }

    @Test
    public void when_afterHarvesting_then_lastHarvestDateIsNotNull() {
        this.harvester.harvest();
        Assert.assertNotNull(harvester.getLastHarvestedAt());
    }

    @Test
    public void when_afterHarvesting_then_lastHarvestDateIsAfterThanBeforeHarvesting() {
        this.harvester.harvest();
        DateTime beforeHavesting = harvester.getLastHarvestedAt();
        final DateTime afterHarvesting = harvester.getLastHarvestedAt();
        Assert.assertTrue(afterHarvesting.isAfter(beforeHavesting) || afterHarvesting.isEqual(beforeHavesting));
    }

}
