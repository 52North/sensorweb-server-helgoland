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
package org.n52.io.geojson;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.n52.io.crs.CRSUtils;
import org.n52.shetland.util.JTSHelper;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * borrwoed from
 * https://github.com/52North/SOS/blob/4.3.4/coding/json/src/main/java/org/n52/sos/encode/json/impl/GeoJSONEncoder.java
 *
 * @since 2.0
 */
public class GeoJSONEncoder {

    // XXX internally we are using CRS:84 which has different axes ordering
    public static final int DEFAULT_SRID = CRSUtils.EPSG_WGS84;

    public static final String SRID_LINK_PREFIX = "http://www.opengis.net/def/crs/EPSG/0/";

    private final JsonNodeFactory jsonFactory = JsonNodeFactory
            .withExactBigDecimals(false);

    public ObjectNode encodeGeometry(Geometry value) throws GeoJSONException {
        if (value == null) {
            return null;
        } else {
            return encodeGeometry(value, DEFAULT_SRID);
        }
    }

    public ObjectNode encodeGeometry(Geometry geometry, int parentSrid) throws
            GeoJSONException {
        if (geometry.isEmpty()) {
            return null;
        } else if (geometry instanceof Point) {
            return encode((Point) geometry, parentSrid);
        } else if (geometry instanceof LineString) {
            return encode((LineString) geometry, parentSrid);
        } else if (geometry instanceof Polygon) {
            return encode((Polygon) geometry, parentSrid);
        } else if (geometry instanceof MultiPoint) {
            return encode((MultiPoint) geometry, parentSrid);
        } else if (geometry instanceof MultiLineString) {
            return encode((MultiLineString) geometry, parentSrid);
        } else if (geometry instanceof MultiPolygon) {
            return encode((MultiPolygon) geometry, parentSrid);
        } else if (geometry instanceof GeometryCollection) {
            return encode((GeometryCollection) geometry, parentSrid);
        } else {
            throw new GeoJSONException("unknown geometry type " + geometry.getGeometryType());
        }
    }

    protected ObjectNode encode(Point geometry, int parentSrid) {
        ObjectNode json = jsonFactory.objectNode();
        json.put(JSONConstants.TYPE, JSONConstants.POINT);
        json.set(JSONConstants.COORDINATES, encodeCoordinates(geometry));
        encodeCRS(json, geometry, parentSrid);
        return json;
    }

    protected ObjectNode encode(LineString geometry, int parentSrid) {
        ObjectNode json = jsonFactory.objectNode();
        json.put(JSONConstants.TYPE, JSONConstants.LINE_STRING);
        json.set(JSONConstants.COORDINATES, encodeCoordinates(geometry));
        encodeCRS(json, geometry, parentSrid);
        return json;
    }

    protected ObjectNode encode(Polygon geometry, int parentSrid) {
        ObjectNode json = jsonFactory.objectNode();
        json.put(JSONConstants.TYPE, JSONConstants.POLYGON);
        json.set(JSONConstants.COORDINATES, encodeCoordinates(geometry));
        encodeCRS(json, geometry, parentSrid);
        return json;
    }

    protected ObjectNode encode(MultiPoint geometry, int parentSrid) {
        ObjectNode json = jsonFactory.objectNode();
        ArrayNode list = json.put(JSONConstants.TYPE, JSONConstants.MULTI_POINT)
                .putArray(JSONConstants.COORDINATES);
        for (int i = 0; i < geometry.getNumGeometries(); ++i) {
            list.add(encodeCoordinates((Point) geometry.getGeometryN(i)));
        }
        encodeCRS(json, geometry, parentSrid);
        return json;
    }

    protected ObjectNode encode(MultiLineString geometry, int parentSrid) {
        ObjectNode json = jsonFactory.objectNode();
        ArrayNode list = json.put(JSONConstants.TYPE, JSONConstants.MULTI_LINE_STRING)
                .putArray(JSONConstants.COORDINATES);
        for (int i = 0; i < geometry.getNumGeometries(); ++i) {
            list.add(encodeCoordinates((LineString) geometry.getGeometryN(i)));
        }
        encodeCRS(json, geometry, parentSrid);
        return json;
    }

    protected ObjectNode encode(MultiPolygon geometry, int parentSrid) {
        ObjectNode json = jsonFactory.objectNode();
        ArrayNode list = json.put(JSONConstants.TYPE, JSONConstants.MULTI_POLYGON)
                .putArray(JSONConstants.COORDINATES);
        for (int i = 0; i < geometry.getNumGeometries(); ++i) {
            list.add(encodeCoordinates((Polygon) geometry.getGeometryN(i)));
        }
        encodeCRS(json, geometry, parentSrid);
        return json;
    }

    public ObjectNode encode(GeometryCollection geometry, int parentSrid) throws
            GeoJSONException {
        ObjectNode json = jsonFactory.objectNode();
        ArrayNode geometries = json.put(JSONConstants.TYPE,
                JSONConstants.GEOMETRY_COLLECTION)
                .putArray(JSONConstants.GEOMETRIES);
        int srid = encodeCRS(json, geometry, parentSrid);
        for (int i = 0; i < geometry.getNumGeometries(); ++i) {
            geometries.add(encodeGeometry(geometry.getGeometryN(i), srid));
        }
        return json;
    }

    protected ArrayNode encodeCoordinate(Coordinate coordinate) {

        ArrayNode array = jsonFactory.arrayNode()
                .add(coordinate.x)
                .add(coordinate.y);

        if (!Double.isNaN(coordinate.getZ())) {
            array.add(coordinate.getZ());
        }

        return array;
    }

    protected ArrayNode encodeCoordinates(CoordinateSequence coordinates) {
        ArrayNode list = jsonFactory.arrayNode();
        for (int i = 0; i < coordinates.size(); ++i) {
            list.add(encodeCoordinate(coordinates.getCoordinate(i)));
        }
        return list;
    }

    protected ArrayNode encodeCoordinates(Point geometry) {
        return encodeCoordinate(geometry.getCoordinate());
    }

    protected ArrayNode encodeCoordinates(LineString geometry) {
        return encodeCoordinates(geometry.getCoordinateSequence());
    }

    protected ArrayNode encodeCoordinates(Polygon geometry) {
        ArrayNode list = jsonFactory.arrayNode();
        Coordinate[] coordinates = JTSHelper.getExteriorRingCoordinatesFromPolygon(geometry);
        list.add(encodeCoordinates(new CoordinateArraySequence(coordinates)));
//        list.add(encodeCoordinates(geometry.getExteriorRing()));
        for (int i = 0; i < geometry.getNumInteriorRing(); ++i) {
            list.add(encodeCoordinates(geometry.getInteriorRingN(i)));
        }
        return list;
    }

    protected int encodeCRS(ObjectNode json, Geometry geometry, int parentSrid) {
        return encodeCRS(geometry.getSRID(), parentSrid, json);
    }

    protected int encodeCRS(int srid, int parentSrid, ObjectNode json) {
        if (srid == parentSrid
                || srid == 0
                || parentSrid == DEFAULT_SRID
                && srid == DEFAULT_SRID) {
            return parentSrid;
        } else {
            json.putObject(JSONConstants.CRS)
                    .put(JSONConstants.TYPE, JSONConstants.LINK)
                    .putObject(JSONConstants.PROPERTIES)
                    .put(JSONConstants.HREF, SRID_LINK_PREFIX + srid);
            return srid;
        }
    }

}
