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

import static java.lang.Double.parseDouble;
import static java.lang.Math.toRadians;
import static org.n52.io.crs.CRSUtils.DEFAULT_CRS;
import static org.n52.io.crs.CRSUtils.createEpsgStrictAxisOrder;
import static org.n52.io.crs.WGS84Util.getLatitudeDelta;
import static org.n52.io.crs.WGS84Util.getLongitudeDelta;
import static org.n52.io.crs.WGS84Util.normalizeLatitude;
import static org.n52.io.crs.WGS84Util.normalizeLongitude;

import org.n52.io.crs.BoundingBox;
import org.n52.io.crs.CRSUtils;
import org.n52.io.geojson.GeojsonPoint;

import com.vividsolutions.jts.geom.Point;

/**
 * Represents the surrounding area based on a center and a radius. All coordinate calculations are based on a
 * EPSG:4326, lon-lat ordered reference frame.
 */
public class Vicinity {

    /**
     * The coordinate reference system. Defaults to {@link CRSUtils#DEFAULT_CRS}.
     */
    private String crs = DEFAULT_CRS;

    private GeojsonPoint center;

    private double radius;

    Vicinity() {
        // for serialization
    }

    /**
     * @param center
     *        the center point.
     * @param radius
     *        the distance around the center
     */
    public Vicinity(GeojsonPoint center, String radius) {
        try {
            this.radius = parseDouble(radius);
            this.center = center;
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse radius.");
        }
    }

    /**
     * @return calculates bounding box within WGS84 and strict EPSG axes order context.
     */
    public BoundingBox calculateBounds() {
        return calculateBounds(createEpsgStrictAxisOrder());
    }

    /**
     * Calculates bounding box with the given CRS context.
     * 
     * @param crsUtils
     *        the reference context.
     * @return a bounding rectangle.
     */
    public BoundingBox calculateBounds(CRSUtils crsUtils) {

        Point center = createCenter(this.center, crsUtils);

        double latInRad = toRadians(center.getY());
        double llEasting = normalizeLongitude(center.getX() - getLongitudeDelta(latInRad, radius));
        double llNorthing = normalizeLatitude(center.getY() - getLatitudeDelta(radius));
        double urEasting = normalizeLongitude(center.getX() + getLongitudeDelta(latInRad, radius));
        double urNorthing = normalizeLatitude(center.getY() + getLatitudeDelta(radius));
        
        if (crsUtils.isLatLonAxesOrder(crs)) {
            Point ll = crsUtils.createPoint(llNorthing, llEasting, crs);
            Point ur = crsUtils.createPoint(urNorthing, urEasting, crs);
            return new BoundingBox(ll, ur, crs);
        }
        Point ll = crsUtils.createPoint(llEasting, llNorthing, crs);
        Point ur = crsUtils.createPoint(urEasting, urNorthing, crs);
        return new BoundingBox(ll, ur, crs);
    }

    /**
     * @param center
     *        the center point as GeoJSON point.
     * @param crsUtils
     *        the reference context.
     * @return the center point.
     */
    private Point createCenter(GeojsonPoint center, CRSUtils crsUtils) {
        Double easting = center.getCoordinates()[0];
        Double northing = center.getCoordinates()[1];
        return crsUtils.createPoint(easting, northing, DEFAULT_CRS);
    }

    /**
     * @param crs
     *        sets the coordinate reference system, e.g. 'EPSG:25832'
     */
    public void setCrs(String crs) {
        if (crs != null) {
            this.crs = crs;
        }
    }

    /**
     * @param center
     *        the center coordinates.
     */
    public void setCenter(GeojsonPoint center) {
        this.center = center;
    }
    
    public GeojsonPoint getCenter() {
        return center;
    }

    /**
     * @param radius
     *        the vicinity's radius.
     * @throws NumberFormatException
     *         if radius could not be parsed to a double value.
     */
    public void setRadius(String radius) {
        this.radius = parseDouble(radius);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [ ");
        sb.append("Center: ").append(center).append(", ");
        sb.append("Radius: ").append(radius).append(" km");
        return sb.append(" ]").toString();
    }

}
