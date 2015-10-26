/**
 * Copyright (C) 2012-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.io.geojson;

import com.fasterxml.jackson.databind.JsonNode;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 *
 * borrowed from https://github.com/52North/SOS/blob/4.3.4/coding/json/src/main/java/org/n52/sos/decode/json/impl/GeoJSONDecoder.java
 * 
 * @since 2.0
 */
public class GeoJSONDecoder {

	private static final PrecisionModel DEFAULT_PRECISION_MODEL = new PrecisionModel(
			PrecisionModel.FLOATING);

	private static final GeometryFactory DEFAULT_GEOMETRY_FACTORY = new GeometryFactory(
			DEFAULT_PRECISION_MODEL, 4326);
	
    public static final int DIM_2D = 2;

    public static final int DIM_3D = 3;
    
	private static final String TYPE = "type";

	private static final String POINT = "Point";
	
	private static final String MULTI_POINT = "MultiPoint";
	
	private static final String LINE_STRING = "LineString";
	
	private static final String MULTI_LINE_STRING = "MultiLineString";
	
	private static final String POLYGON = "Polygon";
	
	private static final String MULTI_POLYGON = "MultiPolygon";
	
	private static final String GEOMETRY_COLLECTION = "GeometryCollection";
	
	private static final String COORDINATES = "coordinates";
	
	private static final String GEOMETRIES = "geometries";
	
	private static final int DEFAULT_SRID = 4326;
	
	
	public static Geometry decode(JsonNode node) {
		if (node == null || node.isNull() || node.isMissingNode()) {
			return null;
		} else {
			return decodeGeometry(node, DEFAULT_GEOMETRY_FACTORY);
		}
	}

	protected static Geometry decodeGeometry(Object o, GeometryFactory parentFactory) {
		if (!(o instanceof JsonNode)) {
			// throw new GeoJSONException("Cannot decode " + o);
		}
		final JsonNode node = (JsonNode) o;
		final String type = getType(node);
		final GeometryFactory factory = getGeometryFactory();
		Geometry geometry = null;
		if (type.equals(POINT)) {
			geometry = decodePoint(node, factory);
		} else if (type.equals(MULTI_POINT)) {
			geometry = decodeMultiPoint(node, factory);
		} else if (type.equals(LINE_STRING)) {
			geometry = decodeLineString(node, factory);
		} else if (type.equals(MULTI_LINE_STRING)) {
			geometry = decodeMultiLineString(node, factory);
		} else if (type.equals(POLYGON)) {
			geometry = decodePolygon(node, factory);
		} else if (type.equals(MULTI_POLYGON)) {
			geometry = decodeMultiPolygon(node, factory);
		} else if (type.equals(GEOMETRY_COLLECTION)) {
			geometry = decodeGeometryCollection(node, factory);
		} else {
//			throw new GeoJSONException("Unkown geometry type: " + type);
		}
		if (geometry != null) {
			geometry.setSRID(DEFAULT_SRID);
		}
		return geometry;
	}
	
    protected static Coordinate[] decodeCoordinates(JsonNode node) {
        if (!node.isArray()) {
//            throw new GeoJSONException("expected array");
        }
        Coordinate[] coordinates = new Coordinate[node.size()];
        for (int i = 0; i < node.size(); ++i) {
            coordinates[i] = decodeCoordinate(node.get(i));
        }
        return coordinates;
    }

    protected static Polygon decodePolygonCoordinates(JsonNode coordinates, GeometryFactory fac) {
        if (!coordinates.isArray()) {
//            throw new GeoJSONException("expected array");
        }
        if (coordinates.size() < 1) {
//            throw new GeoJSONException("missing polygon shell");
        }
        LinearRing shell = fac.createLinearRing(decodeCoordinates(coordinates.get(0)));
        LinearRing[] holes = new LinearRing[coordinates.size() - 1];
        for (int i = 1; i < coordinates.size(); ++i) {
            holes[i - 1] = fac.createLinearRing(decodeCoordinates(coordinates.get(i)));
        }
        return fac.createPolygon(shell, holes);
    }

	protected static MultiLineString decodeMultiLineString(JsonNode node, GeometryFactory fac) {
		JsonNode coordinates = requireCoordinates(node);
		LineString[] lineStrings = new LineString[coordinates.size()];
		for (int i = 0; i < coordinates.size(); ++i) {
			JsonNode coords = coordinates.get(i);
			lineStrings[i] = fac.createLineString(decodeCoordinates(coords));
		}
		return fac.createMultiLineString(lineStrings);
	}

	protected static LineString decodeLineString(JsonNode node, GeometryFactory fac) {
		Coordinate[] coordinates = decodeCoordinates(requireCoordinates(node));
		return fac.createLineString(coordinates);
	}

	protected static MultiPoint decodeMultiPoint(JsonNode node, GeometryFactory fac) {
		Coordinate[] coordinates = decodeCoordinates(requireCoordinates(node));
		return fac.createMultiPoint(coordinates);
	}

	protected static Point decodePoint(JsonNode node, GeometryFactory fac) {
		Coordinate parsed = decodeCoordinate(requireCoordinates(node));
		return fac.createPoint(parsed);
	}

	protected static Polygon decodePolygon(JsonNode node, GeometryFactory fac)  {
		JsonNode coordinates = requireCoordinates(node);
		return decodePolygonCoordinates(coordinates, fac);
	}

	protected static MultiPolygon decodeMultiPolygon(JsonNode node, GeometryFactory fac) {
		JsonNode coordinates = requireCoordinates(node);
		Polygon[] polygons = new Polygon[coordinates.size()];
		for (int i = 0; i < coordinates.size(); ++i) {
			polygons[i] = decodePolygonCoordinates(coordinates.get(i), fac);
		}
		return fac.createMultiPolygon(polygons);
	}

	protected static GeometryCollection decodeGeometryCollection(JsonNode node, GeometryFactory fac) {
		final JsonNode geometries = node.path(GEOMETRIES);
		if (!geometries.isArray()) {
//			throw new GeoJSONException("expected 'geometries' array");
		}
		Geometry[] geoms = new Geometry[geometries.size()];
		for (int i = 0; i < geometries.size(); ++i) {
			geoms[i] = decodeGeometry(geometries.get(i), fac);
		}
		return fac.createGeometryCollection(geoms);
	}

	protected static JsonNode requireCoordinates(JsonNode node) {
		if (!node.path(COORDINATES).isArray()) {
//			throw new GeoJSONException("missing 'coordinates' field");
		}
		return node.path(COORDINATES);
	}

	protected static Coordinate decodeCoordinate(JsonNode node) {
		if (!node.isArray()) {
//			throw new GeoJSONException("expected array");
		}
		final int dim = node.size();
		if (dim < DIM_2D) {
//			throw new GeoJSONException("coordinates may have at least 2 dimensions");
		}
		if (dim > DIM_3D) {
//			throw new GeoJSONException("coordinates may have at most 3 dimensions");
		}
		final Coordinate coordinate = new Coordinate();
		for (int i = 0; i < dim; ++i) {
			if (node.get(i).isNumber()) {
				double value = node.get(i).doubleValue();
				 switch (i) {
			      case 0:
			    	  coordinate.y = value;
			        break;
			      case 1:
			    	  coordinate.x = value;
			        break;
			      case 2:
			    	  coordinate.z = value;
			        break;
			      default:
			        throw new IllegalArgumentException("Invalid index: " + i);
			    }
			} else {
//				throw new GeoJSONException("coordinate index " + i
//						+ " has to be a number");
			}
		}
		return coordinate;
	}

	protected static GeometryFactory getGeometryFactory() {
		 return DEFAULT_GEOMETRY_FACTORY;
	}

	protected static String getType(final JsonNode node) {
		if (!node.has(TYPE)) {
//			throw new GeoJSONException(
//					"Can not determine geometry type (missing 'type' field)");
		}
		if (!node.path(TYPE).isTextual()) {
//			throw new GeoJSONException("'type' field has to be a string");
		}
		return node.path(TYPE).textValue();
	}

	protected static boolean isNumber(JsonNode x) {
		return x == null || !x.isNumber();
	}

}
