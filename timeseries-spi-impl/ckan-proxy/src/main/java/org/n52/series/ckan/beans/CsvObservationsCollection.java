package org.n52.series.ckan.beans;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.n52.series.ckan.da.CkanConstants;
import static org.n52.series.ckan.util.JsonUtil.parseMissingToEmptyString;
import static org.n52.series.ckan.util.JsonUtil.parseMissingToNegativeInt;

public class CsvObservationsCollection {
    
    private final String datasetId;
    
    private final String description;
    
    private final Map<ResourceMember, DataFile> dataCollection;
    
    public CsvObservationsCollection(String datasetId, DescriptionFile description, Map<String, DataFile> csvContents) {
        this.datasetId = datasetId;
        JsonNode node = description.getNode();
        this.description = node.at("/description").asText();
        this.dataCollection = parseResourceMembers(node, csvContents);
    }

    private Map<ResourceMember, DataFile> parseResourceMembers(JsonNode resourceDescription, Map<String, DataFile> csvContents) {
        Map<ResourceMember, DataFile> members = new HashMap<>();
        final JsonNode membersNode = resourceDescription.findValue("members");
        final Iterator<JsonNode> iter = membersNode.elements();
        while (iter.hasNext()) {
            JsonNode node = iter.next();
            ResourceMember member = new ResourceMember();
            // TODO missing ids will cause conflicts/inconsistencies
            member.setId(parseMissingToEmptyString(node, CkanConstants.MEMBER_RESOURCE_NAME, CkanConstants.MEMBER_RESOURCEID));
            member.setResourceType(parseMissingToEmptyString(node, CkanConstants.MEMBER_RESOURCE_TYPE, CkanConstants.MEMBER_RESOURCETYPE));
            final int headerRows = parseMissingToNegativeInt(node, CkanConstants.MEMBER_HEADER_ROWS);
            member.setHeaderRows(headerRows < 0 ? 1 : headerRows); // assume 1 header row by default
            member.setResourceFields(parseResourceFields(node));
            members.put(member, csvContents.get(member.getId()));
        }
        return members;
    }
    
    private List<ResourceField> parseResourceFields(JsonNode member) {
        List<ResourceField> fields = new ArrayList<>();
        JsonNode fieldsNode = member.findValue("fields");
        Iterator<JsonNode> iter = fieldsNode.elements();
        while (iter.hasNext()) {
            JsonNode node = iter.next();
            fields.add(new ResourceField(node));
        }
        return fields;
    }
    
    public String getDatasetId() {
        return datasetId;
    }

    public String getDescription() {
        return description;
    }

    public Map<ResourceMember, DataFile> getDataCollection() {
        return Collections.unmodifiableMap(dataCollection);
    }
    
    public Map<ResourceMember, DataFile> getMetadataCollection() {
        Map<ResourceMember, DataFile> typedCollection = new HashMap<>();
        for (Map.Entry<ResourceMember, DataFile> entry : dataCollection.entrySet()) {
            ResourceMember member = entry.getKey();
            final String resourceType = member.getResourceType();
            if ( !resourceType.equalsIgnoreCase(CkanConstants.RESOURCE_TYPE_OBSERVATIONS)) {
                typedCollection.put(member, entry.getValue());
            }
        }
        return typedCollection;
    }
    
    public Map<ResourceMember, DataFile> getObservationDataCollection() {
        return getDataCollectionsOfType(CkanConstants.RESOURCE_TYPE_OBSERVATIONS);
    }
    
    public Map<ResourceMember, DataFile> getPlatformDataCollection() {
        return getDataCollectionsOfType(CkanConstants.RESOURCE_TYPE_PLATFORMS);
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
    
    public Set<String> getJoinFieldIds(Set<ResourceMember> members) {
        List<ResourceField> allFields = new ArrayList<>();
        FieldCounter counter = new FieldCounter();
        for (ResourceMember member : members) {
            final List<ResourceField> fields = member.getResourceFields();
            counter.updateWith(fields);
            allFields.addAll(fields);
        }
        
        Set<String> joinColumns = new LinkedHashSet<>();
        for (ResourceField field : allFields) {
            if (counter.isJoinColumn(field)) {
                joinColumns.add(field.getFieldId());
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
