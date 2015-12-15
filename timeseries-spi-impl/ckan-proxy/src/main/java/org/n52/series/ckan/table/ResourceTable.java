package org.n52.series.ckan.table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.n52.series.ckan.beans.DataFile;
import org.n52.series.ckan.beans.ResourceField;
import org.n52.series.ckan.beans.ResourceMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceTable extends DataTable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceTable.class);
    
    private final ResourceMember resourceMember;
    
    private final DataFile dataFile;

    public ResourceTable(ResourceMember resourceMember, DataFile dataFile) {
        this.resourceMember = resourceMember;
        this.dataFile = dataFile;
    }
    
    public void readIntoMemory() {
        readIntoMemory(null);
    }
    
    public void readIntoMemory(Set<String> fieldIdsToIndex) {
        final Path filePath = dataFile.getFile().toPath();
        fieldIdsToIndex = lowerCaseFieldIds(fieldIdsToIndex);
        try {
            List<String> allLines = Files.readAllLines(filePath, dataFile.getEncoding());
            List<String> columnHeaders = resourceMember.getColumnHeaders();
            for (int i = resourceMember.getHeaderRows() ; i < allLines.size() ; i ++) {
                String[] values = allLines.get(i).split(",");
                if (values.length != columnHeaders.size()) {
                    
                    // TODO choose csv parsing strategy
                    
                    LOGGER.warn("ignore line: #columnheaders != #csvValues");
                    LOGGER.debug("headers: {}", Arrays.toString(columnHeaders.toArray()));
                    LOGGER.debug("line: {}", allLines.get(i));
                    continue;
                }
                ResourceKey id = new ResourceKey("" + i, resourceMember);
                for (int j = 0 ; j < values.length ; j++) {
                    final ResourceField field = resourceMember.getField(j);
                    final String value = values[j];
                    table.put(id, field, value);
                    
                    if (fieldIdsToIndex.contains(field.getFieldId())) {
                        addJoinIndexValue(id, new JoinIndex(field, value));
                    }
                }
            }
            LOGGER.debug("Resource data '{}' loaded into memory (#{} lines a #{} columns)", 
                    resourceMember.getId(), allLines.size(), columnHeaders.size());
        } catch (IOException e) {
            LOGGER.error("could not read data from {}", filePath, e);
        }
    }

    private Set<String> lowerCaseFieldIds(Set<String> fieldIdsToIndex) {
        Set<String> lowercaseFieldIds = new HashSet<>();
        if (fieldIdsToIndex != null) {
            for (String fieldId : fieldIdsToIndex) {
                lowercaseFieldIds.add(fieldId.toLowerCase());
            }
        }
        return lowercaseFieldIds;
    }

}
