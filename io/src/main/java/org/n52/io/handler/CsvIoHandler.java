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

package org.n52.io.handler;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.n52.io.IoParseException;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.TimeOutput;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.DatasetParameters;
import org.n52.io.response.dataset.TimeseriesMetadataOutput;

public abstract class CsvIoHandler<T extends AbstractValue< ? >> extends IoHandler<Data<T>> {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    // needed by some clients to detect UTF-8 encoding (e.g. excel)
    private static final String UTF8_BYTE_ORDER_MARK = "\uFEFF";

    private final List< ? extends DatasetOutput<T>> seriesMetadatas;

    private final boolean useByteOrderMark;

    private final String tokenSeparator;

    private boolean zipOutput;

    public CsvIoHandler(IoParameters parameters,
                        IoProcessChain<Data<T>> processChain,
                        List< ? extends DatasetOutput<T>> seriesMetadatas) {
        super(parameters, processChain);
        this.seriesMetadatas = seriesMetadatas;
        this.tokenSeparator = parameters.getAsString(Parameters.TOKEN_SEPARATOR, ";");
        this.useByteOrderMark = parameters.getAsBoolean(Parameters.BOM, true);
    }

    protected abstract String[] getHeader(DatasetOutput<T> metadata);

    protected abstract void writeData(DatasetOutput<T> metadata, Data<T> series, OutputStream stream)
            throws IOException;

    protected abstract String getFilenameFor(DatasetOutput<T> seriesMetadata);

    protected List<DatasetOutput<T>> getMetadatas() {
        return Collections.unmodifiableList(seriesMetadatas);
    }

    @Override
    public void encodeAndWriteTo(DataCollection<Data<T>> data, OutputStream stream) throws IoParseException {
        try {
            if (zipOutput) {
                writeAsZipStream(data, stream);
            } else {
                writeAsSingleCsv(data, stream);
            }
        } catch (IOException e) {
            throw new IoParseException("Could not write CSV to output stream.", e);
        }
    }

    protected void writeAsZipStream(DataCollection<Data<T>> data, OutputStream stream) throws IOException {
        try (ZipOutputStream zipStream = new ZipOutputStream(stream)) {
            for (DatasetOutput<T> dataset : seriesMetadatas) {
                String filename = getFilenameFor(dataset) + ".csv";

                ZipEntry zipEntry = new ZipEntry(filename);
                zipStream.putNextEntry(zipEntry);

                writeHeader(dataset, zipStream);
                Data<T> series = data.getSeries(dataset.getId());
                writeData(dataset, series, zipStream);

                zipStream.closeEntry();
                zipStream.flush();
            }
        }
    }

    private void writeAsSingleCsv(DataCollection<Data<T>> data, OutputStream stream) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(stream)) {
            writeHeader(null, bos);
            writeData(data, bos);
            bos.flush();
        }
    }

    private void writeData(DataCollection<Data<T>> data, OutputStream stream) throws IOException {
        for (DatasetOutput<T> metadata : seriesMetadatas) {
            Data<T> series = data.getSeries(metadata.getId());
            writeData(metadata, series, stream);
        }
    }

    protected void writeHeader(DatasetOutput<T> dataset, OutputStream stream) throws IOException {
        String text = csvEncode(getHeader(dataset));
        if (useByteOrderMark) {
            text = UTF8_BYTE_ORDER_MARK + text;
        }
        writeText(text, stream);
    }

    protected void writeText(String text, OutputStream stream) throws IOException {
        stream.write(text.getBytes(UTF8));
    }

    protected String csvEncode(String[] values) {
        return Stream.of(values)
                     .map(value -> {
                         return value.contains(tokenSeparator)
                                 ? "\"" + value + "\""
                                 : value;
                     })
                     .collect(Collectors.joining(tokenSeparator))
                     .concat("\n");
    }

    protected String parseTime(T value) {
        TimeOutput timestart = value.getTimestart();
        TimeOutput timeend = value.getTimeend();
        TimeOutput timestamp = value.getTimestamp();
        return timestart != null
                ? timestart.getDateTime() + "/" + timeend.getDateTime()
                : timestamp.getDateTime().toString();
    }

    public void setZipOutput(boolean zipOutput) {
        this.zipOutput = zipOutput;
    }

    public boolean isZipOutput() {
        return zipOutput;
    }

    protected String getLabel(ParameterOutput output) {
        return output.getLabel();
    }

    protected String getPlatformLabel(DatasetOutput<T> metadata) {
        DatasetParameters parameters = metadata.getDatasetParameters(true);
        ParameterOutput platform = parameters.getPlatform();
        return platform == null
                ? getLabel( ((TimeseriesMetadataOutput) metadata).getStation())
                : platform.getLabel();
    }

}
