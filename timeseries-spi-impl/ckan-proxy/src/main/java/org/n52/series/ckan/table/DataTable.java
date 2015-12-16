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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.n52.series.ckan.beans.ResourceField;
import org.n52.series.ckan.beans.ResourceMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataTable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DataTable.class);
    
    protected final Table<ResourceKey,ResourceField,String> table;
    
    private final Map<JoinIndex, ResourceKey> joinKeys;
    
    private final List<ResourceField> joinableFields;

    protected DataTable() {
        this(HashBasedTable.create());
    }
    
    private DataTable(Table table) {
        this.table = table;
        this.joinKeys = new HashMap<>();
        this.joinableFields = new ArrayList<>();
    }
    
    private DataTable(DataTable table) {
        this(table.getTable());
        joinKeys.putAll(table.joinKeys);
        joinableFields.addAll(table.joinableFields);
    }
    
    public Table getTable() {
        return HashBasedTable.create(table);
    }
    
    public DataTable leftJoin(DataTable other) {
        if ( !isJoinable(other)) {
            return this;
        }
        
        DataTable joinTable = new DataTable(this);
        for (JoinIndex joinOn : joinKeys.keySet()) {
            Map<ResourceField, Map<ResourceKey, String>> columnMap = other.table.columnMap();
            for (Map.Entry<ResourceField, Map<ResourceKey, String>> otherColumnEntry : columnMap.entrySet()) {
                if (otherColumnEntry.getKey().equals(joinOn.getField())) {
                    for (Map.Entry<ResourceKey, String> possibleJoinValue : otherColumnEntry.getValue().entrySet()) {
                        if (joinOn.getValue().equalsIgnoreCase(possibleJoinValue.getValue())) {
                            
                            // TODO new resource key
                            
                            final ResourceKey rightKey = possibleJoinValue.getKey();
                            LOGGER.trace("join key {}", joinOn);
                            for (Map.Entry<ResourceField, String> rightValue : other.table.row(rightKey).entrySet()) {
                                // XXX user new resource key here
                                joinTable.table.put(rightKey, rightValue.getKey(), rightValue.getValue());
                            }
                        }
                    }
                }
            }
//            Map<ResourceKey, Map<ResourceField, String>> otherRows = other.table.rowMap();
//            for (Map.Entry<ResourceKey, Map<ResourceField, String>> otherRowEntry : otherRows.entrySet()) {
//                String value = otherRowEntry.getValue().get(joinOn.getField());
//                if (joinOn.equals(new JoinIndex(joinOn.getField(), value))) {
//                    LOGGER.trace("join key {}", joinOn);
//                    final ResourceKey rightKey = otherRowEntry.getKey();
//                    Map<ResourceField, String> rightValues = otherRowEntry.getValue();
//                    for (Map.Entry<ResourceField, String> rightValue : rightValues.entrySet()) {
//                        joinTable.table.put(rightKey, rightValue.getKey(), rightValue.getValue());
//                    }
//                    
//                    // TODO update JoinIndex
//                }
//            }
        }
        return joinTable;
    }

    private boolean isJoinable(DataTable other) {
        // TODO multiple keys to join
        for (ResourceField possibleJoinColumn : other.table.columnKeySet()) {
            if (joinableFields.contains(possibleJoinColumn)) {
                return true;
            }
        }
        return false;
    }
    
    protected void addJoinIndexValue(ResourceKey rowKey, JoinIndex index) {
        if ( !joinKeys.containsKey(index)) {
            joinableFields.add(index.getField());
        }
        joinKeys.put(index, rowKey);
    }
    
    protected void logMemory() {
        LOGGER.debug("Max Memory: {} Mb", Runtime.getRuntime().maxMemory() / 1048576);
        LOGGER.debug("Total Memory: {} Mb", Runtime.getRuntime().totalMemory() / 1048576);
        LOGGER.debug("Free Memory: {} Mb", Runtime.getRuntime().freeMemory() / 1048576);
    }

}
