package org.n52.series.ckan.beans;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import org.n52.series.ckan.da.CkanConstants;
import static org.n52.series.ckan.util.JsonUtil.parseMissingToEmptyString;
import static org.n52.series.ckan.util.JsonUtil.parseMissingToNegativeInt;

public class CsvObservationsCollection {
    
    private final String datasetId;
    
    private final List<DataFile> csvContents;
    
    private final String resourceDescription;
    
    private final List<ResourceMember> resourceMembers;
    
    public CsvObservationsCollection(String datasetId, DescriptionFile description, List<DataFile> csvContents) {
        this.datasetId = datasetId;
        JsonNode node = description.getNode();
        this.resourceDescription = node.at("/description").asText();
        this.resourceMembers = parseResourceMembers(node);
        this.csvContents = csvContents;
    }

    private List<ResourceMember> parseResourceMembers(JsonNode resourceDescription) {
        List<ResourceMember> members = new ArrayList<>();
        for (JsonNode node : resourceDescription.findValues("/members")) {
            ResourceMember member = new ResourceMember();
            member.setId(parseMissingToEmptyString(CkanConstants.MEMBER_RESOURCE_ID, node));
            member.setResourceType(parseMissingToEmptyString(CkanConstants.MEMBER_RESOURCE_TYPE, node));
            member.setHeaderRows(parseMissingToNegativeInt(CkanConstants.MEMBER_HEADER_ROWS, node));
            member.setResourceFields(parseResourceFields(node));
        }
        return members;
    }
    
    private List<ResourceField> parseResourceFields(JsonNode member) {
        List<ResourceField> fields = new ArrayList<>();
        for (JsonNode node : member.findValues("/fields")) {
            fields.add(new ResourceField(node));
        }
        return fields;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public List<DataFile> getCsvContents() {
        return csvContents;
    }

    public String getResourceDescription() {
        return resourceDescription;
    }

    public List<ResourceMember> getResourceMembers() {
        return resourceMembers;
    }
    
    
}
