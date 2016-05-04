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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.n52.io.crs.CRSUtils;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class ExampleGenerator {

    private final JsonNodeFactory jsonFactory = JsonNodeFactory.withExactBigDecimals(false);

    protected final CRSUtils crsUtils = CRSUtils.createEpsgForcedXYAxisOrder();

    private final ObjectMapper om = new ObjectMapper();

    @Before
    public void setUp() {
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        om.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static JsonNode getFullHref(String context) {
        String href = !context.startsWith("/")
                ? ExampleConstants.BASE_URL + "/" + context
                : ExampleConstants.BASE_URL + context;
        return new TextNode(href);
    }

    protected JsonNode createServiceNode() {
        final JsonNodeFactory factory = getJsonFactory();
        final ObjectNode serviceNode = factory.objectNode();
        serviceNode.set("id", factory.textNode("1"));
        serviceNode.set("label", factory.textNode("Mocked Series REST API"));
        serviceNode.set("href", getFullHref("service/1"));
        return serviceNode;
    }

    protected void writeToFile(String file, Object object) throws JsonProcessingException, IOException {
        Path outputPath = Paths.get(file);
        Files.write(outputPath, getObjectMapper().writeValueAsBytes(object));
    }

    protected JsonNode readFromFile(String file) throws IOException {
        return getObjectMapper().readTree(Paths.get(file).toFile());
    }

    protected JsonNodeFactory getJsonFactory() {
        return jsonFactory;
    }

    protected ObjectMapper getObjectMapper() {
        return om;
    }

}
