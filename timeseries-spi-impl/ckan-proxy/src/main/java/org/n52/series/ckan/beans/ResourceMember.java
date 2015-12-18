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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ResourceMember {
    
    private String id;
    
    private int headerRows;
    
    private String resourceType;
    
    private List<ResourceField> resourceFields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public int getHeaderRows() {
        return headerRows;
    }

    public void setHeaderRows(int headerRows) {
        this.headerRows = headerRows;
    }

    public List<ResourceField> getResourceFields() {
        return Collections.unmodifiableList(resourceFields);
    }
    
    public ResourceField getField(String fieldId) {
        for (ResourceField field : resourceFields) {
            if (field.getFieldId().equalsIgnoreCase(fieldId)) {
                return field;
            }
        }
        return null;
    }
    
    public ResourceField getField(int index) {
        return index >= 0 && index < resourceFields.size()
                ? resourceFields.get(index)
                : null;
    }
    
    public boolean containsField(String fieldId) {
        for (ResourceField field : resourceFields) {
            if (field.getFieldId().equalsIgnoreCase(fieldId)) {
                return true;
            }
        }
        return false;
    }
    
    public List<String> getColumnHeaders() {
        List<String> headers = new ArrayList<>();
        for (ResourceField field : getResourceFields()) {
            headers.add(field.getShortName());
        }
        return headers;
    }
    
    public List<String> getFieldIds() {
        List<String> fieldIds = new ArrayList<>();
        for (ResourceField field : getResourceFields()) {
            fieldIds.add(field.getFieldId());
        }
        return fieldIds;
    }
    
    public Set<ResourceField> getJoinFields(ResourceMember other) {
        if ( !isJoinable(other)) {
            return Collections.<ResourceField>emptySet();
        }
        Set<ResourceField> joinFields = new HashSet<>();
        for (ResourceField otherField : other.resourceFields) {
            if (resourceFields.contains(otherField)) {
                joinFields.add(otherField);
            }
        }
        return joinFields;
    }

    public Set<ResourceField> getJoinableFields(ResourceMember other) {
        if ( !isJoinable(other)) {
            return Collections.<ResourceField>emptySet();
        }
        // TODO remove actial join column(s)?!
        Set<ResourceField> fields = new HashSet<>(other.resourceFields);
        return Collections.unmodifiableSet(fields);
    }
    
    private boolean isJoinable(ResourceMember other) {
        if (this == other || resourceType.equalsIgnoreCase(other.resourceType)) {
            return false;
        }
        for (ResourceField otherField : other.resourceFields) {
            if (resourceFields.contains(otherField)) {
                return true;
            }
        }
        return false;
    }

    public void setResourceFields(List<ResourceField> resourceFields) {
        this.resourceFields = resourceFields;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.id);
        hash = 17 * hash + Objects.hashCode(this.resourceType);
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
        final ResourceMember other = (ResourceMember) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.resourceType, other.resourceType)) {
            return false;
        }
        return true;
    }

    
    
    
}
    
