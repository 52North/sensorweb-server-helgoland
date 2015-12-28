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

import org.n52.series.ckan.cache.InMemoryCkanMetadataCache;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;
import org.n52.series.ckan.beans.CsvObservationsCollection;
import org.n52.series.ckan.beans.DataFile;
import org.n52.series.ckan.beans.ResourceField;
import org.n52.series.ckan.beans.ResourceMember;
import org.n52.series.ckan.table.ResourceTable;
import org.n52.series.ckan.cache.InMemoryCkanDataCache;
import org.n52.series.ckan.cache.InMemoryCkanDataCache.Entry;
import org.n52.series.ckan.da.CkanHarvestingService;

public class FileBasedCkanHarvestingService {
    
    private final InMemoryCkanMetadataCache ckanMetadataCache;
    
    private final InMemoryCkanDataCache ckanDataCache;
    
    private final CkanHarvestingService ckanHarvester;
    
    public FileBasedCkanHarvestingService(File folder) throws URISyntaxException, IOException {
        ckanMetadataCache = new InMemoryCkanMetadataCache();
        ckanDataCache = new InMemoryCkanDataCache();
        
        ckanHarvester = new SeamCkanHarvester("dwd");
        ckanHarvester.setResourceDownloadBaseFolder(folder.toURI().toString());
        ckanHarvester.setMetadataCache(ckanMetadataCache);
        ckanHarvester.setDataCache(ckanDataCache);
        
        MatcherAssert.assertThat(ckanMetadataCache.size(), CoreMatchers.is(0));
        ckanHarvester.harvestDatasets();
        MatcherAssert.assertThat(folder.list().length, CoreMatchers.is(0));
        ckanHarvester.harvestResources();
        Assert.assertTrue(ckanMetadataCache.size() > 0);
    }

    public InMemoryCkanMetadataCache getCkanMetadataCache() {
        return ckanMetadataCache;
    }

    public InMemoryCkanDataCache getCkanDataCache() {
        return ckanDataCache;
    }
    
//    @Test
    public void testtablejoins() throws IOException {
        
        Entry<CkanDataset, CsvObservationsCollection> entry = ckanDataCache.getCollections().iterator().next();
        Map<ResourceMember, DataFile> metadataCollection = entry.getData().getMetadataCollection();
        Map.Entry<ResourceMember, DataFile> data = metadataCollection.entrySet().iterator().next();
        ResourceTable metadataTable = new ResourceTable(data.getKey(), data.getValue());
        metadataTable.readIntoMemory();
        
        Map<ResourceMember, DataFile> observations = entry.getData().getObservationDataCollections();
        for (Map.Entry<ResourceMember, DataFile> observationData : observations.entrySet()) {
            ResourceTable dataTable = new ResourceTable(observationData.getKey(), observationData.getValue());
            dataTable.readIntoMemory();
            metadataTable.innerJoin(dataTable);
        }
        
//        for (Entry<CkanDataset, CsvObservationsCollection> entry : ckanDataCache.getCollections()) {
//            TableMerger tableMerger = new TableMerger(entry.getData());
//            System.out.println("#### DATASET: " + entry.getDataset().getId());
//            tableMerger.printEachMemberIndividually(entry.getData());
//        }
        
        // TODO
    }
    
    private static class SeamResourceField extends ResourceField {

        public SeamResourceField(String fieldId) {
            super(fieldId);
        }
        
    }
}
