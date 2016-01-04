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
package org.n52.series.ckan.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.n52.series.ckan.beans.DataFile;
import org.n52.series.ckan.da.CkanHarvestingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBasedCkanHarvester extends CkanHarvestingService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedCkanHarvester.class);
    
    private static final String TEST_FILES_BASE_PATH = "/files";
    
    private final ObjectMapper om = new ObjectMapper();
    
    private final String contextPath;
    
    {
        CkanClient.configureObjectMapper(om);
    }

    public FileBasedCkanHarvester(String contextPath) {
        this.contextPath = contextPath;
    }
    
    @Override
    public void harvestDatasets() {
        for (File file : getDatasets()) {
            CkanDataset dataset = parseDatasetTestFile(file);
            getMetadataCache().insertOrUpdate(dataset);
        }
    }

    private List<File> getDatasets() {
        List<File> datasets = new ArrayList<>();
        File folder = getSourceDataFolder();
        File[] datasetFiles = folder.listFiles();
        for (File file : datasetFiles) {
            if (file.isDirectory()) {
                Path datasetPath = file.toPath().resolve("dataset.json");
                datasets.add(datasetPath.toFile());
            }
        } 
        return datasets;
    }

    private File getSourceDataFolder() {
        String baseFolder = TEST_FILES_BASE_PATH + "/" + contextPath;
        LOGGER.debug("Source Data Folder: {}", baseFolder);
        return new File(getClass().getResource(baseFolder).getFile());
    }

    private CkanDataset parseDatasetTestFile(File file) {
        try {
            return om.readValue(file, CkanDataset.class);
        } catch (IOException e) {
            LOGGER.error("could not read/parse test file", e);
            return new CkanDataset();
        }
    }

    @Override
    protected DataFile downloadCsvFile(CkanResource resource, Path datasetDownloadFolder) {
        File folder = getSourceDataFolder();
        File[] dataFolders = folder.listFiles();
        for (File file : dataFolders) {
            if (file.isDirectory()) {
                Path datapath = file.toPath().resolve(resource.getId() + ".csv");
                if (datapath.toFile().exists()) {
                    return new DataFile(resource, datapath.toFile());
                }
            }
        } 
        return new DataFile(resource, null);
    }
    
    
}
