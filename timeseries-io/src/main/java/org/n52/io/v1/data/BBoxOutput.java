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
//package org.n52.io.v1.data;
//
//import org.n52.io.geojson.GeojsonPolygon;
//
//public class BBoxOutput {
//
//    private GeojsonPolygon geometry;
//
//    /**
//     * @param polygon
//     *        the bbox's geometry. If not a box itself, the bbox of the given polygon is being used.
//     */
//    private BBoxOutput(GeojsonPolygon polygon) {
//        this.geometry = polygon.getBounds();
//    }
//    
//    public GeojsonPolygon getGeometry() {
//        return geometry;
//    }
//
//    public void setGeometry(GeojsonPolygon geometry) {
//        this.geometry = geometry.getBounds();
//    }
//
//    public static BBoxOutput createZeroBBoxOutput() {
//        GeojsonPolygon zeroBBox = new GeojsonPolygon();
//        zeroBBox.addCoordinates(new Double[] {0d, 0d});
//        zeroBBox.addCoordinates(new Double[] {0d, 0d});
//        zeroBBox.addCoordinates(new Double[] {0d, 0d});
//        zeroBBox.addCoordinates(new Double[] {0d, 0d});
//        return new BBoxOutput(zeroBBox);
//    }
//
//    public static BBoxOutput createBBoxFor(GeojsonPolygon geometry) {
//        return new BBoxOutput(geometry);
//    }
//
//}
