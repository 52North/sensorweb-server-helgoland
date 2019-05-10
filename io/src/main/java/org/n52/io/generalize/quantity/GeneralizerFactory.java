/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.io.generalize.quantity;

import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneralizerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(GeneralizerFactory.class);

    private static final String GENERALIZING_ALGORITHM = "generalizing_algorithm";

    private static final String LARGEST_TRIANGLE_THREE_BUCKETS = "LTTB";

    private static final String DOUGLAS_PEUCKER = "DP";

    public static final Generalizer<Data<QuantityValue>> createGeneralizer(IoParameters parameters) {

        if (!parameters.isGeneralize()) {
            return new NoActionGeneralizer(parameters);
        }

        String algorithm = parameters.containsParameter(GENERALIZING_ALGORITHM)
                ? parameters.getOther(GENERALIZING_ALGORITHM)
                : LARGEST_TRIANGLE_THREE_BUCKETS;

        Generalizer<Data<QuantityValue>> generalizer;
        if (LARGEST_TRIANGLE_THREE_BUCKETS.equalsIgnoreCase(algorithm)) {
            generalizer = new LargestTriangleThreeBucketsGeneralizer(parameters);
        } else if (DOUGLAS_PEUCKER.equalsIgnoreCase(algorithm)) {
            generalizer = new DouglasPeuckerGeneralizer(parameters);
        } else {
            LOG.info("No generalizing algorithm found for code: {}.", algorithm);
            generalizer = new NoActionGeneralizer(parameters);
        }

        LOG.info("Selected {} algorithm.", generalizer.getName());
        return generalizer;
    }

}
