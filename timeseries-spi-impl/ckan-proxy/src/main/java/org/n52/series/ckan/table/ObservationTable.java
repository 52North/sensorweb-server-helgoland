package org.n52.series.ckan.table;

import com.google.common.collect.HashBasedTable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.n52.series.ckan.beans.DataFile;
import org.n52.series.ckan.beans.ResourceMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObservationTable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ObservationTable.class);
    
    private final HashBasedTable<String,String,String> table;
    
    private final ResourceMember resourceMember;
    
    private final DataFile dataFile;

    private final String resourceKey;
    
    public ObservationTable(ResourceMember resourceMember, DataFile dataFile) {
        this.table = HashBasedTable.create();
        this.resourceMember = resourceMember;
        this.resourceKey = "id_" + resourceMember.getId().substring(0, 4);
        this.dataFile = dataFile;
    }
    
    public void readIntoMemory() {
        final Path filePath = dataFile.getFile().toPath();
        try {
            List<String> allLines = Files.readAllLines(filePath, dataFile.getEncoding());
            List<String> columnHeaders = resourceMember.getColumnHeaders();
            for (int i = resourceMember.getHeaderRows() ; i < allLines.size() ; i ++) {
                String[] values = allLines.get(i).split(",");
                if (values.length != columnHeaders.size()) {
                    LOGGER.warn("ignore line: #columnheaders != #csvValues");
                    LOGGER.debug("headers: {}", Arrays.toString(columnHeaders.toArray()));
                    LOGGER.debug("line: {}", allLines.get(i));
                    continue;
                }
                final String id = resourceKey + "_" + i;
                for (int j = 0 ; j < values.length ; j++) {
                    table.put(id, columnHeaders.get(j), values[j]);
                }
            }
            LOGGER.debug("Resource data '{}' loaded into memory (#{} lines a #{} columns)", 
                    resourceMember.getId(), allLines.size(), columnHeaders.size());
        } catch (IOException e) {
            LOGGER.error("could not read data from {}", filePath, e);
        }
    }
    
    
}
