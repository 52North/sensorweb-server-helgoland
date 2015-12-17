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
import eu.trentorise.opendata.jackan.model.CkanDataset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.n52.series.ckan.da.CkanConstants;
import org.n52.series.ckan.util.JsonUtil;
import static org.n52.series.ckan.util.JsonUtil.parseMissingToEmptyString;
import static org.n52.series.ckan.util.JsonUtil.parseMissingToNegativeInt;

public class SchemaDescriptor {
    
    private final JsonNode node;
    
    private final CkanDataset dataset;
    
    private final List<ResourceMember> members;

    public SchemaDescriptor(CkanDataset dataset, JsonNode node) {
        this.node = node;
        this.dataset = dataset;
        members = parseMemberDescriptions();
    }

    public String getVersion() {
        return JsonUtil.parseMissingToEmptyString(node, CkanConstants.SchemaDescriptor.VERSION);
    }

    public String getDescription() {
        return JsonUtil.parseMissingToEmptyString(node, CkanConstants.SchemaDescriptor.DESCRIPTION);
    }
    
    public JsonNode getNode() {
        return node;
    }

    public CkanDataset getDataset() {
        return dataset;
    }
    
    public List<ResourceMember> getMembers() {
        return Collections.unmodifiableList(members);
    }
    
    public String getSchemaDescriptionType() {
        return JsonUtil.parseMissingToEmptyString(node, CkanConstants.SchemaDescriptor.RESOURCE_TYPE);
    }

    public boolean hasDescription() {
        return !node.isMissingNode();
    }
    
    public Map<ResourceMember, DataFile> relateWithDataFiles(Map<String, DataFile> csvContents) {
        Map<ResourceMember, DataFile> memberRelations = new HashMap<>();
        for (ResourceMember member : members) {
            memberRelations.put(member, csvContents.get(member.getId()));
        }
        return memberRelations;
    }
    
    private List<ResourceMember> parseMemberDescriptions() {
        List<ResourceMember> resourceMembers = new ArrayList<>();
        final JsonNode membersNode = node.findValue("members");
        final Iterator<JsonNode> iter = membersNode.elements();
        while (iter.hasNext()) {
            JsonNode memberNode = iter.next();
            for (String id : JsonUtil.parseMissingToEmptyArray(memberNode, CkanConstants.MemberProperty.RESOURCE_NAME, CkanConstants.MemberProperty.RESOURCEID)) {
                ResourceMember member = new ResourceMember();
                member.setId(id); // TODO missing ids will cause conflicts/inconsistencies
                member.setResourceType(parseMissingToEmptyString(memberNode, CkanConstants.MemberProperty.RESOURCE_TYPE, CkanConstants.MemberProperty.RESOURCETYPE));
                final int headerRows = parseMissingToNegativeInt(memberNode, CkanConstants.MemberProperty.HEADER_ROWS);
                member.setHeaderRows(headerRows < 0 ? 1 : headerRows); // assume 1 header row by default
                member.setResourceFields(parseResourceFields(memberNode));
                resourceMembers.add(member);
            }
        }
        return resourceMembers;
    }
            
    private List<ResourceField> parseResourceFields(JsonNode member) {
        List<ResourceField> fields = new ArrayList<>();
        JsonNode fieldsNode = member.findValue("fields");
        Iterator<JsonNode> iter = fieldsNode.elements();
        while (iter.hasNext()) {
            JsonNode fieldNode = iter.next();
            fields.add(new ResourceField(fieldNode));
        }
        return fields;
    }
    

}
