/*
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
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
package org.n52.io.handler.simple;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.locationtech.jts.geom.Geometry;
import org.n52.io.handler.CsvIoHandler;
import org.n52.io.handler.IoProcessChain;
import org.n52.io.request.IoParameters;
import org.n52.io.response.FeatureOutput;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.DatasetParameters;

public class SimpleCsvIoHandler<T extends AbstractValue< ? >> extends CsvIoHandler<T> {

    private static final String HEADER_PHENOMENON = "Phenomenon: ";
    private static final String META_SENSOR = "Sensor: ";
    private static final String META_FEATURE = "Feature: ";
    private static final String META_UNIT = "Unit: ";
    private static final String META_GEOMETRY = "Geometry: ";
    private static final String COLUMN_GEOMETRY = "geometry";
    private static final String COLUMN_VALUE = "value";
    private static final String COLUMN_TIME = "time";
    private static final String LINEBREAK = "\n";

    public SimpleCsvIoHandler(IoParameters parameters,
                              IoProcessChain<Data<T>> processChain,
                              List< ? extends DatasetOutput<T>> seriesMetadatas) {
        super(parameters, processChain, seriesMetadatas);
    }

    @Override
    public String[] getHeader(DatasetOutput<T> metadata) {
        StringBuilder metaHeader = new StringBuilder();
        DatasetParameters datasetParameters = metadata.getDatasetParameters(true);

        metaHeader.append(HEADER_PHENOMENON)
                  .append(getLabel(datasetParameters.getPhenomenon()))
                  .append(LINEBREAK);
        metaHeader.append(META_SENSOR)
                  .append(getPlatformLabel(metadata))
                  .append(LINEBREAK);
        metaHeader.append(META_UNIT)
                  .append(metadata.getUom())
                  .append(LINEBREAK);

        return isTrajectory(metadata)
                ? createTrajectoryHeader(metaHeader)
                : createSimpleHeader(metadata, metaHeader);

    }

    private String[] createSimpleHeader(DatasetOutput<T> metadata, StringBuilder metaHeader) {
        FeatureOutput feature = metadata.getFeature();
        metaHeader.append(META_FEATURE)
                  .append(getLabel(feature))
                  .append(LINEBREAK);

        Geometry geometry = feature.getGeometry();
        metaHeader.append(META_GEOMETRY)
                  .append(geometry.toText())
                  .append(LINEBREAK);

        /*
         * Note: last line break will cause an empty first column
         */
        return new String[] {
            metaHeader.toString(),
            COLUMN_TIME,
            COLUMN_VALUE,
        };
    }

    private String[] createTrajectoryHeader(StringBuilder metaHeader) {
        return new String[] {
            // Note: first column after last line break
            metaHeader.append(COLUMN_GEOMETRY)
                      .toString(),
            COLUMN_TIME,
            COLUMN_VALUE,
        };
    }

    @Override
    protected void writeData(DatasetOutput<T> metadata, Data<T> series, OutputStream stream)
            throws IOException {
        int columnSize = getHeader(metadata).length;
        String[] row = new String[columnSize];
        for (T value : series.getValues()) {
            row[0] = isTrajectory(metadata)
                    ? value.getGeometry().toString()
                    : "";
            row[1] = parseTime(value);
            row[2] = value.getFormattedValue();
            writeText(csvEncode(row), stream);
        }
    }

    @Override
    protected String getFilenameFor(DatasetOutput<T> metadata) {
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
        return filename.length() < 251;
    }

    private String shortenFileName(String filename) {
        return filename.substring(0, 250);
    }

}
