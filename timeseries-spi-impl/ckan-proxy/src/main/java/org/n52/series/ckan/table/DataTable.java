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

public class DataTable {
    
    protected final HashBasedTable<ResourceKey,ResourceField,String> table;
    
    private final Map<JoinIndex, List<ResourceKey>> joinKeys;
    
    private final List<ResourceField> joinableFields;

    protected DataTable() {
        table = HashBasedTable.create();
        joinableFields = new ArrayList<>();
        joinKeys = new HashMap<>();
    }
    
    public Table getTable() {
        return ImmutableTable.copyOf(table);
    }
    
    public void leftJoin(DataTable other) {
        if ( !isJoinable(other)) {
            return;
        }
        
        List<ResourceMember> resourceMembers = new ArrayList<>();
        for (JoinIndex joinOn : joinKeys.keySet()) {
            List<ResourceKey> joinRowKeys = joinKeys.get(joinOn);
            Map<ResourceKey, Map<ResourceField, String>> otherRows = other.table.rowMap();
            for (Map.Entry<ResourceKey, Map<ResourceField, String>> otherRowEntry : otherRows.entrySet()) {
                String value = otherRowEntry.getValue().get(joinOn.getField());
                if (joinOn.equals(new JoinIndex(joinOn.getField(), value))) {
                    resourceMembers.addAll(otherRowEntry.getKey().getMembers());
                    joinRowKeys.add(otherRowEntry.getKey());
                }
            }
            
            ResourceKey key = joinRowKeys.get(0);
            
            
//            Map<ResourceField, String> leftRow = table.row(leftRowId);
                    
            final int matchCount = joinRowKeys.size();
            for (int i = 1 ; i < joinRowKeys.size() ; i++ ) {
                ResourceKey rightKey = joinRowKeys.get(i);
                key.addMembers(rightKey.getMembers());
            }
            if (matchCount == 2) {
                ResourceKey rightRowId = joinRowKeys.get(1);
                
            } else {
                // no match
            }
        }
            
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
            joinKeys.put(index, new ArrayList<ResourceKey>());
        }
        joinKeys.get(index).add(rowKey);
    }
    
}
