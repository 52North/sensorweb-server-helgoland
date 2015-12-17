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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.n52.series.ckan.da.CkanConstants;

public class CsvObservationsCollection {
    
    private final String datasetId;
    
    private final String description;
    
    private final Map<ResourceMember, DataFile> dataCollection;
    
    private final DescriptionFile schemaDescriptor;
    
    public CsvObservationsCollection(String datasetId, DescriptionFile description, Map<String, DataFile> csvContents) {
        this.datasetId = datasetId;
        SchemaDescriptor descriptor = description.getSchemaDescription();
        this.dataCollection = descriptor.relateWithDataFiles(csvContents);
        this.description = descriptor.getDescription();
        this.schemaDescriptor = description;
    }
    
    public String getDatasetId() {
        return datasetId;
    }

    public String getDescription() {
        return description;
    }

    public DescriptionFile getSchemaDescriptor() {
        return schemaDescriptor;
    }

    public Map<ResourceMember, DataFile> getDataCollection() {
        return Collections.unmodifiableMap(dataCollection);
    }
    
    public Map<ResourceMember, DataFile> getMetadataCollection() {
        Map<ResourceMember, DataFile> typedCollection = new HashMap<>();
        for (Map.Entry<ResourceMember, DataFile> entry : dataCollection.entrySet()) {
            ResourceMember member = entry.getKey();
            final String resourceType = member.getResourceType();
            if ( !resourceType.equalsIgnoreCase(CkanConstants.ResourceType.OBSERVATIONS)) {
                typedCollection.put(member, entry.getValue());
            }
        }
        return typedCollection;
    }
    
    public Map<ResourceMember, DataFile> getObservationDataCollections() {
        return getDataCollectionsOfType(CkanConstants.ResourceType.OBSERVATIONS);
    }
    
    public Map<ResourceMember, DataFile> getPlatformDataCollections() {
        return getDataCollectionsOfType(CkanConstants.ResourceType.PLATFORMS);
    }
    
    public Map<ResourceMember, DataFile> getDataCollectionsOfType(String type) {
        Map<ResourceMember, DataFile> typedCollection = new HashMap<>();
        for (Map.Entry<ResourceMember, DataFile> entry : dataCollection.entrySet()) {
            ResourceMember member = entry.getKey();
            if (member.getResourceType().equalsIgnoreCase(type)) {
                typedCollection.put(member, entry.getValue());
            }
        }
        return typedCollection;
    }
    
    public Set<ResourceField> getJoinFieldIds(Set<ResourceMember> members) {
        List<ResourceField> allFields = new ArrayList<>();
        FieldCounter counter = new FieldCounter();
        for (ResourceMember member : members) {
            final List<ResourceField> fields = member.getResourceFields();
            counter.updateWith(fields);
            allFields.addAll(fields);
        }
        
        // XXX buggy as it contains fields of the same resource type
        // this might lead (for example) to join columns of similar 
        // structured resources (two observation tables containing
        // both the field MESS_DATUM
        
        Set<ResourceField> joinColumns = new LinkedHashSet<>();
        for (ResourceField field : allFields) {
            if (counter.isJoinColumn(field)) {
                joinColumns.add(field);
            }
        }
        return joinColumns;
    }
    
    private class FieldCounter {
        private final Map<ResourceField, FieldCount> counts = new HashMap<>();
        void updateWith(List<ResourceField> fields) {
            for (ResourceField field : fields) {
                if (counts.containsKey(field)) {
                    counts.get(field).count++;
                } else {
                    counts.put(field, new FieldCount());
                }
            }
        }
        boolean isJoinColumn(ResourceField field) {
            return counts.get(field).count > 1;
        }
    }
    
    private class FieldCount {
        private int count;
        public FieldCount() {
            count++;
        }
    }

}
