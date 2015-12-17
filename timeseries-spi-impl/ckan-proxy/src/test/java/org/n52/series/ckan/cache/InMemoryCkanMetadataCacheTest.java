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
package org.n52.series.ckan.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.n52.series.ckan.cache.InMemoryCkanMetadataCache;
import eu.trentorise.opendata.jackan.model.CkanDataset;
import eu.trentorise.opendata.jackan.model.CkanPair;
import eu.trentorise.opendata.jackan.model.CkanResource;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.n52.series.ckan.beans.SchemaDescriptor;
import org.n52.series.ckan.da.CkanConstants;

public class InMemoryCkanMetadataCacheTest {
    
    private InMemoryCkanMetadataCache ckanCache;
    
    @Before
    public void setUp() {
        ckanCache = new InMemoryCkanMetadataCache();
    }
    
    @Test
    public void shouldInstantiateEmpty() {
        final Iterable<String> ids = ckanCache.getDatasetIds();
        MatcherAssert.assertThat(ids.iterator().hasNext(), CoreMatchers.is(false));
    }
    
    @Test
    public void shouldReturnResourceDescription() {
        CkanDataset dataset = new CkanDataset("test-dataset");
        CkanPair extras = new CkanPair(CkanConstants.SchemaDescriptor.SCHEMA_DESCRIPTOR, "{\"resource_type\":\"csv-observations-collection\",\"schema_descriptor_version\":\"0.1\"}");
        dataset.setExtras(Collections.singletonList(extras));
        
        ckanCache.insertOrUpdate(dataset);
        final SchemaDescriptor actual = ckanCache.getSchemaDescription(dataset.getId());
        String actualId = actual.getVersion();
        MatcherAssert.assertThat(actualId, CoreMatchers.is("0.1"));
    }
    
    private CkanResource createRandomCkanResource(CkanDataset dataset) {
        String id = UUID.randomUUID().toString();
        String url = "https://nonsense.eu/" + id;
        CkanResource normalResource = new CkanResource(url, dataset.getId());
        normalResource.setId(id);
        return normalResource;
    }
}
