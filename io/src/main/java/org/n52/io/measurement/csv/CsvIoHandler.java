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
package org.n52.io.measurement.csv;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.joda.time.DateTime;
import org.n52.io.I18N;
import org.n52.io.IoHandler;
import org.n52.io.IoParseException;
import org.n52.io.measurement.img.RenderingContext;
import org.n52.io.response.series.MeasurementData;
import org.n52.io.response.series.MeasurementSeriesOutput;
import org.n52.io.response.series.MeasurementValue;
import org.n52.io.response.series.SeriesDataCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvIoHandler implements IoHandler<MeasurementData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvIoHandler.class);

    private static final String[] HEADER = {"station", "phenomenon", "uom", "date", "value"};

    private static final Charset UTF8 = Charset.forName("UTF-8");

    // needed by some clients to detect UTF-8 encoding (e.g. excel)
    private static final String UTF8_BYTE_ORDER_MARK = "\uFEFF";

    private RenderingContext context = RenderingContext.createEmpty();

    private NumberFormat numberformat = DecimalFormat.getInstance();

    private SeriesDataCollection<MeasurementData> data = new SeriesDataCollection<>();

    private boolean useByteOrderMark = true;

    private boolean zipOutput = false;

    private String tokenSeparator = ";";

    public CsvIoHandler(RenderingContext context, String locale) {
        this.context = context;
        I18N i18n = I18N.getMessageLocalizer(locale);
        this.numberformat = DecimalFormat.getInstance(i18n.getLocale());
    }

    public void setTokenSeparator(String tokenSeparator) {
        this.tokenSeparator = tokenSeparator == null
                ? this.tokenSeparator
                : tokenSeparator;
    }

    public void setIncludeByteOrderMark(boolean byteOrderMark) {
        this.useByteOrderMark = byteOrderMark;
    }

    public void setZipOutput(boolean zipOutput) {
        this.zipOutput = zipOutput;
    }

    @Override
    public void generateOutput(SeriesDataCollection<MeasurementData> data) throws IoParseException {
        // hold the data so we can stream it directly when #encodeAndWriteTo is called
        this.data = data;
    }

    @Override
    public void encodeAndWriteTo(OutputStream stream) throws IoParseException {
        BufferedOutputStream bos = new BufferedOutputStream(stream);
        ZipOutputStream zipStream = null;
        try {
            if (zipOutput) {
                zipStream = new ZipOutputStream(stream);
                zipStream.putNextEntry(new ZipEntry("csv-zip-content.csv"));
                writeHeader(zipStream);
                writeData(zipStream);
            } else {
                writeHeader(bos);
                writeData(bos);
            }
        } catch (IOException e) {
            throw new IoParseException("Could not write CSV to output stream.", e);
        } finally {
            try {
                if (zipStream != null) {
                    zipStream.flush();
                    zipStream.close();
                }
            } catch (IOException e) {
                LOGGER.debug("Stream already flushed and closed.", e);
            }
        }
    }

    private void writeHeader(OutputStream stream) throws IOException {
        String csvLine = csvEncode(HEADER);
        if (useByteOrderMark) {
            csvLine = UTF8_BYTE_ORDER_MARK + csvLine;
        }
        writeCsvLine(csvLine, stream);
    }

    private void writeData(OutputStream stream) throws IOException {
        for (MeasurementSeriesOutput metadata : context.getSeriesMetadatas()) {
            MeasurementData timeseries = data.getSeries(metadata.getId());
            String station = metadata.getSeriesParameters().getPlatform().getLabel();
            // instanceof SeriesMetadataV1Output // XXX hack
            //? (String) ((SeriesMetadataV1Output) metadata).getStation().getProperties().get("label")
            //: ((SeriesMetadataV2Output) metadata).getLabel();
            String phenomenon = metadata.getSeriesParameters().getPhenomenon().getLabel();
            String uom = metadata.getUom();

            for (MeasurementValue timeseriesValue : timeseries.getValues()) {
                String[] values = new String[HEADER.length];
                values[0] = station;
                values[1] = phenomenon;
                values[2] = uom;

                long timestamp = timeseriesValue.getTimestamp();
                values[3] = new DateTime(timestamp).toString();
                values[4] = numberformat.format(timeseriesValue.getValue());
                writeCsvLine(csvEncode(values), stream);
            }
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

}
