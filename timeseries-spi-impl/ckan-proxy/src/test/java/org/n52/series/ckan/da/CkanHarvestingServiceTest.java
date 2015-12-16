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
package org.n52.series.ckan.da;

import org.n52.series.ckan.cache.InMemoryCkanMetadataCache;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.CkanQuery;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.n52.series.ckan.beans.CsvObservationsCollection;
import org.n52.series.ckan.beans.DataFile;
import org.n52.series.ckan.beans.ResourceMember;
import org.n52.series.ckan.table.ResourceTable;
import org.n52.series.ckan.cache.InMemoryCkanDataCache;
import org.n52.series.ckan.cache.InMemoryCkanDataCache.Entry;
import org.n52.series.ckan.util.ResourceClient;

//@Ignore
public class CkanHarvestingServiceTest {
    
    private final ObjectMapper om = new ObjectMapper();
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    private InMemoryCkanMetadataCache ckanMetadataCache;
    
    private InMemoryCkanDataCache ckanDataCache;
    
    private CkanHarvestingService ckanHarvester;
    
    @Before
    public void setUp() throws URISyntaxException {
        ckanMetadataCache = new InMemoryCkanMetadataCache();
        ckanDataCache = new InMemoryCkanDataCache();
//        ckanHarvester = new CkanHarvestingService();
        ckanHarvester = new SeamCkanHarvester();
        
        ckanHarvester.setResourceClient(new ResourceClient()); // TODO load locally from seam
        ckanHarvester.setCkanClient(new CkanClient("https://ckan.colabis.de")); // TODO load locally from seam
        ckanHarvester.setResourceDownloadBaseFolder(testFolder.getRoot().toURI().toString());
        ckanHarvester.setMetadataCache(ckanMetadataCache);
        ckanHarvester.setDataCache(ckanDataCache);
        
        MatcherAssert.assertThat(ckanMetadataCache.size(), CoreMatchers.is(0));
//        CkanQuery query = CkanQuery.filter().byTagNames("DWD");
        ckanHarvester.harvestDatasets();
    }
    
    @Test
    public void harvestDatasets() {
        MatcherAssert.assertThat(ckanMetadataCache.size(), CoreMatchers.is(4));
    }
    
    @Test
    public void harvestResources() throws IOException {
        MatcherAssert.assertThat(testFolder.getRoot().list().length, CoreMatchers.is(0));
        ckanHarvester.harvestResources();
        
        Entry<CkanDataset, CsvObservationsCollection> entry = ckanDataCache.getCollections().iterator().next();
        Map<ResourceMember, DataFile> metadataCollection = entry.getData().getMetadataCollection();
        Map.Entry<ResourceMember, DataFile> data = metadataCollection.entrySet().iterator().next();
        ResourceTable metadataTable = new ResourceTable(data.getKey(), data.getValue());
        metadataTable.readIntoMemory(Collections.singleton("stations_id"));
        
        Map<ResourceMember, DataFile> observations = entry.getData().getObservationDataCollection();
        for (Map.Entry<ResourceMember, DataFile> observationData : observations.entrySet()) {
            ResourceTable dataTable = new ResourceTable(observationData.getKey(), observationData.getValue());
            dataTable.readIntoMemory(null);
            metadataTable.leftJoin(dataTable);
        }
        
//        for (Entry<CkanDataset, CsvObservationsCollection> entry : ckanDataCache.getCollections()) {
//            TableMerger tableMerger = new TableMerger(entry.getData());
//            System.out.println("#### DATASET: " + entry.getDataset().getId());
//            tableMerger.printEachMemberIndividually(entry.getData());
//        }
        
        // TODO
    }
    
}
