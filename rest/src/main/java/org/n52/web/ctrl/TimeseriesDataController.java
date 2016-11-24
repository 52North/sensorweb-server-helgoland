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
package org.n52.web.ctrl;

import static org.n52.io.MimeType.APPLICATION_ZIP;
import static org.n52.io.MimeType.TEXT_CSV;
import static org.n52.io.request.IoParameters.createFromQuery;
import static org.n52.io.request.QueryParameters.createFromQuery;
import static org.n52.io.request.RequestSimpleParameterSet.createForSingleSeries;
import static org.n52.io.request.RequestSimpleParameterSet.createFromDesignedParameters;
import static org.n52.series.spi.srv.GeneralizingMeasurementDataService.composeDataService;
import static org.n52.web.common.Stopwatch.startStopwatch;
import static org.n52.web.ctrl.UrlSettings.COLLECTION_TIMESERIES;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.n52.io.DatasetFactoryException;
import org.n52.io.DefaultIoFactory;
import org.n52.io.IntervalWithTimeZone;
import org.n52.io.IoFactory;
import org.n52.io.MimeType;
import org.n52.io.PreRenderingJob;
import org.n52.io.measurement.format.FormatterFactory;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestParameterSet;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.response.dataset.DataCollection;
import org.n52.io.response.dataset.measurement.MeasurementData;
import org.n52.io.response.dataset.measurement.MeasurementDatasetOutput;
import org.n52.io.response.dataset.measurement.MeasurementValue;
import org.n52.io.v1.data.RawFormats;
import org.n52.series.spi.srv.DataService;
import org.n52.series.spi.srv.ParameterService;
import org.n52.web.common.Stopwatch;
import org.n52.web.exception.BadRequestException;
import org.n52.web.exception.InternalServerException;
import org.n52.web.exception.ResourceNotFoundException;
import org.n52.web.exception.WebExceptionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@Deprecated
@RestController
@RequestMapping(value = COLLECTION_TIMESERIES, produces = {"application/json"})
public class TimeseriesDataController extends BaseController {

    private final static Logger LOGGER = LoggerFactory.getLogger(TimeseriesDataController.class);

    private ParameterService<MeasurementDatasetOutput> timeseriesMetadataService;

    private DataService<MeasurementData> timeseriesDataService;

    private PreRenderingJob preRenderingTask;

    private String requestIntervalRestriction;

    @RequestMapping(value = "/getData", produces = {"application/json"}, method = POST)
    public ModelAndView getTimeseriesCollectionData(HttpServletResponse response,
            @RequestBody RequestSimpleParameterSet parameters) throws Exception {

        checkIfUnknownTimeseries(parameters, parameters.getDatasets());
        if (parameters.isSetRawFormat()) {
            getRawTimeseriesCollectionData(response, parameters);
            return null;
        }

        DataCollection<MeasurementData> seriesData = getTimeseriesData(parameters);
        DataCollection<?> formattedDataCollection = format(seriesData, parameters.getFormat());
        return new ModelAndView().addObject(formattedDataCollection.getAllSeries());
    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {"application/json"}, method = GET)
    public ModelAndView getTimeseriesData(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query) {

        IoParameters map = createFromQuery(query);
        checkIfUnknownTimeseries(map, timeseriesId);

        IntervalWithTimeZone timespan = map.getTimespan();
        checkAgainstTimespanRestriction(timespan.toString());
        RequestSimpleParameterSet parameters = createForSingleSeries(timeseriesId, map);
        if (map.getResultTime() != null) {
            parameters.setResultTime(map.getResultTime().toString());
        }

        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        // TODO add paging
        DataCollection<MeasurementData> seriesData = getTimeseriesData(parameters);
        DataCollection<?> formattedDataCollection = format(seriesData, map.getFormat());
        if (map.isExpanded()) {
            return new ModelAndView().addObject(formattedDataCollection.getAllSeries());
        }
        Object formattedTimeseries = formattedDataCollection.getAllSeries().get(timeseriesId);
        return new ModelAndView().addObject(formattedTimeseries);
    }

    @RequestMapping(value = "/getData", method = POST, params = {RawFormats.RAW_FORMAT})
    public void getRawTimeseriesCollectionData(HttpServletResponse response, @RequestBody RequestSimpleParameterSet parameters) throws Exception {
        checkIfUnknownTimeseries(parameters, parameters.getDatasets());
        if (!timeseriesDataService.supportsRawData()) {
            throw new BadRequestException("Querying of raw timeseries data is not supported by the underlying service!");
        }

        try (InputStream inputStream = timeseriesDataService.getRawDataService().getRawData(parameters)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("No raw data found.");
            }
            IOUtils.copyLarge(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new InternalServerException("Error while querying raw data", e);
        }
    }

    @RequestMapping(value = "/{timeseriesId}/getData", method = GET, params = {RawFormats.RAW_FORMAT})
    public void getRawTimeseriesData(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @RequestParam MultiValueMap<String, String> query) {
        IoParameters map = createFromQuery(query);
        checkIfUnknownTimeseries(map, timeseriesId);
        RequestSimpleParameterSet parameters = createForSingleSeries(timeseriesId, map);
        if (!timeseriesDataService.supportsRawData()) {
            throw new BadRequestException("Querying of raw procedure data is not supported by the underlying service!");
        }
        try (InputStream inputStream = timeseriesDataService.getRawDataService().getRawData(parameters)) {
            if (inputStream == null) {
                throw new ResourceNotFoundException("No raw data found for id '" + timeseriesId + "'.");
            }
            IOUtils.copyLarge(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new InternalServerException("Error while querying raw data", e);
        }
    }

    private DataCollection<?> format(DataCollection<MeasurementData> timeseriesData, String format) {
        return FormatterFactory.createFormatterFactory(format)
                .create()
                .format(timeseriesData);
    }

    @RequestMapping(value = "/getData", produces = {"application/pdf"}, method = POST)
    public void getTimeseriesCollectionReport(HttpServletResponse response,
            @RequestBody RequestStyledParameterSet requestParameters) throws Exception {

        IoParameters map = createFromQuery(requestParameters);
        checkIfUnknownTimeseries(map, requestParameters.getDatasets());

        RequestSimpleParameterSet parameters = createFromDesignedParameters(requestParameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        response.setContentType(MimeType.APPLICATION_PDF.getMimeType());
        createIoFactory(parameters)
                .withStyledRequest(requestParameters)
                .createHandler(MimeType.APPLICATION_PDF.getMimeType())
                .writeBinary(response.getOutputStream());
    }

    private IoFactory<MeasurementData, MeasurementDatasetOutput, MeasurementValue> createIoFactory(RequestSimpleParameterSet parameters)
            throws DatasetFactoryException, URISyntaxException, MalformedURLException {
        return new DefaultIoFactory<MeasurementData, MeasurementDatasetOutput, MeasurementValue>()
                .create("measurement")
                .withSimpleRequest(parameters)
                .withBasePath(getRootResource())
                .withDataService(timeseriesDataService)
                .withDatasetService(timeseriesMetadataService);
    }

    private URI getRootResource() throws URISyntaxException, MalformedURLException {
        return getServletConfig().getServletContext().getResource("/").toURI();
    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {"application/pdf"}, method = GET)
    public void getTimeseriesReport(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {

        IoParameters map = createFromQuery(query);
        checkIfUnknownTimeseries(map, timeseriesId);

        MeasurementDatasetOutput metadata = timeseriesMetadataService.getParameter(timeseriesId, map);
        RequestSimpleParameterSet parameters = createForSingleSeries(timeseriesId, map);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());

        response.setContentType(MimeType.APPLICATION_PDF.getMimeType());
        createIoFactory(parameters)
                .createHandler(MimeType.APPLICATION_PDF.getMimeType())
                .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {"application/zip"}, method = GET)
    public void getTimeseriesAsZippedCsv(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {
        query.put("zip", Arrays.asList(new String[]{Boolean.TRUE.toString()}));

        response.setContentType(MimeType.APPLICATION_ZIP.getMimeType());
        getTimeseriesAsCsv(response, timeseriesId, query);
    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {"text/csv"}, method = GET)
    public void getTimeseriesAsCsv(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {

        IoParameters map = createFromQuery(query);
        checkIfUnknownTimeseries(map, timeseriesId);

        MeasurementDatasetOutput metadata = timeseriesMetadataService.getParameter(timeseriesId, map);
        RequestSimpleParameterSet parameters = createForSingleSeries(timeseriesId, map);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());


        response.setCharacterEncoding("UTF-8");
        if (Boolean.parseBoolean(map.getOther("zip"))) {
            response.setContentType(APPLICATION_ZIP.toString());
        } else {
            response.setContentType(TEXT_CSV.toString());
        }

        createIoFactory(parameters)
                .createHandler("text/csv")
                .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/getData", produces = {"image/png"}, method = POST)
    public void getTimeseriesCollectionChart(HttpServletResponse response,
            @RequestBody RequestStyledParameterSet requestParameters) throws Exception {

        IoParameters map = createFromQuery(requestParameters);
        checkIfUnknownTimeseries(map, requestParameters.getDatasets());

        RequestSimpleParameterSet parameters = createFromDesignedParameters(requestParameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());
        parameters.setExpanded(map.isExpanded());
        parameters.setBase64(map.isBase64());

        response.setContentType(MimeType.IMAGE_PNG.getMimeType());
        createIoFactory(parameters)
                .withStyledRequest(requestParameters)
                .createHandler("image/png")
                .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {"image/png"}, method = GET)
    public void getTimeseriesChart(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {

        IoParameters map = createFromQuery(query);
        checkIfUnknownTimeseries(map, timeseriesId);

        RequestSimpleParameterSet parameters = createForSingleSeries(timeseriesId, map);
        RequestStyledParameterSet styledParameters = map.toRequestStyledParameterSet();
        checkAgainstTimespanRestriction(parameters.getTimespan());

        parameters.setGeneralize(map.isGeneralize());
        parameters.setBase64(map.isBase64());
        parameters.setExpanded(map.isExpanded());

        response.setContentType(MimeType.IMAGE_PNG.getMimeType());
        createIoFactory(parameters)
                .withStyledRequest(styledParameters)
                .createHandler("image/png")
                .writeBinary(response.getOutputStream());
    }

    @RequestMapping(value = "/{timeseriesId}/{chartQualifier}", produces = {"image/png"}, method = GET)
    public void getTimeseriesChartByInterval(HttpServletResponse response,
            @PathVariable String timeseriesId,
            @PathVariable String chartQualifier,
            @RequestParam(required = false) MultiValueMap<String, String> query) throws Exception {
        if (preRenderingTask == null) {
            throw new ResourceNotFoundException("Diagram prerendering is not enabled.");
        }
        if (!preRenderingTask.hasPrerenderedImage(timeseriesId, chartQualifier)) {
            throw new ResourceNotFoundException("No pre-rendered chart found for timeseries '" + timeseriesId + "'.");
        }
        preRenderingTask.writePrerenderedGraphToOutputStream(timeseriesId, chartQualifier, response.getOutputStream());
    }

    private void checkAgainstTimespanRestriction(String timespan) {
        Duration duration = Period.parse(requestIntervalRestriction).toDurationFrom(new DateTime());
        if (duration.getMillis() < Interval.parse(timespan).toDurationMillis()) {
            throw new BadRequestException("Requested timespan is to long, please use a period shorter than '"
                    + requestIntervalRestriction + "'");
        }
    }

    private void checkIfUnknownTimeseries(RequestParameterSet parameters, String... timeseriesIds) {
        checkIfUnknownTimeseries(IoParameters.createFromQuery(parameters), timeseriesIds);
    }

    private void checkIfUnknownTimeseries(IoParameters parameters, String... timeseriesIds) {
        for (String timeseriesId : timeseriesIds) {
            if (!timeseriesMetadataService.exists(timeseriesId, parameters)) {
                throw new ResourceNotFoundException("The timeseries with id '" + timeseriesId + "' was not found.");
            }
        }
    }

//    /**
//     * @param response the response to write binary on.
//     * @param parameters the timeseries parameter to request raw data.
//     * @param renderer an output renderer.
//     * @throws InternalServerException if data processing fails for some reason.
//     */
//    private void handleBinaryResponse(HttpServletResponse response,
//            RequestSimpleParameterSet parameters,
//            IoHandler<MeasurementData> renderer) {
//        try {
//            renderer.generateOutput(getTimeseriesData(parameters));
//            if (parameters.isBase64()) {
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                renderer.encodeAndWriteTo(baos);
//                byte[] imageData = baos.toByteArray();
//                byte[] encode = Base64.encodeBase64(imageData);
//                response.getOutputStream().write(encode);
//            } else {
//                renderer.encodeAndWriteTo(response.getOutputStream());
//            }
//        } catch (IOException e) { // handled by BaseController
//            throw new InternalServerException("Error handling output stream.", e);
//        } catch (IoParseException e) { // handled by BaseController
//            throw new InternalServerException("Could not write binary to stream.", e);
//        } finally {
//            try {
//                if ( !response.isCommitted()) {
//                    response.flushBuffer();
//                }
//            } catch (IOException e) {
//                throw new InternalServerException("Could not flush buffer.", e);
//            }
//        }
//    }

    private DataCollection<MeasurementData> getTimeseriesData(RequestSimpleParameterSet parameters) {
        Stopwatch stopwatch = startStopwatch();
        DataCollection<MeasurementData> timeseriesData = parameters.isGeneralize()
                ? composeDataService(timeseriesDataService).getData(parameters)
                : timeseriesDataService.getData(parameters);
        LOGGER.debug("Processing request took {} seconds.", stopwatch.stopInSeconds());
        return timeseriesData;
    }

    public ParameterService<MeasurementDatasetOutput> getTimeseriesMetadataService() {
        return timeseriesMetadataService;
    }

    public void setTimeseriesMetadataService(ParameterService<MeasurementDatasetOutput> timeseriesMetadataService) {
        this.timeseriesMetadataService = new WebExceptionAdapter<>(timeseriesMetadataService);
    }

    public DataService<MeasurementData> getTimeseriesDataService() {
        return timeseriesDataService;
    }

    public void setTimeseriesDataService(DataService<MeasurementData> timeseriesDataService) {
        this.timeseriesDataService = timeseriesDataService;
    }

    public PreRenderingJob getPreRenderingTask() {
        return preRenderingTask;
    }

    public void setPreRenderingTask(PreRenderingJob prerenderingTask) {
        this.preRenderingTask = prerenderingTask;
    }

    public String getRequestIntervalRestriction() {
        return requestIntervalRestriction;
    }

    public void setRequestIntervalRestriction(String requestIntervalRestriction) {
        // validate requestIntervalRestriction, if it's no period an exception occured
        Period.parse(requestIntervalRestriction);
        this.requestIntervalRestriction = requestIntervalRestriction;
    }

}
