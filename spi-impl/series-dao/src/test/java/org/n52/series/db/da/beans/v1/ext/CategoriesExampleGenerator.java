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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class CategoriesExampleGenerator extends ExampleGenerator {

    private static final String CAGTEGORIES_FILE = "categories_ext_output.json";

    @Test
    public void generatePlatformExamples() throws IOException, GeoJSONException {
        generateCategoryExtListModel();
        generateCategoryInstance();
    }

    private void generateCategoryExtListModel() throws IOException {
        final ObjectNode root = getJsonFactory().objectNode();
        root.set("categories", encodeSimpleCategoryExtList());
        writeToFile(CAGTEGORIES_FILE, root);
    }

    private JsonNode encodeSimpleCategoryExtList() {
        int index = 0;
        final ArrayNode categories = getJsonFactory().arrayNode();

        categories.add(encodeCategory("" + index++, "Discharge"));
        categories.add(encodeCategory("" + index++, "Air Temperature"));
        categories.add(encodeCategory("" + index++, "Precipitation"));

        return categories;
    }

    private JsonNode encodeCategory(String index, String label) {
        final JsonNodeFactory factory = getJsonFactory();
        final ObjectNode categoryNode = getJsonFactory().objectNode();
        categoryNode.set("id", factory.textNode(index));
        categoryNode.set("label", factory.textNode(label));
        categoryNode.set("href", getFullHref(String.format("categories/%s", index)));
        categoryNode.set("service", createServiceNode());
        return categoryNode;
    }

    private void generateCategoryInstance() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
