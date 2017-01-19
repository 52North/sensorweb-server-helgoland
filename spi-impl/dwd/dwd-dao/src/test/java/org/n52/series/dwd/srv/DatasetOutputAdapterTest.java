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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.Test;
import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.DatasetType;
import org.n52.io.response.dataset.dwd.DwdAlertDatasetOutput;
import org.n52.series.dwd.beans.WarnCell;
import org.n52.series.dwd.rest.Alert;
import org.n52.series.dwd.rest.VorabInformationAlert;

public class DatasetOutputAdapterTest {

    @Test
    public void when_warnCellAndAlert_then_createOutput() {
        WarnCell warnCell = new WarnCell("109771000");
        Alert alert = new VorabInformationAlert();
        alert.setEvent("VORABINFORMATION HEFTIGER / ERGIEBIGER REGEN");

        DatasetOutputAdapter adapter = new DatasetOutputAdapter(null, null);
        DwdAlertDatasetOutput output = adapter.createCondensed(alert, warnCell, IoParameters.createDefaults());
        assertThat(DatasetType.extractId(output.getId()), is("109771000-VORABINFORMATION HEFTIGER - ERGIEBIGER REGEN"));
    }

    @Test
    public void when_incomingVorabinformationIdWithSpacesAndSlash_then_parseId() {
        DatasetOutputAdapter adapter = new DatasetOutputAdapter(null, null);
        List<String> parts = adapter.parseId("109771000-VORABINFORMATION HEFTIGER - ERGIEBIGER REGEN");
        assertThat(parts.get(0), is("109771000"));
        assertThat(parts.get(1), is("VORABINFORMATION HEFTIGER / ERGIEBIGER REGEN"));
    }

}
