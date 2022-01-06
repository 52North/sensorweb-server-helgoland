/*
 * Copyright (C) 2013-2022 52°North Spatial Information Research GmbH
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

import org.n52.io.response.CategoryOutput;
import org.n52.series.spi.srv.CountingMetadataService;
import org.n52.series.spi.srv.ParameterService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(UrlSettings.COLLECTION_CATEGORIES)
public class CategoriesParameterController extends ParameterRequestMappingAdapter<CategoryOutput> {

    public CategoriesParameterController(CountingMetadataService counter, ParameterService<CategoryOutput> service) {
        super(counter, service);
    }

    @Override
    public String getCollectionName() {
        return UrlSettings.COLLECTION_CATEGORIES;
    }

}
