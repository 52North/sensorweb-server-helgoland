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
package org.n52.io.measurement.report;

import static java.io.File.createTempFile;
import static org.n52.io.MimeType.APPLICATION_PDF;
import static org.n52.io.MimeType.IMAGE_PNG;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.n52.io.IoParseException;
import org.n52.io.measurement.TvpDataCollection;
import org.n52.io.measurement.img.ChartRenderer;
import org.n52.io.response.TimeseriesMetadataOutput;
import org.n52.io.response.series.MeasurementData;
import org.n52.io.response.series.MeasurementSeriesOutput;
import org.n52.io.response.series.MeasurementValue;
import org.n52.io.response.series.SeriesDataCollection;
import org.n52.io.response.series.SeriesParameters;
import org.n52.oxf.DocumentStructureDocument;
import org.n52.oxf.DocumentStructureType;
import org.n52.oxf.DocumentStructureType.TimeSeries;
import org.n52.oxf.MetadataType;
import org.n52.oxf.MetadataType.GenericMetadataPair;
import org.n52.oxf.TableType;
import org.n52.oxf.TableType.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class PDFReportGenerator extends ReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(PDFReportGenerator.class);

    private static final String TEMP_FILE_PREFIX = "52n_swc_";

    private static final String LOCALE_REPLACER = "{locale}";

    private static final String PDF_TRANSORMATION_RULES = "pdf/Document_2_PDF_" + LOCALE_REPLACER + ".xslt";

    private final DocumentStructureDocument document;

    private final ChartRenderer renderer;

    private URI baseURI;

    public PDFReportGenerator(ChartRenderer renderer, String locale) {
        super(renderer.getRenderingContext(), locale);
        this.document = DocumentStructureDocument.Factory.newInstance();
        this.document.addNewDocumentStructure();
        this.renderer = configureRenderer(renderer);
    }

    public void setBaseURI(URI baseURI) {
        this.baseURI = baseURI;
    }

    private ChartRenderer configureRenderer(ChartRenderer renderer) {
        renderer.setMimeType(IMAGE_PNG);
        renderer.setShowTooltips(false);
        renderer.setDrawLegend(true);
        return renderer;
    }

    @Override
    public void generateOutput(SeriesDataCollection<MeasurementData> data) throws IoParseException {
        try {
            generateTimeseriesChart(data);
            generateTimeseriesMetadata();
        } catch (IOException e) {
            throw new IoParseException("Error handling (temp) file!", e);
        }
    }

    private void generateTimeseriesChart(SeriesDataCollection<MeasurementData> data) throws IOException {
        renderer.generateOutput(data);
        File tmpFile = createTempFile(TEMP_FILE_PREFIX, "_chart.png");
        try (FileOutputStream stream = new FileOutputStream(tmpFile)){
            renderer.encodeAndWriteTo(stream);
            document.getDocumentStructure().setDiagramURL(tmpFile.getAbsolutePath());
//            String absoluteFilePath = getFoAbsoluteFilepath(tmpFile);
//            document.getDocumentStructure().setDiagramURL(absoluteFilePath);
            stream.flush();
        }
    }

    private String getFoAbsoluteFilepath(File tmpFile) {
        return tmpFile.toURI().toString();
    }

    private void generateTimeseriesMetadata() {
        for (MeasurementSeriesOutput metadata : getSeriesMetadatas()) {
            TimeSeries timeseries = addTimeseries(metadata);
            // addDataTable(timeseries, metadata, data);
            addMetadata(timeseries, metadata);
        }
    }

    @Override
    public void encodeAndWriteTo(OutputStream stream) throws IoParseException {
        try {
            DefaultConfigurationBuilder cfgBuilder = new DefaultConfigurationBuilder();
            Configuration cfg = cfgBuilder.build(document.newInputStream());
            FopFactory fopFactory = new FopFactoryBuilder(baseURI)
                    .setConfiguration(cfg)
                    .build();
            Fop fop = fopFactory.newFop(APPLICATION_PDF.getMimeType(), stream);

//            FopFactory fopFactory = FopFactory.newInstance(cfg);
//            Fop fop = fopFactory.newFop(APPLICATION_PDF.getMimeType(), stream);
//            FopFactory fopFactory = fopFactoryBuilder.build();
//            Fop fop = fopFactory.newFop(APPLICATION_PDF.getMimeType(), stream);
            // Create PDF via XSLT transformation
            TransformerFactory transFact = TransformerFactory.newInstance();
            StreamSource transformationRule = getTransforamtionRule();
            Transformer transformer = transFact.newTransformer(transformationRule);

            Source source = new StreamSource(document.newInputStream());
            Result result = new SAXResult(fop.getDefaultHandler());
            if (LOGGER.isDebugEnabled()) {
                try {
                    File tempFile = createTempFile(TEMP_FILE_PREFIX, ".xml");
                    StreamResult debugResult = new StreamResult(tempFile);
                    transformer.transform(source, debugResult);
                    String xslResult = XmlObject.Factory.parse(tempFile).xmlText();
                    LOGGER.debug("xsl-fo input (locale '{}'): {}", i18n.getTwoDigitsLanguageCode(), xslResult);
                } catch (Exception e) {
                    LOGGER.error("Could not debug XSL result output!", e);
                }
            }

            // XXX debug, diagram is not embedded
            transformer.transform(source, result);
        } catch (FOPException e) {
            throw new IoParseException("Failed to create Formatting Object Processor (FOP)", e);
        } catch (SAXException | ConfigurationException | IOException e) {
            throw new IoParseException("Failed to read config for Formatting Object Processor (FOP)", e);
        } catch (TransformerConfigurationException e) {
            throw new IoParseException("Invalid transform configuration. Inspect xslt!", e);
        } catch (TransformerException e) {
            throw new IoParseException("Could not generate PDF report!", e);
        }
    }

    private StreamSource getTransforamtionRule() {
        String rules = PDF_TRANSORMATION_RULES.replace(LOCALE_REPLACER, i18n.getTwoDigitsLanguageCode());
        return new StreamSource(getClass().getResourceAsStream("/" + rules));
    }

    private TimeSeries addTimeseries(MeasurementSeriesOutput metadata) {
        DocumentStructureType report = document.getDocumentStructure();
        TimeSeries timeseries = report.addNewTimeSeries();

        SeriesParameters parameters = metadata.getSeriesParameters();
        timeseries.setFeatureOfInterestID(parameters.getFeature().getLabel());
        timeseries.setPhenomenID(parameters.getPhenomenon().getLabel());
        timeseries.setProcedureID(parameters.getProcedure().getLabel());
        return timeseries;
    }

    private MetadataType addMetadata(TimeSeries timeseries, MeasurementSeriesOutput timeseriesMetadata) {
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

        MeasurementData data = dataCollection.getSeries(metadata.getId());
        for (MeasurementValue valueEntry : data.getValues()) {
            Entry entry = dataTable.addNewEntry();
            entry.setTime(new DateTime(valueEntry.getTimestamp()).toString());
            entry.setValue(Double.toString(valueEntry.getValue()));
        }
    }

    private String createValueTableHeader(TimeseriesMetadataOutput metadata) {
        SeriesParameters parameters = metadata.getSeriesParameters();
        String phenomenon = parameters.getPhenomenon().getLabel();
        return phenomenon + " (" + metadata.getUom() + ")";
    }

}
