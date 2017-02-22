/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.n52.series.srv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.n52.io.request.FilterResolver;
import org.n52.io.request.IoParameters;
import org.n52.io.response.CategoryOutput;
import org.n52.io.response.FeatureOutput;
import org.n52.io.response.PhenomenonOutput;
import org.n52.io.response.PlatformOutput;
import org.n52.io.response.ProcedureOutput;
import org.n52.io.response.StationOutput;
import org.n52.io.response.TimeseriesMetadataOutput;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.series.db.da.OutputAssembler;
import org.n52.series.spi.search.SearchResult;
import org.n52.series.spi.search.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

public class Search implements SearchService {

    @Autowired
    private OutputAssembler<ProcedureOutput> procedureRepository;

    @Autowired
    private OutputAssembler<PhenomenonOutput> phenomenonRepository;

    @Autowired
    private OutputAssembler<FeatureOutput> featureRepository;

    @Autowired
    private OutputAssembler<CategoryOutput> categoryRepository;

    @Autowired
    private OutputAssembler<PlatformOutput> platformRepository;

    @Autowired
    private OutputAssembler<DatasetOutput<?,?>> datasetRepository;

    @Autowired
    @Deprecated
    private OutputAssembler<TimeseriesMetadataOutput> timeseriesRepository;

    @Autowired
    @Deprecated
    private OutputAssembler<StationOutput> stationRepository;

    @Override
    public Collection<SearchResult> searchResources(IoParameters parameters) {
        List<SearchResult> results = new ArrayList<>();
        results.addAll(phenomenonRepository.searchFor(parameters));
        results.addAll(procedureRepository.searchFor(parameters));
        results.addAll(featureRepository.searchFor(parameters));
        results.addAll(categoryRepository.searchFor(parameters));
        results.addAll(platformRepository.searchFor(parameters));
        results.addAll(datasetRepository.searchFor(parameters));

        FilterResolver filterResolver = new FilterResolver(parameters);
        if (filterResolver.shallBehaveBackwardsCompatible()) {
            results.addAll(timeseriesRepository.searchFor(parameters));
            results.addAll(stationRepository.searchFor(parameters));
        }
        return results;
    }

}
