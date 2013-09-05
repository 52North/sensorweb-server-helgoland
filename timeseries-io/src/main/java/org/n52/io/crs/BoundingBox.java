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

package org.n52.io.crs;

import java.io.Serializable;

import com.vividsolutions.jts.geom.Point;

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
     */
    public BoundingBox(Point ll, Point ur, String srs) {
        this.ll = ll;
        this.ur = ur;
        this.srs = srs;
    }

    /**
     * Indicates if the given point is contained by this instance. The point's coordinates are assumed to be
     * in the same coordinate reference system.
     * 
     * @param point
     *        the point to check.
     * @return if this instance contains the given coordiantes.
     */
    public boolean contains(Point point) {
        return isWithinXRange(point.getX()) && isWithinYRange(point.getY());
    }

    private boolean isWithinXRange(double x) {
        return ll.getX() <= x && x <= ur.getX();
    }

    private boolean isWithinYRange(double y) {
        return ll.getY() <= y && y <= ur.getY();
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
