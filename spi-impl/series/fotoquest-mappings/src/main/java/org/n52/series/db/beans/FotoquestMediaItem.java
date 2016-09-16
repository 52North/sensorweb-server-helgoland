package org.n52.series.db.beans;

import java.util.HashMap;
import java.util.Map;

import org.n52.io.geojson.FeatureOutputSerializer;
import org.n52.io.geojson.GeoJSONFeature;
import org.n52.io.geojson.GeoJSONObject;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.vividsolutions.jts.geom.Geometry;

@JsonSerialize(using = FeatureOutputSerializer.class, as = GeoJSONObject.class)
public class FotoquestMediaItem implements GeoJSONFeature {

    private Long id;

    private String url;

    private Geometry geometry;

    @Override
    public String getId() {
        return id != null
                ? id.toString()
                : null;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public Geometry getGeometry() {
        return geometry;
    }

    @Override
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @Override
    public boolean isSetGeometry() {
        return !(geometry == null || geometry.isEmpty());
    }

    @Override
    public Map<String, Object> getProperties() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("id", getId());
        properties.put("href", url);
        return properties;
    }

}
