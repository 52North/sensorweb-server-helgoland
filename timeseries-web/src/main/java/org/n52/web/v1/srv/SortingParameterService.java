/**
 * ﻿Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.n52.web.v1.srv;

import java.util.Arrays;

import org.n52.io.IoParameters;

public class SortingParameterService<T> implements ParameterService<T> {

    private ParameterService<T> composedService;

    public SortingParameterService(ParameterService<T> toCompose) {
        this.composedService = toCompose;
    }

    @Override
    public T[] getExpandedParameters(IoParameters query) {
        T[] result = composedService.getExpandedParameters(query);
        Arrays.sort(result);
        return result;
    }

    @Override
    public T[] getCondensedParameters(IoParameters query) {
        T[] result = composedService.getCondensedParameters(query);
        Arrays.sort(result);
        return result;
    }

    @Override
    public T[] getParameters(String[] items) {
        T[] result = composedService.getParameters(items);
        Arrays.sort(result);
        return result;
    }

    @Override
    public T[] getParameters(String[] items, IoParameters query) {
        T[] result = composedService.getParameters(items, query);
        Arrays.sort(result);
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

    
}
