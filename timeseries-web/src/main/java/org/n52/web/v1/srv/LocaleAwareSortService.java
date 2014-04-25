/**
 * Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.web.v1.srv;

import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

import org.n52.io.IoParameters;
import org.n52.io.v1.data.CollatorComparable;

public class LocaleAwareSortService<T> implements ParameterService<T> {

    private ParameterService<T> composedService;

    public LocaleAwareSortService(ParameterService<T> toCompose) {
        this.composedService = toCompose;
    }

    protected Collator createCollator(String locale) {
        return Collator.getInstance(new Locale(locale));
    }

    @Override
    public T[] getExpandedParameters(IoParameters query) {
        T[] result = composedService.getExpandedParameters(query);
        sort(createCollator(query.getLocale()), result);
        return result;
    }

    @Override
    public T[] getCondensedParameters(IoParameters query) {
        T[] result = composedService.getCondensedParameters(query);
        sort(createCollator(query.getLocale()), result);
        return result;
    }

    @Override
    public T[] getParameters(String[] items) {
        T[] result = composedService.getParameters(items);
        Arrays.sort(result); // TODO sort locale dependend
        return result;
    }

    @Override
    public T[] getParameters(String[] items, IoParameters query) {
        T[] result = composedService.getParameters(items, query);
        sort(createCollator(query.getLocale()), result);
        return result;
    }

    @Override
    public T getParameter(String item) {
        return composedService.getParameter(item);
    }

    @Override
    public T getParameter(String item, IoParameters query) {
        return composedService.getParameter(item, query);
    }

    private void sort(Collator collator, T[] toSort) {
        if (toSort == null || toSort.length == 0) {
            return;
        }
        
        if ( !isCollatorComparable(toSort)) {
            Arrays.sort(toSort);
            return;
        }
        
        for (int i = 0; i < toSort.length; i++) {
            for (int j = i + 1; j < toSort.length; j++) {
                CollatorComparable<T> first = (CollatorComparable<T>) toSort[i];
                T second = toSort[j];
                if (first.compare(collator, second) > 0) {
                    swap(toSort, i, j);
                }
            }
        }
    }

    private boolean isCollatorComparable(T[] toSort) {
        return CollatorComparable.class.isAssignableFrom(toSort[0].getClass());
    }

    private void swap(T[] container, int i, int j) {
        T tmp = container[i];
        container[i] = container[j];
        container[j] = tmp;;
    }

}
