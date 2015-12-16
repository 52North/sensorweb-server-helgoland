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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.n52.series.ckan.beans.CsvObservationsCollection;
import org.n52.series.ckan.beans.DataFile;
import org.n52.series.ckan.beans.ResourceMember;


class TableMerger {
    
    private HashBasedTable<String,String,String> table;
    
    private CsvObservationsCollection observationCollection;

    public HashBasedTable<String,String,String> merge(ResourceTable table) {
        this.table = HashBasedTable.create();
        
        return this.table;
    }
    
    private void joinResourceTables(CsvObservationsCollection observationCollection) {
        Map<ResourceMember, DataFile> dataCollection = observationCollection.getDataCollection();
        Set<String> joinFields = observationCollection.getJoinFieldIds(dataCollection.keySet());
        for (Map.Entry<ResourceMember, DataFile> entry : dataCollection.entrySet()) {
            
        }
    }
    
    // TODO
    public void printEachMemberIndividually(CsvObservationsCollection collection) {
        StringBuilder horizontalLine = new StringBuilder("+-");
        StringBuilder headerLine = new StringBuilder();
        StringBuilder format = new StringBuilder("| ");
        for (Map.Entry<ResourceMember, DataFile> entry : collection.getDataCollection().entrySet()) {
            ResourceMember member = entry.getKey();
            List<String> columnsHeaders = member.getColumnHeaders();
            int[] colunmLengths = new int[columnsHeaders.size()];
            for (int i = 0 ; i < colunmLengths.length; i++) {
                final String header = columnsHeaders.get(i);
                final int length = header.length();
                int width = Math.round(length + 0.2f * length);
                final String columnFormat = "%-" + width + "s | ";
                format.append(columnFormat);
                
                for (int j = 0 ; j < width ; j++) {
                    horizontalLine.append("-");
                }
                horizontalLine.append("-+");
                
            }
            
            format.append(" |%n");
            horizontalLine.append("-+%n");
            String line = horizontalLine.toString();
            headerLine
                    .append(line)
                    .append(String.format(format.toString(), columnsHeaders.toArray()))
                    .append(line);
            System.out.format(headerLine.toString());
        }
        
    }
}
