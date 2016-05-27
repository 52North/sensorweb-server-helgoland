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
package org.n52.series.db.da.beans.v1.ext;

import java.io.IOException;

import org.junit.Test;
import org.n52.io.geojson.GeoJSONException;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class PlatformsExampleGenerator extends ExampleGenerator {

    private static final String PLATFORMS_FILE = "platforms_output.json";

    private static final String MOBILE_INSITU_1_FILE = "mobile_insitu_output_1.json";

    private static final String MOBILE_INSITU_2_FILE = "mobile_insitu_output_2.json";

    private static final String STATIONARY_INSITU_FILE = "stationary_insitu_output.json";

    private static final String MOBILE_REMOTE_FILE = "mobile_remote_output.json";

    private static final String STATIONARY_REMOTE_FILE = "stationary_remote_output.json";

    @Test
    public void generatePlatformExamples() throws IOException, GeoJSONException {
        generatePlatformListCommonModel();
        generateStationInstance();
    }

    private void generatePlatformListCommonModel() throws IOException {
        final ObjectNode root = getJsonFactory().objectNode();
        root.set("platforms", encodeSimplePlatformList());
        writeToFile(PLATFORMS_FILE, root);
    }

    private ArrayNode encodeSimplePlatformList() {
        final ArrayNode platforms = getJsonFactory().arrayNode();
        platforms.add(getObjectMapper().valueToTree(PlatformExample.BEVER_TALSPERRE.getCondensed()));
        platforms.add(getObjectMapper().valueToTree(PlatformExample.RV_SONNE_MISSION_1.getCondensed()));
        platforms.add(getObjectMapper().valueToTree(PlatformExample.RV_SONNE_MISSION_2.getCondensed()));
//        platforms.add(getObjectMapper().valueToTree(PlatformExample.STATIC_WEBCAM.getCondensed()));
        // TODO mobile/remote
        return platforms;
    }

    private void generateStationInstance() throws IOException, GeoJSONException {
        writeToFile(STATIONARY_INSITU_FILE, PlatformExample.BEVER_TALSPERRE.getOutput());
        writeToFile(MOBILE_INSITU_1_FILE, PlatformExample.RV_SONNE_MISSION_1.getOutput());
        writeToFile(MOBILE_INSITU_2_FILE, PlatformExample.RV_SONNE_MISSION_2.getOutput());
//        writeToFile(STATIONARY_REMOTE_FILE, PlatformExample.STATIC_WEBCAM.getOutput());
    }

}
