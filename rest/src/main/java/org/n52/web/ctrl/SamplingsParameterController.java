/*
 * Copyright (C) 2013-2021 52°North Spatial Information Research GmbH
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
package org.n52.web.ctrl;

import java.util.Collections;

import org.joda.time.DateTime;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.sampling.SamplingOutput;
import org.n52.series.spi.srv.CountingMetadataService;
import org.n52.series.spi.srv.ParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = UrlSettings.COLLECTION_SAMPLINGS)
public class SamplingsParameterController extends ParameterRequestMappingAdapter<SamplingOutput> {

    @Autowired
    public SamplingsParameterController(CountingMetadataService counter, ParameterService<SamplingOutput> service) {
        super(counter, service);
    }

    @Override
    public String getCollectionName() {
        return UrlSettings.COLLECTION_SAMPLINGS;
    }

    @Override
    protected MultiValueMap<String, String> addAdditionalParameter(MultiValueMap<String, String> query) {
        if (!query.containsKey(Parameters.TIMESPAN)) {
            DateTime now = new DateTime();
            // TODO make this configurable
            query.put(Parameters.TIMESPAN,
                    Collections.singletonList(IoParameters.createTimespan(now.minusMonths(3), now).toString()));
        }
        return super.addAdditionalParameter(query);
    }
}
