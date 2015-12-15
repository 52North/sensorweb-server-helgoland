package org.n52.series.ckan.table;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
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

    private final String resourceKey;
    
    public ResourceTable(ResourceMember resourceMember, DataFile dataFile) {
        this.resourceMember = resourceMember;
        this.resourceKey = "id_" + resourceMember.getId().substring(0, 6); // TODO
        this.dataFile = dataFile;
    }
    
    public void readIntoMemory(Set<String> fieldIdsToIndex) {
        final Path filePath = dataFile.getFile().toPath();
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
                final String id = resourceKey + "_" + i;
                for (int j = 0 ; j < values.length ; j++) {
                    final String value = values[j];
                    table.put(id, columnHeaders.get(j), value);
                    final ResourceField field = resourceMember.getField(j);
                    if (fieldIdsToIndex.contains(field.getFieldId())) {
                        JoinIndex index = new JoinIndex(field, value);
                        addValueToIndex(index, id);
                    }
                }
            }
            LOGGER.debug("Resource data '{}' loaded into memory (#{} lines a #{} columns)", 
                    resourceMember.getId(), allLines.size(), columnHeaders.size());
        } catch (IOException e) {
            LOGGER.error("could not read data from {}", filePath, e);
        }
    }
    
}
