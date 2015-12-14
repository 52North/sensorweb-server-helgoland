package org.n52.series.ckan.util;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonUtil {
    
    public static String parseMissingToEmptyString(String fieldName, JsonNode node) {
        JsonNode field = node.at("/" + fieldName);
        field = field.isMissingNode()
                ? node.at("/" + fieldName.toLowerCase())
                : field;
        return !field.isMissingNode()
                ? field.asText()
                : "";
    }
    
    public static int parseMissingToNegativeInt(String fieldName, JsonNode node) {
        JsonNode field = node.at("/" + fieldName);
        field = field.isMissingNode()
                ? node.at("/" + fieldName.toLowerCase())
                : field;
        return !field.isMissingNode()
                ? field.asInt()
                : -1;
    }
    

}
