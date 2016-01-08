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
package org.n52.series.ckan.table;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.n52.series.ckan.beans.ResourceField;
import org.n52.series.ckan.beans.ResourceMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DataTable.class);
    
    protected final Table<ResourceKey,ResourceField,String> table;
    
    protected final ResourceMember resourceMember;
    
    private final List<ResourceMember> joinedMembers;

    protected DataTable(ResourceMember resourceMember) {
        this(HashBasedTable.create(), resourceMember);
    }
    
    private DataTable(DataTable table, ResourceMember resourceMember) {
        this(table.copy(), resourceMember);
    }
    
    private DataTable(Table table, ResourceMember resourceMember) {
        this.table = table;
        this.resourceMember = resourceMember;
        this.joinedMembers = new ArrayList<>();
    }
    
    private Table<ResourceKey,ResourceField,String> copy() {
        return HashBasedTable.create(table);
    }
    
    public Table<ResourceKey,ResourceField,String> getTable() {
        return table; // TODO making immutable costs performance, consider delegate with getters returning immutable collections?
    }
    
    public ResourceMember getResourceMember() {
        return resourceMember;
    }
    
    public DataTable innerJoin(DataTable other, ResourceField... fields) {
        DataTable outputTable = new DataTable(resourceMember);
        if ( !isJoinable(other)) {
            return outputTable;
        }
        
        outputTable.joinedMembers.add(other.resourceMember);
        Collection<ResourceField> joinFields = fields == null || fields.length == 0
                ? resourceMember.getJoinFields(other.resourceMember)
                : Arrays.asList(fields);
        
        joinTable(other, outputTable, joinFields);
        return outputTable;
    }

    private void joinTable(DataTable other, DataTable outputTable, Collection<ResourceField> joinFields) {
        if (joinFields == null || joinFields.isEmpty()) {
            return;
        }
        
        for (ResourceField field : joinFields) {
            if ( !table.containsColumn(field)
                    || !other.table.containsColumn(field)) {
                return;
            }
        }
        
        LOGGER.debug("joining table {} (#{} rows) with table {} (#{} rows)", 
                resourceMember.getId(), table.rowKeySet().size(), 
                other.resourceMember.getId(), other.table.rowKeySet().size());
        long start = System.currentTimeMillis();
        for (ResourceField field : joinFields) {
            final Map<ResourceKey, String> joinOnIndex = table.column(field);
            int i = 0;
            for (Map.Entry<ResourceKey, String> joinOnIndexEntry : joinOnIndex.entrySet()) {
                Map<ResourceKey, String> toJoinIndex = other.table.column(field);
                for (Map.Entry<ResourceKey, String> toJoinIndexEntry : toJoinIndex.entrySet()) {
                    if ( !joinOnIndexEntry.getValue().equalsIgnoreCase(toJoinIndexEntry.getValue())) {
                        continue;
                    } 
                    final ResourceKey otherKey = toJoinIndexEntry.getKey();
                    final String newId = toJoinIndexEntry.getKey().getKeyId() + "_" + i++;
                    ResourceKey newKey = new ResourceKey(newId, outputTable.resourceMember);

                    // add other's values
                    final Map<ResourceField, String> toJoinRow = other.table.row(otherKey);
                    for (Map.Entry<ResourceField, String> otherValue : toJoinRow.entrySet()) {
                        final ResourceField rightField = otherValue.getKey();
                        ResourceField joinedField = ResourceField.copy(rightField);
                        joinedField.setQualifier(otherKey.getMember());
                        outputTable.table.put(newKey, joinedField, otherValue.getValue());
                    }

                    // add this instance's values
                    Map<ResourceField, String> joinOnRow = table.row(joinOnIndexEntry.getKey());
                    for (Map.Entry<ResourceField, String> value : joinOnRow.entrySet()) {
                        outputTable.table.put(newKey, value.getKey(), value.getValue());
                    }
                }
            }
        }
        LOGGER.debug("joined table has #{} rows, took {}s", 
                outputTable.table.rowKeySet().size(),
                (System.currentTimeMillis() - start) / 1000d);
    }

    /**
     * Indicates if this instance is joinable with the passed table instance.
     * Two indicators are used here:<br/>
     * <ul>
     * <li>Both {@link DataTable#resourceMember} are <b>not</b> of the 
     * same type</li>
     * <li>both share at least one {@link ResourceField}</li>
     * </ul>
     * 
     *
     * @param other the other table.
     * @return if this instance is joinable with the passed table instance.
     */
    public boolean isJoinable(DataTable other) {
        ResourceMember otherResourceMember = other.resourceMember;
        String otherResourceType = otherResourceMember.getResourceType();
        if (otherResourceType.equalsIgnoreCase(resourceMember.getResourceType())) {
            LOGGER.debug("Same resourceTypes are not joinable.");
            return false;
        }
        
        Set<ResourceField> joinableFields = resourceMember.getJoinableFields(otherResourceMember);
        for (ResourceField possibleJoinColumn : other.table.columnKeySet()) {
            if (joinableFields.contains(possibleJoinColumn)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append("DataTable(")
                .append("size=")
                .append(table.rowKeySet().size())
                .append(" rows, ")
                .append("resource=")
                .append(resourceMember)
                .append(". Joined resources: [")
                .append(Arrays.toString(joinedMembers.toArray()))
                .append(" ])")
                .toString();
    }
    
    protected void logMemory() {
        LOGGER.trace("Max Memory: {} Mb", Runtime.getRuntime().maxMemory() / 1048576);
        LOGGER.trace("Total Memory: {} Mb", Runtime.getRuntime().totalMemory() / 1048576);
        LOGGER.trace("Free Memory: {} Mb", Runtime.getRuntime().freeMemory() / 1048576);
    }

}
