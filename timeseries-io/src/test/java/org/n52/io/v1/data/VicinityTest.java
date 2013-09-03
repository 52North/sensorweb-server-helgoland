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

package org.n52.io.v1.data;

import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;
import org.n52.io.crs.BoundingBox;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VicinityTest {
    
    private static final double ERROR_DELTA = 0.1;

    private String circleAroundNorthPole = "{\"center\":[\"-89.99\",\"89.999\"],\"radius\":\"500\"}";
    
    private String circleAroundSouthPole = "{\"center\":[\"23\",\"-89.999\"],\"radius\":\"500\"}";
    
    private String circleCenterAtGreenwhichAndEquator = "{\"center\":[\"0\",\"0\"],\"radius\":\"500\"}";

    @Test
    public void
    shouldHaveInversedLatitudesWhenCenterIsOnEquator()
    {
        Vicinity vicinity = createRadiusAtNorthPole(circleCenterAtGreenwhichAndEquator);
        BoundingBox bounds = vicinity.calculateBounds();
        double llLatitudeOfSmallCircle = bounds.getLowerLeftCorner().getNorthing();
        double urLatitudeOfSmallCircle = bounds.getUpperRightCorner().getNorthing();
        assertThat(llLatitudeOfSmallCircle, closeTo(-urLatitudeOfSmallCircle, ERROR_DELTA));
    }

    @Test
    public void
    shouldHaveInversedLongitudesWhenCenterIsOnGreenwhich()
    {
        Vicinity vicinity = createRadiusAtNorthPole(circleCenterAtGreenwhichAndEquator);
        BoundingBox bounds = vicinity.calculateBounds();
        double llLongitudeOfGreatCircle = bounds.getLowerLeftCorner().getEasting();
        double urLongitudeOnGreatCircle = bounds.getUpperRightCorner().getEasting();
        assertThat(llLongitudeOfGreatCircle, closeTo(-urLongitudeOnGreatCircle, ERROR_DELTA));
    }
    
    @Test
    public void
    shouldHaveCommonLatitudeCircleWhenCenterIsNorthPole()
    {
        Vicinity vicinity = createRadiusAtNorthPole(circleAroundNorthPole);
        BoundingBox bounds = vicinity.calculateBounds();
        double llLatitudeOfSmallCircle = bounds.getLowerLeftCorner().getNorthing();
        double urLatitudeOfSmallCircle = bounds.getUpperRightCorner().getNorthing();
        assertThat(llLatitudeOfSmallCircle, closeTo(urLatitudeOfSmallCircle, ERROR_DELTA));
    }

    @Test
    public void
    shouldHaveCommonLatitudeCircleWhenCenterIsSouthPole()
    {
            Vicinity vicinity = createRadiusAtNorthPole(circleAroundSouthPole);
            BoundingBox bounds = vicinity.calculateBounds();
            double llLatitudeOfSmallCircle = bounds.getLowerLeftCorner().getNorthing();
            double urLatitudeOfSmallCircle = bounds.getUpperRightCorner().getNorthing();
            assertThat(llLatitudeOfSmallCircle, closeTo(urLatitudeOfSmallCircle, ERROR_DELTA));
    }

    private Vicinity createRadiusAtNorthPole(String circleJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(circleJson, Vicinity.class);
        }
        catch (JsonParseException e) {
            fail("Could not parse GeoJson");
        }
        catch (IOException e) {
            fail("Could not read GeoJson");
        }
        return null;
    }
    
}
