package org.n52.series.ckan.beans;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.Objects;
import org.n52.series.ckan.da.CkanConstants;
import static org.n52.series.ckan.util.JsonUtil.parseMissingToEmptyString;

public class ResourceField {
    
    private final String fieldId;
    
    private final JsonNode node;
    
    protected ResourceField(String fieldId) {
        this.fieldId = fieldId.toLowerCase();
        node = null;
    }

    public ResourceField(JsonNode node) {
        this.node = node;
        String id = parseMissingToEmptyString(node, CkanConstants.MEMBER_FIELD_ID);
        this.fieldId = id.toLowerCase();
    }
    
    public Iterator<String> getFieldNames() {
        return node.fieldNames();
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getShortName() {
        return parseMissingToEmptyString(node, CkanConstants.MEMBER_FIELD_SHORT_NAME, CkanConstants.MEMBER_FIELD_SHORTNAME);
    }


    public String getLongName() {
        return parseMissingToEmptyString(node, CkanConstants.MEMBER_FIELD_LONG_NAME, CkanConstants.MEMBER_FIELD_LONGNAME);
    }

    public String getDescription() {
        return parseMissingToEmptyString(node, CkanConstants.MEMBER_FIELD_DESCRIPTION);
    }

    public String getFieldType() {
        return parseMissingToEmptyString(node, CkanConstants.MEMBER_FIELD_TYPE, CkanConstants.MEMBER_FIELDTYPE);
    }
    
    public String getOther(String name) {
        return node.at(name).asText();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.fieldId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResourceField other = (ResourceField) obj;
        if (this.fieldId == null || other.fieldId == null) {
            return false;
        }
        return Objects.equals(this.fieldId.toLowerCase(), other.fieldId.toLowerCase());
    }

    @Override
    public String toString() {
        return "ResourceField{" + "fieldId=" + fieldId + '}';
    }
    
}
