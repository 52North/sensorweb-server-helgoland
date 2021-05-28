/*
 * Copyright (C) 2013-2021 52°North Spatial Information Research GmbH
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
package org.n52.io.crs;

import java.io.Serializable;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;


public class BoundingBox implements Serializable {

    private static final long serialVersionUID = -674668726920006020L;

    private Point ll;

    private Point ur;

    private String srs;

    @SuppressWarnings("unused")
    private BoundingBox() {
        // client requires to be default instantiable
    }

    /**
     * @param ll
     *        the lower left corner
     * @param ur
     *        the upper right corner
     * @param srs
     *        the spatial reference system
     */
    public BoundingBox(Point ll, Point ur, String srs) {
        this.ll = ll;
        this.ur = ur;
        this.srs = srs;
    }

    /**
     * Indicates if the given geometry is contained completely by this instance. The point's coordinates are
     * assumed to be in the same coordinate reference system.
     *
     * @param geometry
     *        the geometry to check
     * @return if this instance completely contains the given geometry
     */
    public boolean contains(Geometry geometry) {
        return geometry.getDimension() > 2
            ? !(geometry.contains(ll) || geometry.contains(ur))
            : asEnvelop().contains(geometry.getEnvelopeInternal());
    }

    public Envelope asEnvelop() {
        return new Envelope(ll.getCoordinate(), ur.getCoordinate());
    }

    /**
     * Extends the bounding box with the given point. If point is contained by this instance nothing is
     * changed.
     *
     * @param point
     *        the point in CRS:84 which shall extend the bounding box.
     */
    public void extendBy(Point point) {
        if (!contains(point)) {
            CRSUtils crsUtils = CRSUtils.createEpsgForcedXYAxisOrder();
            this.ll = crsUtils.createPoint(Math.min(point.getX(), this.ll.getX()),
                                           Math.max(point.getX(), this.ur.getX()),
                                           this.srs);
            this.ur = crsUtils.createPoint(Math.min(point.getY(), this.ll.getY()),
                                           Math.max(point.getY(), this.ur.getY()),
                                           this.srs);
        }
    }

    public void setLl(Point ll) {
        this.ll = ll;
    }

    public void setUr(Point ur) {
        this.ur = ur;
    }

    /**
     * @return the lower left corner coordinate.
     */
    public Point getLowerLeft() {
        return ll;
    }

    /**
     * @return the upper right corner coordinate.
     */
    public Point getUpperRight() {
        return ur;
    }

    /**
     * @return the system this instance is referenced in.
     */
    public String getSrs() {
        return srs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BBOX [ (");
        sb.append(ll.getX()).append(",").append(ll.getY()).append(");(");
        sb.append(ur.getX()).append(",").append(ur.getY()).append(") ");
        sb.append("srs: ").append(getSrs());
        return sb.append(" ]").toString();
    }

}
