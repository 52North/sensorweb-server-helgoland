package org.n52.series.ckan.beans;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;
import java.util.Objects;
import org.n52.series.ckan.da.CkanConstants;
import static org.n52.series.ckan.util.JsonUtil.parseMissingToEmptyString;

public class ResourceField {
    
    private final String fieldId;
    
    private final JsonNode node;

    public ResourceField(JsonNode node) {
        this.node = node;
        this.fieldId = parseMissingToEmptyString(CkanConstants.MEMBER_FIELD_ID, node);
    }
    
    public Iterator<String> getFieldNames() {
        return node.fieldNames();
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getShortName() {
        return parseMissingToEmptyString(CkanConstants.MEMBER_FIELD_SHORTNAME, node);
    }


    public String getLongName() {
        return parseMissingToEmptyString(CkanConstants.MEMBER_FIELD_LONGNAME, node);
    }

    public String getDescription() {
        return parseMissingToEmptyString(CkanConstants.MEMBER_FIELD_DESCRIPTION, node);
    }

    public String getFieldType() {
        return parseMissingToEmptyString(CkanConstants.MEMBER_FIELD_TYPE, node);
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
    
    
}
