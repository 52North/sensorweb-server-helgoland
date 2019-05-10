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
package org.n52.io.geojson.old;

import java.util.Arrays;

@Deprecated
public class GeojsonPoint extends GeojsonGeometry {

    private static final long serialVersionUID = 4348077077881433456L;

    private static final String GEOJSON_TYPE_POINT = "Point";

    protected Double[] coordinates;

    public static GeojsonPoint createWithCoordinates(Double[] coordinates) {
        GeojsonPoint sfGeometry = new GeojsonPoint();
        //sfGeometry.setCoordinates(Utils.copy(coordinates));
        sfGeometry.setCoordinates(Arrays.copyOf(coordinates, coordinates.length));
        return sfGeometry;
    }

    public void setCoordinates(Double[] coordinates) {
        //this.coordinates = checkCoordinates(Utils.copy(coordinates));
        this.coordinates = assertCoordinates(Arrays.copyOf(coordinates, coordinates.length));
    }

    void setType(String type) {
        // keep for serialization
    }

    @Override
    public String getType() {
        return GEOJSON_TYPE_POINT;
    }

    public Double[] getCoordinates() {
        return Arrays.copyOf(coordinates, coordinates.length);
//        return Utils.copy(coordinates);
    }

}
