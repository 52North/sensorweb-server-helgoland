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

package org.n52.io.type.quantity.handler.csv;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.n52.io.handler.CsvIoHandler;
import org.n52.io.handler.IoProcessChain;
import org.n52.io.request.IoParameters;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.DatasetParameters;
import org.n52.io.response.dataset.quantity.QuantityValue;

// TODO extract non quantity specifics to csvhandler

public class QuantityCsvIoHandler extends CsvIoHandler<QuantityValue> {

    private static final String PLATFORM = "platform";
    private static final String PHENOMENON = "phenomenon";
    private static final String PROCEDURE = "procedure";
    private static final String UOM = "uom";
    private static final String TIME = "time";
    private static final String VALUE = "value";

    public QuantityCsvIoHandler(IoParameters parameters,
                                IoProcessChain<Data<QuantityValue>> processChain,
                                List<? extends DatasetOutput<QuantityValue>> seriesMetadatas) {
        super(parameters, processChain, seriesMetadatas);
    }

    @Override
    public String[] getHeader(DatasetOutput<QuantityValue> metadata) {
        return new String[] {
            PLATFORM,
            PHENOMENON,
            PROCEDURE,
            UOM,
            TIME,
            VALUE
        };
    }

    @Override
    protected void writeData(DatasetOutput<QuantityValue> metadata, Data<QuantityValue> series, OutputStream stream)
            throws IOException {
        DatasetParameters parameters = metadata.getDatasetParameters();
        String[] row = new String[getHeader(metadata).length];
        
        row[0] = getPlatformLabel(metadata);
        row[1] = getLabel(parameters.getPhenomenon());
        row[2] = getLabel(parameters.getProcedure());
        row[3] = metadata.getUom();

        for (QuantityValue value : series.getValues()) {
            row[4] = parseTime(value);
            row[5] = value.getFormattedValue();
            writeText(csvEncode(row), stream);
        }
    }

    @Override
    protected String getFilenameFor(DatasetOutput<QuantityValue> metadata) {
        DatasetParameters datasetParameters = metadata.getDatasetParameters(true);
        String filename = Stream.of(getPlatformLabel(metadata),
                                    getLabel(datasetParameters.getPhenomenon()),
                                    getLabel(datasetParameters.getProcedure()),
                                    metadata.getUom())
                                .collect(Collectors.joining("_"));

        return !hasAppropriateLength(filename)
                ? shortenFileName(filename)
                : filename;
    }

    private boolean hasAppropriateLength(String filename) {
        return filename.length() > 251;
    }

    private String shortenFileName(String filename) {
        return filename.substring(0, 250);
    }

}
