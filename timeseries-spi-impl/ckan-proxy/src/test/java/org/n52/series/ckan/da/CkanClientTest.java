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

import eu.trentorise.opendata.jackan.CkanClient;
import eu.trentorise.opendata.jackan.CkanQuery;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanResource;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CkanClientTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CkanClientTest.class);

    @Test
    @Ignore
    public void getAllDatasetNames() {
        LOGGER.info("#### all dataset names available");
        CkanClient client = new CkanClient("https://ckan.colabis.de/");
        List<String> datasetList = client.getDatasetList();
        for (String dataset : datasetList) {
            LOGGER.info("dataset {}", dataset);
        }
    }
    
    @Test
    @Ignore
    public void getAllDatasetsTaggedWithDWD() {
        LOGGER.info("#### first 10 datasets tagged with DWD");
        CkanClient client = new CkanClient("https://ckan.colabis.de/");
        CkanQuery query = CkanQuery.filter().byTagNames("DWD");
        List<CkanDataset> datasetList = client.searchDatasets(query, 10, 0).getResults();
        for (CkanDataset dataset : datasetList) {
            LOGGER.info("Dataset ##############################");
            LOGGER.info("id: {}", dataset.getId());
            LOGGER.info("name: {}", dataset.getName());
        }
    }

}
