package org.n52.io.extension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class MetadataJsonEntitiyTest {
    
    @Test
    public void givenMetadataJsonEntity_whenSerialize_ValueAsJsonNode() throws JsonProcessingException, IOException {
        MetadataJsonEntity entity = new MetadataJsonEntity();
        entity.setPkid(1L);
        entity.setName("some_metadata");
        entity.setSeriesId(1L);
        entity.setType("json");
        entity.setValue("{\"key\":\"value\",\"object\":{\"key1\":\"string\",\"key2\":42}}");
        
        ObjectMapper om = new ObjectMapper();
        String jsonString = om.writeValueAsString(entity);
        JsonNode jsonNode = om.readTree(jsonString);
        JsonNode at = jsonNode.path("value").path("object");
        Assert.assertTrue(at.isObject());
    }
            
}
