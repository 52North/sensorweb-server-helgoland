package org.n52.series.ckan.beans;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
            member.setId(parseMissingToEmptyString(CkanConstants.MEMBER_RESOURCE_ID, node));
            member.setResourceType(parseMissingToEmptyString(CkanConstants.MEMBER_RESOURCE_TYPE, node));
            member.setHeaderRows(parseMissingToNegativeInt(CkanConstants.MEMBER_HEADER_ROWS, node));
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
    
    public Map<ResourceMember, DataFile> getDataCollectionOfType(String type) {
        Map<ResourceMember, DataFile> typedCollection = new HashMap<>();
        for (Map.Entry<ResourceMember, DataFile> entry : dataCollection.entrySet()) {
            ResourceMember member = entry.getKey();
            if (member.getResourceType().toLowerCase().equals(type.toLowerCase())) {
                typedCollection.put(member, entry.getValue());
            }
        }
        return typedCollection;
    }

}
