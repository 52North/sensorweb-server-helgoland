package org.n52.io.response.series;

import java.io.Serializable;

import org.n52.io.geojson.GeoJSONGeometrySerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vividsolutions.jts.geom.Geometry;

public class SeriesData implements Serializable {

    private static final long serialVersionUID = 3119211667773416585L;

    private Geometry geometry;
    
    @JsonSerialize(using = GeoJSONGeometrySerializer.class)
    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
}
