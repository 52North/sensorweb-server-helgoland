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
    
    private final HashBasedTable<String,String,String> table;
    
    private final CsvObservationsCollection observationCollection;

    public static HashBasedTable<String,String,String> merge(ObservationTable table) {
        this.table = HashBasedTable.create();
    }
    
    private void joinResourceTables(CsvObservationsCollection observationCollection) {
        Map<ResourceMember, DataFile> dataCollection = observationCollection.getDataCollection();
        Set<String> joinFields = getJoinFields(dataCollection.keySet());
        for (Map.Entry<ResourceMember, DataFile> entry : dataCollection.entrySet()) {
            
        }
    }

    private Set<String> getJoinFields(Set<ResourceMember> members) {
        List<String> allColumns = new ArrayList<>();
        ColumnCounter counter = new ColumnCounter();
        for (ResourceMember member : members) {
            final List<String> columnHeaders = member.getColumnHeaders();
            counter.updateWith(columnHeaders);
            allColumns.addAll(columnHeaders);
        }
        
        Set<String> joinColumns = new LinkedHashSet<>();
        for (String column : allColumns) {
            if (counter.isJoinColumn(column)) {
                joinColumns.add(column);
            }
        }
        return joinColumns;
    }
    
    private class ColumnCounter {
        private final Map<String, ColumnCount> counts = new HashMap<>();
        void updateWith(List<String> columns) {
            for (String column : columns) {
                if (counts.containsKey(column)) {
                    counts.get(column).count++;
                } else {
                    counts.put(column, new ColumnCount());
                }
            }
        }
        boolean isJoinColumn(String column) {
            return counts.get(column).count > 1;
        }
    }
    
    private class ColumnCount {
        private int count;
        public ColumnCount() {
            count++;
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
