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

import org.n52.io.crs.CRSUtils;

import com.fasterxml.jackson.databind.JsonNode;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 *
 * borrowed from
 * https://github.com/52North/SOS/blob/4.3.4/coding/json/src/main/java/org/n52/sos/decode/json/impl/GeoJSONDecoder.java
 *
 * @since 2.0
 */
public class GeoJSONDecoder {

    public static final int DIM_2D = 2;

    public static final int DIM_3D = 3;

    private static final String[] SRS_LINK_PREFIXES = {
        "http://www.opengis.net/def/crs/EPSG/0/",
        "http://spatialreference.org/ref/epsg/"};

    private static final String[] SRS_NAME_PREFIXES = {
        "urn:ogc:def:crs:EPSG::",
        "EPSG::",
        "EPSG:"};

    private static final int DEFAULT_SRID = CRSUtils.EPSG_WGS84;

    private static final PrecisionModel DEFAULT_PRECISION_MODEL = new PrecisionModel(
            PrecisionModel.FLOATING);

    private static final GeometryFactory DEFAULT_GEOMETRY_FACTORY = new GeometryFactory(
            DEFAULT_PRECISION_MODEL,
            DEFAULT_SRID);

    public Geometry decodeGeometry(JsonNode node) throws GeoJSONException {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        } else {
            return decodeGeometry(node, DEFAULT_GEOMETRY_FACTORY);
        }
    }

    protected Geometry decodeGeometry(Object o, GeometryFactory parentFactory)
            throws GeoJSONException {
        if (!(o instanceof JsonNode)) {
            throw new GeoJSONException("Cannot decode " + o);
        }
        final JsonNode node = (JsonNode) o;
        final String type = getType(node);
        final GeometryFactory factory = getGeometryFactory(node, parentFactory);
        if (type.equals(JSONConstants.POINT)) {
            return decodePoint(node, factory);
        } else if (type.equals(JSONConstants.MULTI_POINT)) {
            return decodeMultiPoint(node, factory);
        } else if (type.equals(JSONConstants.LINE_STRING)) {
            return decodeLineString(node, factory);
        } else if (type.equals(JSONConstants.MULTI_LINE_STRING)) {
            return decodeMultiLineString(node, factory);
        } else if (type.equals(JSONConstants.POLYGON)) {
            return decodePolygon(node, factory);
        } else if (type.equals(JSONConstants.MULTI_POLYGON)) {
            return decodeMultiPolygon(node, factory);
        } else if (type.equals(JSONConstants.GEOMETRY_COLLECTION)) {
            return decodeGeometryCollection(node, factory);
        } else {
            throw new GeoJSONException("Unkown geometry type: " + type);
        }
    }

    protected CoordinateArraySequence decodeCoordinates(JsonNode node) throws
            GeoJSONException {
        if (!node.isArray()) {
            throwExpectedArrayException();
        }
        Coordinate[] coordinates = new Coordinate[node.size()];
        for (int i = 0; i < node.size(); ++i) {
            coordinates[i] = decodeCoordinate(node.get(i));
        }
        return new CoordinateArraySequence(coordinates);
    }

    protected Polygon decodePolygonCoordinates(JsonNode coordinates,
            GeometryFactory fac) throws GeoJSONException {
        if (!coordinates.isArray()) {
            throwExpectedArrayException();
        }
        if (coordinates.size() < 1) {
            throw new GeoJSONException("missing polygon shell");
        }
        LinearRing shell = fac.createLinearRing(decodeCoordinates(coordinates.get(0)));
        LinearRing[] holes = new LinearRing[coordinates.size() - 1];
        for (int i = 1; i < coordinates.size(); ++i) {
            holes[i - 1] = fac.createLinearRing(decodeCoordinates(coordinates.get(i)));
        }
        return fac.createPolygon(shell, holes);
    }

    protected MultiLineString decodeMultiLineString(JsonNode node,
            GeometryFactory fac) throws GeoJSONException {
        JsonNode coordinates = requireCoordinates(node);
        LineString[] lineStrings = new LineString[coordinates.size()];
        for (int i = 0; i < coordinates.size(); ++i) {
            JsonNode coords = coordinates.get(i);
            lineStrings[i] = fac.createLineString(decodeCoordinates(coords));
        }
        return fac.createMultiLineString(lineStrings);
    }

    protected LineString decodeLineString(JsonNode node, GeometryFactory fac)
            throws GeoJSONException {
        return fac.createLineString(decodeCoordinates(requireCoordinates(node)));
    }

    protected MultiPoint decodeMultiPoint(JsonNode node, GeometryFactory fac)
            throws GeoJSONException {
        return fac.createMultiPoint(decodeCoordinates(requireCoordinates(node)));
    }

    protected Point decodePoint(JsonNode node, GeometryFactory fac) throws GeoJSONException {
        Coordinate parsed = decodeCoordinate(requireCoordinates(node));
        return fac.createPoint(parsed);
    }

    protected Polygon decodePolygon(JsonNode node, GeometryFactory fac) throws
            GeoJSONException {
        JsonNode coordinates = requireCoordinates(node);
        return decodePolygonCoordinates(coordinates, fac);
    }

    protected MultiPolygon decodeMultiPolygon(JsonNode node, GeometryFactory fac)
            throws GeoJSONException {
        JsonNode coordinates = requireCoordinates(node);
        Polygon[] polygons = new Polygon[coordinates.size()];
        for (int i = 0; i < coordinates.size(); ++i) {
            polygons[i] = decodePolygonCoordinates(coordinates.get(i), fac);
        }
        return fac.createMultiPolygon(polygons);
    }

    protected GeometryCollection decodeGeometryCollection(JsonNode node,
            GeometryFactory fac) throws GeoJSONException {
        final JsonNode geometries = node.path(JSONConstants.GEOMETRIES);
        if (!geometries.isArray()) {
            throw new GeoJSONException("expected 'geometries' array");
        }
        Geometry[] geoms = new Geometry[geometries.size()];
        for (int i = 0; i < geometries.size(); ++i) {
            geoms[i] = decodeGeometry(geometries.get(i), fac);
        }
        return fac.createGeometryCollection(geoms);
    }

    protected JsonNode requireCoordinates(JsonNode node) throws GeoJSONException {
        if (!node.path(JSONConstants.COORDINATES).isArray()) {
            throw new GeoJSONException("missing 'coordinates' field");
        }
        return node.path(JSONConstants.COORDINATES);
    }

    protected Coordinate decodeCoordinate(JsonNode node) throws GeoJSONException {
        if (!node.isArray()) {
            throwExpectedArrayException();
        }
        final int dim = node.size();
        if (dim < DIM_2D) {
            throw new GeoJSONException("coordinates may have at least 2 dimensions");
        }
        if (dim > DIM_3D) {
            throw new GeoJSONException("coordinates may have at most 3 dimensions");
        }
        final Coordinate coordinate = new Coordinate();
        for (int i = 0; i < dim; ++i) {
            if (node.get(i).isNumber()) {
                coordinate.setOrdinate(i, node.get(i).doubleValue());
            } else {
                throw new GeoJSONException("coordinate index " + i + " has to be a number");
            }
        }
        return coordinate;
    }

    protected GeometryFactory getGeometryFactory(int srid,
            GeometryFactory factory) {
        if (srid == factory.getSRID()) {
            return factory;
        } else {
            return new GeometryFactory(DEFAULT_PRECISION_MODEL, srid);
        }
    }

    protected GeometryFactory getGeometryFactory(JsonNode node,
            GeometryFactory factory) throws GeoJSONException {
        if (!node.hasNonNull(JSONConstants.CRS)) {
            return factory;
        } else {
            return decodeCRS(node, factory);
        }
    }

    protected GeometryFactory decodeCRS(JsonNode node, GeometryFactory factory)
            throws GeoJSONException {
        if (!node.path(JSONConstants.CRS).hasNonNull(JSONConstants.TYPE)) {
            throw new GeoJSONException("Missing CRS type");
        }
        String type = node.path(JSONConstants.CRS)
                .path(JSONConstants.TYPE)
                .textValue();
        JsonNode properties = node.path(JSONConstants.CRS)
                .path(JSONConstants.PROPERTIES);
        if (type.equals(JSONConstants.NAME)) {
            return decodeNamedCRS(properties, factory);
        } else if (type.equals(JSONConstants.LINK)) {
            return decodeLinkedCRS(properties, factory);
        } else {
            throw new GeoJSONException("Unknown CRS type: " + type);
        }
    }

    protected GeometryFactory decodeNamedCRS(JsonNode properties,
            GeometryFactory factory) throws GeoJSONException {
        String name = properties.path(JSONConstants.NAME).textValue();
        if (name == null) {
            throw new GeoJSONException("Missing name attribute for name crs");
        }
        for (String prefix : SRS_NAME_PREFIXES) {
            if (name.startsWith(prefix)) {
                try {
                    int srid = Integer.parseInt(name.substring(prefix.length()));
                    return getGeometryFactory(srid, factory);
                } catch (NumberFormatException e) {
                    throw new GeoJSONException("Invalid CRS name", e);
                }
            }
        }
        throw new GeoJSONException("Unsupported named crs: " + name);
    }

    protected GeometryFactory decodeLinkedCRS(JsonNode properties,
            GeometryFactory factory) throws GeoJSONException {
        String href = properties.path(JSONConstants.HREF).textValue();
        if (href == null) {
            throw new GeoJSONException("Missing href attribute for link crs");
        }
        for (String prefix : SRS_LINK_PREFIXES) {
            if (href.startsWith(prefix)) {
                try {
                    int srid = Integer.parseInt(href.substring(prefix.length()));
                    return getGeometryFactory(srid, factory);
                } catch (NumberFormatException e) {
                    throw new GeoJSONException("Invalid CRS link", e);
                }
            }
        }
        throw new GeoJSONException("Unsupported linked crs: " + href);
    }

    protected String getType(final JsonNode node) throws GeoJSONException {
        if (!node.has(JSONConstants.TYPE)) {
            throw new GeoJSONException("Can not determine geometry type (missing 'type' field)");
        }
        if (!node.path(JSONConstants.TYPE).isTextual()) {
            throw new GeoJSONException("'type' field has to be a string");
        }
        return node.path(JSONConstants.TYPE).textValue();
    }

    protected boolean isNumber(JsonNode x) {
        return x == null || !x.isNumber();
    }

    private void throwExpectedArrayException() throws GeoJSONException {
        throw new GeoJSONException("expected array");
    }

}
