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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
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
            LOGGER.debug("loading table ...");
            long start = System.currentTimeMillis();
            CSVParser csvParser = CSVParser.parse(filePath.toFile(), dataFile.getEncoding(), CSVFormat.DEFAULT);
            Iterator<CSVRecord> iterator = csvParser.iterator();
            List<String> columnHeaders = resourceMember.getColumnHeaders();
            for (int i = 0 ; i < resourceMember.getHeaderRows() ; i++) {
                iterator.next(); // skip
            }
            int lineNbr = 0;
            while (iterator.hasNext()) {
                CSVRecord line = iterator.next();
                if (line.size() != columnHeaders.size()) {
                    
                    // TODO choose csv parsing strategy
                    
                    LOGGER.warn("ignore line: #columnheaders != #csvValues");
                    LOGGER.debug("headers: {}", Arrays.toString(columnHeaders.toArray()));
                    LOGGER.debug("line: {}", line);
                    continue;
                }
                ResourceKey id = new ResourceKey("" + lineNbr++, resourceMember);
                for (int j = 0 ; j < line.size() ; j++) {
                    final ResourceField field = resourceMember.getField(j);
                    final String value = line.get(j);
                    table.put(id, field, value);
                    
                    if (fieldIdsToIndex.contains(field.getFieldId())) {
                        addJoinIndexValue(id, new JoinIndex(field, value));
                    }
                }
            }
            LOGGER.debug("Resource data '{}' loaded into memory (#{} lines a #{} columns)", 
                    resourceMember.getId(), lineNbr, columnHeaders.size());
            LOGGER.debug("Loading took {}s", (System.currentTimeMillis() - start)/1000);
            logMemory();
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
