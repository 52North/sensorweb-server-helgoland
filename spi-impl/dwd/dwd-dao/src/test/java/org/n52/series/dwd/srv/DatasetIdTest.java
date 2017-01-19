/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.series.dwd.srv;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.n52.io.response.dataset.DatasetType;
import org.n52.io.response.dataset.dwd.DwdAlertDatasetOutput;

public class DatasetIdTest {

    private DatasetOutputAdapter adapter = new DatasetOutputAdapter(null, null);

    private String prefix = "dwd-alert_";

    private String warnCell = "123456";

    private String phenomenon = "temperature";

    @Test
    public void testCreateId() {
        DwdAlertDatasetOutput output = createDwdAlertDatasetOutput();
        Assert.assertThat(output.getId().equals(createId()), Matchers.is(true));
    }

    @Test
    public void testParseId() {
        DwdAlertDatasetOutput output = createDwdAlertDatasetOutput();
        List<String> parseId = adapter.parseId(DatasetType.extractId(output.getId()));
        Assert.assertThat(parseId.size(), Matchers.is(2));
        Assert.assertThat(parseId.get(0).equals(warnCell), Matchers.is(true));
        Assert.assertThat(parseId.get(1).equals(phenomenon), Matchers.is(true));
    }

    private String createId() {
        StringBuilder builder = new StringBuilder();
        builder.append(DwdAlertDatasetOutput.DATASET_TYPE).append("_").append(warnCell).append("-").append(phenomenon);
        return builder.toString();
    }

    private DwdAlertDatasetOutput createDwdAlertDatasetOutput() {
        DwdAlertDatasetOutput output = new DwdAlertDatasetOutput();
        output.setId(adapter.createId(warnCell, phenomenon));
        return output;
    }
}
