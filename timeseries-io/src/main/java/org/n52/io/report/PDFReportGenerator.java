/**
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.io.report;

import static java.io.File.createTempFile;
import static org.n52.io.MimeType.APPLICATION_PDF;
import static org.n52.io.MimeType.IMAGE_PNG;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.joda.time.DateTime;
import org.n52.io.IoHandler;
import org.n52.io.IoParseException;
import org.n52.io.format.TvpDataCollection;
import org.n52.io.img.ChartRenderer;
import org.n52.io.v1.data.TimeseriesData;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.n52.io.v1.data.TimeseriesOutput;
import org.n52.io.v1.data.TimeseriesValue;
import org.n52.oxf.DocumentStructureDocument;
import org.n52.oxf.DocumentStructureType;
import org.n52.oxf.DocumentStructureType.TimeSeries;
import org.n52.oxf.MetadataType;
import org.n52.oxf.MetadataType.GenericMetadataPair;
import org.n52.oxf.TableType;
import org.n52.oxf.TableType.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PDFReportGenerator extends ReportGenerator implements IoHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PDFReportGenerator.class);

    private static final String LOCALE_REPLACER = "{locale}";

    private static final String PDF_TRANSORMATION_RULES = "pdf/Document_2_PDF_" + LOCALE_REPLACER + ".xslt";

    private final DocumentStructureDocument document;

    private final ChartRenderer renderer;

    public PDFReportGenerator(ChartRenderer renderer, String locale) {
        super(renderer.getRenderingContext(), locale);
        this.document = DocumentStructureDocument.Factory.newInstance();
        this.document.addNewDocumentStructure();
        this.renderer = configureRenderer(renderer);
    }

    private ChartRenderer configureRenderer(ChartRenderer renderer) {
        renderer.setMimeType(IMAGE_PNG);
        renderer.setShowTooltips(false);
        return renderer;
    }

    @Override
    public void generateOutput(TvpDataCollection data) throws IoParseException {
        try {
            generateTimeseriesChart(data);
            generateTimeseriesMetadata();
        } catch(IOException e) {
            throw new IoParseException("Error handling (temp) file!", e);
        }
    }

    private void generateTimeseriesChart(TvpDataCollection data) throws IOException {
        FileOutputStream stream = null;
        try {
            renderer.generateOutput(data);
            File tmpFile = createTempFile("52n_swc_", "_chart.png");
            stream = new FileOutputStream(tmpFile);
            renderer.encodeAndWriteTo(stream);
            document.getDocumentStructure().setDiagramURL(tmpFile.getAbsolutePath());
        }  finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private void generateTimeseriesMetadata() {
        for (TimeseriesMetadataOutput metadata : getTimeseriesMetadatas()) {
            TimeSeries timeseries = addTimeseries(metadata);
            // addDataTable(timeseries, metadata, data);
            addMetadata(timeseries, metadata);
        }
    }

    @Override
    public void encodeAndWriteTo(OutputStream stream) throws IoParseException {
        try {
            FopFactory fopFactory = FopFactory.newInstance();
            Fop fop = fopFactory.newFop(APPLICATION_PDF.getMimeType(), stream);

            // Create PDF via XSLT transformation
            TransformerFactory transFact = TransformerFactory.newInstance();
            StreamSource transformationRule = getTransforamtionRule();
            Transformer transformer = transFact.newTransformer(transformationRule);

            Source source = new StreamSource(document.newInputStream());
            Result result = new SAXResult(fop.getDefaultHandler());
            transformer.transform(source, result);
        }
        catch (FOPException e) {
            throw new IoParseException("Failed to create Formatting Object Processor (FOP)", e);
        }
        catch (TransformerConfigurationException e) {
            throw new IoParseException("Invalid transform configuration. Inspect xslt!", e);
        }
        catch (TransformerException e) {
            throw new IoParseException("Could not generate PDF report!", e);
        } finally {
            try {
                stream.flush();
                stream.close();
            } catch(IOException e) {
                LOGGER.debug("Stream already flushed and closed.", e);
            }
        }
    }

    private StreamSource getTransforamtionRule() {
        String rules = PDF_TRANSORMATION_RULES.replace(LOCALE_REPLACER, i18n.getTwoDigitsLanguageCode());
        return new StreamSource(getClass().getResourceAsStream("/" + rules));
    }

    private TimeSeries addTimeseries(TimeseriesMetadataOutput metadata) {
        DocumentStructureType report = document.getDocumentStructure();
        TimeSeries timeseries = report.addNewTimeSeries();

        TimeseriesOutput parameters = metadata.getParameters();
        timeseries.setFeatureOfInterestID(parameters.getFeature().getLabel());
        timeseries.setPhenomenID(parameters.getPhenomenon().getLabel());
        timeseries.setProcedureID(parameters.getProcedure().getLabel());
        return timeseries;
    }

    private MetadataType addMetadata(TimeSeries timeseries, TimeseriesMetadataOutput timeseriesMetadata) {
        MetadataType metadata = timeseries.addNewMetadata();
        GenericMetadataPair infoPair = metadata.addNewGenericMetadataPair();

        // if (attributeVal.equals("urn:ogc:identifier:stationName")) {
        //            name = "Station"; //$NON-NLS-1$
        // }
        //
        // if (attributeVal.equals("urn:ogc:identifier:operator")) {
        //            name = "Operator"; //$NON-NLS-1$
        // }
        //
        // if (attributeVal.equals("urn:ogc:identifier:stationID")) {
        //            name = "ID"; //$NON-NLS-1$
        // }
        //
        // if (attributeVal.equals("urn:ogc:identifier:sensorType")) {
        //            name = "Sensor"; //$NON-NLS-1$
        // }

        return metadata;

    }

    private void addDataTable(TimeSeries timeseries,
                              TimeseriesMetadataOutput metadata,
                              TvpDataCollection dataCollection) {
        TableType dataTable = timeseries.addNewTable();

        // TODO add language context

        dataTable.setLeftColHeader("Date");
        dataTable.setRightColHeader(createValueTableHeader(metadata));

        TimeseriesData data = dataCollection.getTimeseries(metadata.getId());
        for (TimeseriesValue valueEntry : data.getValues()) {
            Entry entry = dataTable.addNewEntry();
            entry.setTime(new DateTime(valueEntry.getTimestamp()).toString());
            entry.setValue(Double.toString(valueEntry.getValue()));
        }
    }

    private String createValueTableHeader(TimeseriesMetadataOutput metadata) {
        TimeseriesOutput parameters = metadata.getParameters();
        String phenomenon = parameters.getPhenomenon().getLabel();
        return phenomenon + " (" + metadata.getUom() + ")";
    }

}
