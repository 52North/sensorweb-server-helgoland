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
import java.util.ArrayList;
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

    public SchemaDescriptor(JsonNode node) {
        this.node = node;
    }

    public String getVersion() {
        return JsonUtil.parseMissingToEmptyString(node, CkanConstants.SCHEMA_DESCRIPTOR_VERSION);
    }

    public String getDescription() {
        return JsonUtil.parseMissingToEmptyString(node, CkanConstants.SCHEMA_DESCRIPTOR_DESCRIPTION);
    }
    
    public JsonNode getNode() {
        return node;
    }
    
    public String getSchemaDescriptionType() {
        return JsonUtil.parseMissingToEmptyString(node, CkanConstants.MEMBER_RESOURCE_TYPE);
    }

    public boolean hasDescription() {
        return !node.isMissingNode();
    }
    
    public Map<ResourceMember, DataFile> relateWithDataFiles(Map<String, DataFile> csvContents) {
        Map<ResourceMember, DataFile> members = new HashMap<>();
        final JsonNode membersNode = node.findValue("members");
        final Iterator<JsonNode> iter = membersNode.elements();
        while (iter.hasNext()) {
            JsonNode memberNode = iter.next();
            for (String id : JsonUtil.parseMissingToEmptyArray(memberNode, CkanConstants.MEMBER_RESOURCE_NAME, CkanConstants.MEMBER_RESOURCEID)) {
                ResourceMember member = new ResourceMember();
                member.setId(id); // TODO missing ids will cause conflicts/inconsistencies
                member.setResourceType(parseMissingToEmptyString(memberNode, CkanConstants.MEMBER_RESOURCE_TYPE, CkanConstants.MEMBER_RESOURCETYPE));
                final int headerRows = parseMissingToNegativeInt(memberNode, CkanConstants.MEMBER_HEADER_ROWS);
                member.setHeaderRows(headerRows < 0 ? 1 : headerRows); // assume 1 header row by default
                member.setResourceFields(parseResourceFields(memberNode));
                members.put(member, csvContents.get(member.getId()));
            }
        }
        return members;
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
