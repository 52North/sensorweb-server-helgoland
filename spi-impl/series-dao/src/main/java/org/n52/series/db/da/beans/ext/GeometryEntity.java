package org.n52.series.db.da.beans.ext;

import com.vividsolutions.jts.geom.Geometry;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class GeometryEntity {

    private Geometry geometry;

    private Double lon;

    private Double lat;

    public boolean isSetGeometry() {
        return geometry != null;
    }

    public boolean isSetLonLat() {
        return lon != null && lat != null;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

}
