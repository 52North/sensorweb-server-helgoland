/*
 * Copyright (C) 2013-2021 52°North Initiative for Geospatial Open Source
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

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.n52.io.crs.BoundingBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.n52.io.geojson.GeoJSONDecoder;
import org.n52.io.geojson.GeoJSONException;

public class VicinityTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(VicinityTest.class);

    private static final double ERROR_DELTA = 0.1;

    private final String circleAroundNorthPole = ""
            + "{"
            + "  \"type\": \"Point\", "
            + "  \"coordinates\": [-89.99,89.999] "
            + "}";

    private final String circleAroundSouthPole = ""
            + "{"
            + "  \"type\": \"Point\","
            + "  \"coordinates\": [-89.99,-89.999]"
            + "}";

    private final String circleCenterAtGreenwhichAndEquator = ""
            + "{"
            + "   \"type\": \"Point\","
            + "   \"coordinates\": [ 0,0 ]"
            + "}";

    @Test
    public void
            shouldHaveInversedLatitudesWhenCenterIsOnEquator() throws GeoJSONException, IOException {
        Vicinity vicinity = createVicinity(circleCenterAtGreenwhichAndEquator, 500);
        BoundingBox bounds = vicinity.calculateBounds();
        double llLatitudeOfSmallCircle = bounds.getLowerLeft().getY();
        double urLatitudeOfSmallCircle = bounds.getUpperRight().getY();
        assertThat(llLatitudeOfSmallCircle, closeTo(-urLatitudeOfSmallCircle, ERROR_DELTA));
    }

    @Test
    public void
            shouldHaveInversedLongitudesWhenCenterIsOnGreenwhich() throws GeoJSONException, IOException {
        Vicinity vicinity = createVicinity(circleCenterAtGreenwhichAndEquator, 500);
        BoundingBox bounds = vicinity.calculateBounds();
        double llLongitudeOfGreatCircle = bounds.getLowerLeft().getX();
        double urLongitudeOnGreatCircle = bounds.getUpperRight().getX();
        assertThat(llLongitudeOfGreatCircle, closeTo(-urLongitudeOnGreatCircle, ERROR_DELTA));
    }

    @Test
    public void
            shouldHaveCommonLatitudeCircleWhenCenterIsNorthPole() throws GeoJSONException, IOException {
        Vicinity vicinity = createVicinity(circleAroundNorthPole, 500);
        BoundingBox bounds = vicinity.calculateBounds();
        double llLatitudeOfSmallCircle = bounds.getLowerLeft().getY();
        double urLatitudeOfSmallCircle = bounds.getUpperRight().getY();
        assertThat(llLatitudeOfSmallCircle, closeTo(urLatitudeOfSmallCircle, ERROR_DELTA));
    }

    @Test
    public void
            shouldHaveCommonLatitudeCircleWhenCenterIsSouthPole() throws GeoJSONException, IOException {
        Vicinity vicinity = createVicinity(circleAroundSouthPole, 500);
        BoundingBox bounds = vicinity.calculateBounds();
        double llLatitudeOfSmallCircle = bounds.getLowerLeft().getY();
        double urLatitudeOfSmallCircle = bounds.getUpperRight().getY();
        assertThat(llLatitudeOfSmallCircle, closeTo(urLatitudeOfSmallCircle, ERROR_DELTA));
    }

    private Vicinity createVicinity(String center, int radius) throws GeoJSONException, IOException {
        Vicinity vicinity = new Vicinity();
        vicinity.setRadius(radius);
        ObjectMapper mapper = new ObjectMapper();
        final JsonNode node = mapper.readTree(center);
        Point p = (Point) new GeoJSONDecoder().decodeGeometry(node);
        vicinity.setCenter(p);
        return vicinity;
    }

}
