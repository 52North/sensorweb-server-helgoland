/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
//
//package org.n52.io.geojson;
//
//import org.geotools.geojson.GeoJSON;
//import org.geotools.geojson.geom.GeometryJSON;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//
//
//
//public class GeojsonPolygon extends GeojsonGeometry {
//
//    private static final long serialVersionUID = 1238376453113150366L;
//    
//    private static final String GEOJSON_TYPE_POLYGON = "Polygon";
//    
//    public static GeojsonPolygon create(Double[][] polygonCoordinates) {
//        GeojsonPolygon sfGeometry = new GeojsonPolygon();
//        sfGeometry.setCoordinates(coordinatesArray);
//        return sfGeometry;
//    }
//
//    public String getType() {
//        return GEOJSON_TYPE_POLYGON;
//    }
//
//    public Double[][][] getCoordinates() {
//        return coordinates.toArray(new Double[0][][]);
//    }
//    
//    public void addCoordinates(Double[] coordinates) {
//        this.coordinates.add(checkCoordinates(coordinates));
//    }
//    
//    @JsonIgnore
//    public GeojsonPolygon getBounds() {
//        double minx = 0d;
//        double maxx = 0d;
//        double miny = 0d;
//        double maxy = 0d;
//        for (Double[] coordinates : this.coordinates) {
//            minx = Math.min(coordinates[0].doubleValue(), minx);
//            maxx = Math.max(coordinates[0].doubleValue(), maxx);
//            miny = Math.min(coordinates[1].doubleValue(), miny);
//            maxy = Math.max(coordinates[1].doubleValue(), maxy);
//        }
//        Double[] ll = new Double[] {new Double(minx), new Double(miny)};
//        Double[] ur = new Double[] {new Double(maxx), new Double(maxy)};
//        return GeojsonPolygon.create(new Double[][]{ll,ur});
//    }
//
//}
