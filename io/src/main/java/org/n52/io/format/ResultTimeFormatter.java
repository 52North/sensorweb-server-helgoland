/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.io.format;

import java.util.Map.Entry;

import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;

public class ResultTimeFormatter<I extends Data< ? extends AbstractValue< ? >>>
        implements DataFormatter<I, ResultTimeClassifiedData<AbstractValue< ? >>> {

    @Override
    public DataCollection<ResultTimeClassifiedData<AbstractValue< ? >>> format(DataCollection<I> toFormat) {
        DataCollection<ResultTimeClassifiedData<AbstractValue< ? >>> formatted = new DataCollection<>();
        for (Entry<String, I> entry : toFormat.getAllSeries()
                                              .entrySet()) {
            String datasetId = entry.getKey();
            formatted.addNewSeries(datasetId, formatData(entry.getValue()));
        }
        return formatted;
    }

    private ResultTimeClassifiedData<AbstractValue< ? >> formatData(Data< ? extends AbstractValue< ? >> data) {
        ResultTimeClassifiedData<AbstractValue< ? >> rtData = new ResultTimeClassifiedData<>();
        for (AbstractValue< ? > value : data.getValues()) {
            rtData.classifyValue(value);
        }
        return rtData;
    }

}
