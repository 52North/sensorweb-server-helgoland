/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.series.ckan.beans;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Date;
import java.util.Objects;
import org.n52.series.ckan.da.CkanConstants;
import static org.n52.series.ckan.util.JsonUtil.parseMissingToEmptyString;

public class ResourceField {

    public static ResourceField copy(ResourceField field) {
        return new ResourceField(field.node, field.index);
    }
    
    private ResourceMember qualifier;
    
    private final String fieldId;
    
    private final JsonNode node;
    
    private final int index;
    
    protected ResourceField(String fieldId) {
        // just for testing
        this.fieldId = fieldId.toLowerCase();
        node = null;
        index = -1;
    }

    public ResourceField(JsonNode node, int index) {
        this.node = node;
        this.index = index;
        String id = parseMissingToEmptyString(node, CkanConstants.MemberProperty.FIELD_ID);
        this.fieldId = id.toLowerCase();
    }
    
    public String getFieldId() {
        return fieldId;
    }

    public int getIndex() {
        return index;
    }

    public ResourceMember getQualifier() {
        return qualifier;
    }

    public void setQualifier(ResourceMember qualifier) {
        this.qualifier = qualifier;
    }
    
    public String getShortName() {
        return parseMissingToEmptyString(node, CkanConstants.MemberProperty.SHORT_NAME, CkanConstants.MemberProperty.FIELD_SHORTNAME);
    }

    public String getLongName() {
        return parseMissingToEmptyString(node, CkanConstants.MemberProperty.MEMBER_FIELD_LONG_NAME, CkanConstants.MemberProperty.FIELD_LONGNAME);
    }

    public String getDescription() {
        return parseMissingToEmptyString(node, CkanConstants.MemberProperty.FIELD_DESCRIPTION);
    }

    public String getFieldType() {
        return parseMissingToEmptyString(node, CkanConstants.MemberProperty.FIELD_TYPE, CkanConstants.MemberProperty.FIELDTYPE);
    }
    
    public boolean isField(String knownFieldId) {
        return getFieldId().equalsIgnoreCase(knownFieldId);
    }
    
    public boolean hasProperty(String property) {
        return !node.at("/" + property).isMissingNode();
    }
    
    public String getOther(String name) {
        return node.at("/" + name).asText();
    }
    
    public boolean isOfType(Class<?> clazz) {
        final String fieldType = getFieldType();
        if (clazz == Integer.class) {
            return fieldType.equalsIgnoreCase("Integer")
                    || fieldType.equalsIgnoreCase("int");
        }
        if (clazz == Boolean.class) {
            return fieldType.equalsIgnoreCase("Boolean")
                    || fieldType.equalsIgnoreCase("bool");
        }
        if (clazz == Date.class) {
            return fieldType.equalsIgnoreCase("date")
                    || fieldType.equalsIgnoreCase("datum");
        }
        if (clazz == Double.class) {
            return fieldType.equalsIgnoreCase("float")
                    || fieldType.equalsIgnoreCase("double")
                    || fieldType.equalsIgnoreCase("decimal");
        }
        if (clazz == String.class) {
            return fieldType.equalsIgnoreCase("String")
                    || fieldType.equalsIgnoreCase("text");
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        if (!Objects.equals(this.fieldId, other.fieldId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ResourceField{fieldId=" + fieldId + ", qualifier=" + qualifier + ", index=" + index + '}';
    }

}
