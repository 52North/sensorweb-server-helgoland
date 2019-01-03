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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.joda.time.DateTime;
import org.n52.io.IoParseException;
import org.n52.io.handler.CsvIoHandler;
import org.n52.io.handler.IoProcessChain;
import org.n52.io.request.IoParameters;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.DatasetOutput;
import org.n52.io.response.dataset.TimeseriesMetadataOutput;
import org.n52.io.response.dataset.quantity.QuantityValue;

// TODO extract non quantity specifics to csvhandler

public class QuantityCsvIoHandler extends CsvIoHandler<Data<QuantityValue>> {

    private static final String STATION = "station";
    private static final String PHENOMENON = "phenomenon";
    private static final String PROCEDURE = "procedure";
    private static final String UOM = "uom";
    private static final String TIME = "time";
    private static final String VALUE = "value";

    private static final Charset UTF8 = Charset.forName("UTF-8");

    // needed by some clients to detect UTF-8 encoding (e.g. excel)
    private static final String UTF8_BYTE_ORDER_MARK = "\uFEFF";

    private final List< ? extends DatasetOutput> seriesMetadatas;

    private NumberFormat numberformat = DecimalFormat.getInstance();

    private boolean useByteOrderMark = true;

    private String tokenSeparator = ";";

    private boolean zipOutput;

    public QuantityCsvIoHandler(IoParameters parameters,
                                IoProcessChain<Data<QuantityValue>> processChain,
                                List< ? extends DatasetOutput> seriesMetadatas) {
        super(parameters, processChain);
        this.numberformat = DecimalFormat.getInstance(i18n.getLocale());
        this.seriesMetadatas = seriesMetadatas;
    }

    @Override
    protected String[] getHeader() {
        return new String[] {STATION, PHENOMENON, PROCEDURE, UOM, TIME, VALUE};
    }

    public void setTokenSeparator(String tokenSeparator) {
        this.tokenSeparator = tokenSeparator == null ? this.tokenSeparator : tokenSeparator;
    }

    public void setIncludeByteOrderMark(boolean byteOrderMark) {
        this.useByteOrderMark = byteOrderMark;
    }

    public void setZipOutput(boolean zipOutput) {
        this.zipOutput = zipOutput;
    }

    @Override
    public void encodeAndWriteTo(DataCollection<Data<QuantityValue>> data, OutputStream stream)
            throws IoParseException {
        try {
            if (zipOutput) {
                writeAsZipStream(data, stream);
            } else {
                writeAsPlainCsv(data, stream);
            }
        } catch (IOException e) {
            throw new IoParseException("Could not write CSV to output stream.", e);
        }
    }

    private void writeAsPlainCsv(DataCollection<Data<QuantityValue>> data, OutputStream stream) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(stream);
        writeHeader(bos);
        writeData(data, bos);
        bos.flush();
    }

    private void writeAsZipStream(DataCollection<Data<QuantityValue>> data, OutputStream stream) throws IOException {
        try (ZipOutputStream zipStream = new ZipOutputStream(stream)) {
            StringBuilder filename = new StringBuilder();
            Data<QuantityValue> series;
            if (seriesMetadatas.size() == 1) {
                Map<String, String> metadataMap = parseMetadata(seriesMetadatas.get(0));
                filename.append(metadataMap.get(STATION) + "_");
                filename.append(metadataMap.get(PHENOMENON) + "_");
                filename.append(metadataMap.get(PROCEDURE));

                // Check if filename is exceeding 255 (including extension) character bound of most Filesystems.
                // Fallback to only station name if that is the case
                if (filename.length() > 251) {
                    filename.append(metadataMap.get(STATION));
                }
            } else {
                String prefix = "multiple_datasets_";
                filename.append(prefix);
                for (DatasetOutput metadata : seriesMetadatas) {
                    filename.append(getStation(metadata) + "_");
                }
                // Remove trailing underscore
                filename.deleteCharAt(filename.length() - 1);

                // Check if filename is exceeding 255 (including extension) character bound of most Filesystems.
                // Fallback to UUID if that is the case
                if (filename.length() > 251) {
                    filename.append(prefix + UUID.randomUUID());
                }
            }
            filename.append(".csv");

            zipStream.putNextEntry(new ZipEntry(filename.toString()));
            writeHeader(zipStream);
            writeData(data, zipStream);
            zipStream.closeEntry();
            zipStream.flush();
        }
    }

    private void writeHeader(OutputStream stream) throws IOException {
        String csvLine = csvEncode(getHeader());
        if (useByteOrderMark) {
            csvLine = UTF8_BYTE_ORDER_MARK + csvLine;
        }
        writeCsvLine(csvLine, stream);
    }

    private void writeData(DataCollection<Data<QuantityValue>> data, OutputStream stream) throws IOException {
        for (DatasetOutput metadata : seriesMetadatas) {
            Data<QuantityValue> series = data.getSeries(metadata.getId());
            writeData(metadata, series, stream);
        }
    }

    private void writeData(DatasetOutput metadata, Data<QuantityValue> series, OutputStream stream) throws IOException {
        Map<String, String> metadataMap = parseMetadata(metadata);
        String[] values = new String[getHeader().length];
        values[0] = metadataMap.get(getHeader()[0]);
        values[1] = metadataMap.get(getHeader()[1]);
        values[2] = metadataMap.get(getHeader()[2]);
        values[3] = metadataMap.get(getHeader()[3]);

        for (QuantityValue timeseriesValue : series.getValues()) {
            Long timestart = timeseriesValue.getTimestart();
            Long timeend = (timestart != null) ? timeseriesValue.getTimeend() : timeseriesValue.getTimestamp();
            values[4] = getISO8601Time(timestart, timeend);
            values[5] = numberformat.format(timeseriesValue.getValue());
            writeCsvLine(csvEncode(values), stream);
        }
    }

    private void writeCsvLine(String line, OutputStream stream) throws IOException {
        stream.write(line.getBytes(UTF8));
    }

    private String csvEncode(String[] values) {
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append(tokenSeparator);
        }
        sb.deleteCharAt(sb.lastIndexOf(tokenSeparator));
        return sb.append("\n").toString();
    }

    private Map<String, String> parseMetadata(DatasetOutput metadata) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(STATION, getStation(metadata));
        map.put(PHENOMENON, metadata.getDatasetParameters(true).getPhenomenon().getLabel());
        map.put(PROCEDURE, metadata.getDatasetParameters(true).getProcedure().getLabel());
        map.put(UOM, metadata.getUom());
        return map;
    }

    private String getISO8601Time(Long start, Long end) {
        return ((start == null) ? "" : (new DateTime(start).toString() + "/")) + new DateTime(end).toString();
    }

    private String getStation(DatasetOutput metadata) {
        ParameterOutput platform = metadata.getDatasetParameters(true).getPlatform();
        if (platform == null) {
            TimeseriesMetadataOutput output = (TimeseriesMetadataOutput) metadata;
            return output.getStation().getLabel();
        } else {
            return platform.getLabel();
        }
    }

}
