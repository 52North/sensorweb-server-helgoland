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
package org.n52.io.extension;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.n52.io.extension.metadata.MetadataJsonEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MetadataJsonEntitiyTest {

    @Test
    public void givenMetadataJsonEntity_whenSerialize_ValueAsJsonNode() throws JsonProcessingException, IOException {
        MetadataJsonEntity entity = new MetadataJsonEntity();
        entity.setPkid(1L);
        entity.setName("some_metadata");
        entity.setSeriesId(1L);
        entity.setType("json");
        entity.setValue("{\"key\":\"value\",\"object\":{\"key1\":\"string\",\"key2\":42}}");

        ObjectMapper om = new ObjectMapper();
        String jsonString = om.writeValueAsString(entity);
        JsonNode jsonNode = om.readTree(jsonString);
        JsonNode at = jsonNode.path("value").path("object");
        Assert.assertTrue(at.isObject());
    }

}
