/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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

import java.io.IOException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.n52.io.geojson.GeoJSONDecoder;
import org.n52.io.geojson.GeoJSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;

public class VicinityDeserializer extends JsonDeserializer<Vicinity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(VicinityDeserializer.class);

    private static final String RADIUS = "radius";

    private static final String CENTER = "center";

    @Override
    public Vicinity deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        Point center = (Point) decodeGeometry(getCenter(node));
        double radius = getRadius(node).asDouble(0);
        return new Vicinity(center, radius);
    }

    private JsonNode getCenter(JsonNode node) {
        return !node.path(CENTER).isObject()
                ? MissingNode.getInstance()
                : node.path(CENTER);
    }

    private JsonNode getRadius(JsonNode node) {
        return !node.path(RADIUS).isDouble()
                ? MissingNode.getInstance()
                : node.path(RADIUS);
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
