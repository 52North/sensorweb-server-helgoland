/*
 * Copyright (C) 2013-2022 52°North Spatial Information Research GmbH
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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.locationtech.jts.geom.Point;
import org.n52.io.geojson.GeoJSONEncoder;
import org.n52.io.geojson.GeoJSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BBoxSerializer extends JsonSerializer<BBox> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BBoxSerializer.class);

    @Override
    public void serialize(BBox value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("ll", encodeGeometry(value.getLl()));
        gen.writeObjectField("ur", encodeGeometry(value.getUr()));
        gen.writeEndObject();
    }

    private Object encodeGeometry(Point value) {
        try {
            final GeoJSONEncoder enc = new GeoJSONEncoder();
            return enc.encodeGeometry(value);
        } catch (GeoJSONException e) {
            LOGGER.error("could not properly encode geometry.", e);
            return null;
        }
    }
}
