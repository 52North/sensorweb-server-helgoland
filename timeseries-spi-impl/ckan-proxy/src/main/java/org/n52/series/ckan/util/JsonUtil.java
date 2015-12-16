package org.n52.series.ckan.util;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtil {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);
    
    public static String parseMissingToEmptyString(JsonNode node, String fieldName, String... alternateFieldNames) {
        JsonNode field = findField(node, fieldName, alternateFieldNames);
        return !field.isMissingNode()
                ? field.asText()
                : "";
    }

    public static int parseMissingToNegativeInt(JsonNode node, String fieldName, String... alternateFieldNames) {
        JsonNode field = findField(node, fieldName, alternateFieldNames);
        return !field.isMissingNode()
                ? field.asInt()
                : -1;
    }
    
    private static JsonNode findField(JsonNode node, String fieldName, String[] alternateFieldNames) {
        JsonNode field = getNodeWithName(fieldName, node);
        field = tryLowerCasedIfMissing(field, fieldName, node);
        if (field.isMissingNode() && alternateFieldNames != null) {
            for (String alternateFieldName : alternateFieldNames) {
                field = getNodeWithName(alternateFieldName, node);
                field = tryLowerCasedIfMissing(field, alternateFieldName, node);
                if ( !field.isMissingNode()) {
                    LOGGER.debug("found node with deprecated property '{}'", alternateFieldName);
                    break;
                }
            }
        }
        return field;
    }

    private static JsonNode getNodeWithName(String fieldName, JsonNode node) {
        return node.at("/" + fieldName);
    }
    
    private static JsonNode tryLowerCasedIfMissing(JsonNode field, String fieldName, JsonNode node) {
        return field.isMissingNode()
                ? getNodeWithLowerCasedName(fieldName, node)
                : field;
    }

    private static JsonNode getNodeWithLowerCasedName(String fieldName, JsonNode node) {
        return node.at("/" + fieldName.toLowerCase());
    }
    

}
