package org.n52.io.response.v1.ext;

public class DatasetType {

    private static final String SEPERATOR = "_";

    public static String extractType(String id) {
        if (id == null || id.isEmpty()) {
            return id;
        }
        return id.substring(0, id.indexOf(SEPERATOR));
    }
    public static String extractId(String id) {
        if (id == null || id.isEmpty()) {
            return id;
        }
        return id.substring(id.indexOf(SEPERATOR) + 1);
    }
}
