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
import static org.hamcrest.Matchers.greaterThan;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.n52.io.request.IoParameters;
import org.n52.io.response.GeometryInfo;
import org.n52.io.response.OutputCollection;
import org.n52.series.dwd.beans.ServiceInfo;
import org.n52.series.dwd.rest.AlertCollection;
import org.n52.series.dwd.rest.VorabInformationAlert;
import org.n52.series.dwd.rest.WarnungAlert;
import org.n52.series.dwd.store.AlertStore;
import org.n52.series.dwd.store.InMemoryAlertStore;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GeometryOutputAdapterTest {

    private AlertStore alertStore;

    private ServiceInfo serviceInfo;

    private GeometryOutputAdapter adapter;

    @Before
    public void setUp() throws URISyntaxException {
        alertStore = new InMemoryAlertStore();
        serviceInfo = new ServiceInfo();
        serviceInfo.setServiceId("1");
        serviceInfo.setType("foo");

        adapter = new GeometryOutputAdapter(alertStore, serviceInfo);
        adapter.setPlatformOutputAdapter(new PlatformOutputAdapter(alertStore, serviceInfo));
    }

    @Test
    public void when_defaultQuery_then_returnNonEmptyList() throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry geometry = reader.read("POINT (7.45 52 2)");
        this.alertStore.setWarnCellGeometries(Collections.singletonMap("foobar", geometry));

        AlertCollection alerts = new AlertCollection();
        alerts.addWarnungAlert("foobar", new WarnungAlert());
        alertStore.updateCurrentAlerts(alerts);

        IoParameters query = IoParameters.createDefaults();
        OutputCollection<GeometryInfo> list = this.adapter.getCondensedParameters(query);
        assertThat(list.size(), is(1));
    }

    @Test
    public void when_bboxFilter_then_condensedGeometriesWithinFilter() throws ParseException {

        String bbox = "{" +
                "    \"ll\": {" +
                "        \"type\": \"Point\"," +
                "        \"coordinates\": [7, 51]" +
                "    }," +
                "    \"ur\": {" +
                "        \"type\": \"Point\"," +
                "        \"coordinates\": [9, 54]" +
                "    }" +
                "}";

        WKTReader reader = new WKTReader();
        Map<String, Geometry> geometries = new HashMap<>();
        geometries.put("foo_within1", reader.read("POINT (7.45 52)"));
        geometries.put("foo_within2", reader.read("POINT (8 53)"));
        geometries.put("foo_outside1", reader.read("POINT (9.1 53)"));
        geometries.put("foo_outside2", reader.read("POINT (8 50)"));
        this.alertStore.setWarnCellGeometries(geometries);

        AlertCollection alerts = new AlertCollection();
        alerts.addWarnungAlert("foo_within1", new WarnungAlert());
        alerts.addWarnungAlert("foo_outside1", new WarnungAlert());
        alerts.addVorabInformationAlert("foo_within2", new VorabInformationAlert());
        alerts.addVorabInformationAlert("foo_outside2", new VorabInformationAlert());
        alertStore.updateCurrentAlerts(alerts);

        IoParameters query = IoParameters.createDefaults().extendWith("bbox", bbox);
        OutputCollection<GeometryInfo> list = adapter.getCondensedParameters(query);
        assertThat(list.size(), is(2));
    }


}
