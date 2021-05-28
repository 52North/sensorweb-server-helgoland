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
package org.n52.io.type.quantity;

import org.n52.io.format.ResultTimeClassifiedData;
import org.n52.io.format.ResultTimeFormatter;
import org.n52.io.handler.IoProcessChain;
import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.quantity.QuantityValue;
import org.n52.io.type.quantity.format.FormatterFactory;
import org.n52.io.type.quantity.generalize.GeneralizingQuantityService;
import org.n52.series.spi.srv.DataService;

final class QuantityIoProcessChain implements IoProcessChain<Data<QuantityValue>> {

    private final DataService<Data<QuantityValue>> dataService;

    private final IoParameters parameters;

    QuantityIoProcessChain(DataService<Data<QuantityValue>> dataService, IoParameters parameters) {
        this.dataService = dataService;
        this.parameters = parameters;
    }

    @Override
    public DataCollection<Data<QuantityValue>> getData() {
        boolean generalize = parameters.isGeneralize();
        DataService<Data<QuantityValue>> service = generalize
                ? new GeneralizingQuantityService(dataService)
                : dataService;
        return service.getData(parameters);
    }

    @Override
    public DataCollection< ? > getProcessedData() {
        return parameters.shallClassifyByResultTimes()
                ? formatAccordingToResultTimes()
                : formatValueOutputs();
    }

    private DataCollection<ResultTimeClassifiedData<AbstractValue< ? >>> formatAccordingToResultTimes() {
        return new ResultTimeFormatter<Data<QuantityValue>>().format(getData());
    }

    private DataCollection< ? > formatValueOutputs() {
        FormatterFactory factory = FormatterFactory.createFormatterFactory(parameters);
        DataCollection<Data<QuantityValue>> data = getData();
        return factory.create()
                      .format(data);
    }

}
