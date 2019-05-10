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
package org.n52.series.spi.srv;

import java.text.Collator;
import java.util.Locale;

import org.n52.io.request.IoParameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;

public class LocaleAwareSortService<T extends ParameterOutput> extends ParameterService<T> {

    private final ParameterService<T> composedService;

    public LocaleAwareSortService(ParameterService<T> toCompose) {
        this.composedService = toCompose;
    }

    protected Collator createCollator(String locale) {
        return Collator.getInstance(new Locale(locale));
    }

    @Override
    public OutputCollection<T> getExpandedParameters(IoParameters query) {
        OutputCollection<T> result = composedService.getExpandedParameters(query);
        return result.withSortedItems(createCollator(query.getLocale()));
    }

    @Override
    public OutputCollection<T> getCondensedParameters(IoParameters query) {
        OutputCollection<T> result = composedService.getCondensedParameters(query);
        return result.withSortedItems(createCollator(query.getLocale()));
    }

    @Override
    public OutputCollection<T> getParameters(String[] items, IoParameters query) {
        OutputCollection<T> result = composedService.getParameters(items, query);
        return result.withSortedItems(createCollator(query.getLocale()));
    }

    @Override
    public T getParameter(String item, IoParameters query) {
        return composedService.getParameter(item, query);
    }

    @Override
    public boolean exists(String id, IoParameters parameters) {
        return composedService.exists(id, parameters);
    }

    @Override
    public RawDataService getRawDataService() {
        return composedService.getRawDataService();
    }

    @Override
    public boolean supportsRawData() {
        return composedService.supportsRawData();
    }

}
