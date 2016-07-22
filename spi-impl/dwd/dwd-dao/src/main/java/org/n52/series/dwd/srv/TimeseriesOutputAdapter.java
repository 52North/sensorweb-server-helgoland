/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.series.dwd.srv;

import java.util.Comparator;

import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.TimeseriesMetadataOutput;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.series.spi.srv.DataService;
import org.n52.series.spi.srv.ParameterService;

public class TimeseriesOutputAdapter <T extends ParameterOutput> extends ParameterService<TimeseriesMetadataOutput> implements DataService<Data<? extends AbstractValue<?>>> {

    private OutputCollection<TimeseriesMetadataOutput> createOutputCollection() {
        return new OutputCollection<TimeseriesMetadataOutput>() {
            @Override
            protected Comparator<TimeseriesMetadataOutput> getComparator() {
                return ParameterOutput.defaultComparator();
            }
        };
    }

    @Override
    public OutputCollection<TimeseriesMetadataOutput> getExpandedParameters(IoParameters query) {
        return createOutputCollection();
    }

    @Override
    public OutputCollection<TimeseriesMetadataOutput> getCondensedParameters(IoParameters query) {
        return createOutputCollection();
    }

    @Override
    public OutputCollection<TimeseriesMetadataOutput> getParameters(String[] items, IoParameters query) {
        return createOutputCollection();
    }

    @Override
    public TimeseriesMetadataOutput getParameter(String item, IoParameters query) {
        return null;
    }

    @Override
    public boolean exists(String id) {
        return false;
    }

    @Override
    public DataCollection<Data<? extends AbstractValue<?>>> getData(RequestSimpleParameterSet parameters) {
        return null;
    }

}
