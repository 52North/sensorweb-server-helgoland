package org.n52.io.geojson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.vividsolutions.jts.geom.Geometry;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.n52.io.response.v2.FeatureOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoJSONSerializer extends JsonSerializer<FeatureOutput> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoJSONSerializer.class);
    
    // TODO transform to requested crs
    // configure encoder

    @Override
    public void serialize(FeatureOutput value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        gen.writeStringField("type", "Feature");
        gen.writeStringField("id", value.getId());
        gen.writeObjectField("properties", encodeProperties(value));
        gen.writeObjectField("geometry", encodeGeometry(value));
        gen.writeEndObject();
    }

    private Object encodeGeometry(FeatureOutput value) {
        try {
            final GeoJSONEncoder enc = new GeoJSONEncoder();
            final Geometry geometry = value.getGeometry();
            return enc.encodeGeometry(geometry);
        } catch (GeoJSONException e) {
            LOGGER.error("could not properly encode geometry.", e);
            return null;
        }
    }

    private Map<String, Object> encodeProperties(FeatureOutput value) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", value.getId());
        properties.put("label", value.getLabel());
        properties.put("type", value.getFeatureType());
        properties.put("domainId", value.getDomainId());
        properties.putAll(value.getProperties());
        return properties;
    }
    
}
