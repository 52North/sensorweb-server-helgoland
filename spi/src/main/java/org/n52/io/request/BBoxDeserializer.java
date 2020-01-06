/*
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.io.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import java.io.IOException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.n52.io.geojson.GeoJSONDecoder;
import org.n52.io.geojson.GeoJSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BBoxDeserializer extends JsonDeserializer<BBox> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BBoxDeserializer.class);

    @Override
    public BBox deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        Point ll = (Point) decodeGeometry(getLowerLeft(node));
        Point ur = (Point) decodeGeometry(getUpperRight(node));
        return new BBox(ll, ur);
    }

    private JsonNode getLowerLeft(JsonNode node) {
        return getObjectNode("ll", node);
    }

    private JsonNode getUpperRight(JsonNode node) {
        return getObjectNode("ur", node);
    }

    private JsonNode getObjectNode(String path, JsonNode node) {
        return !node.path(path).isObject()
                ? MissingNode.getInstance()
                : node.path(path);
    }

    private Geometry decodeGeometry(JsonNode value) {
        try {
            final GeoJSONDecoder dec = new GeoJSONDecoder();
            return dec.decodeGeometry(value);
        } catch (GeoJSONException e) {
            LOGGER.error("could not properly decode geometry.", e);
            return null;
        }
    }


}
