/**
 * ?Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.web.v1.ctrl;

import static org.n52.io.MimeType.APPLICATION_PDF;
import static org.n52.io.format.FormatterFactory.createFormatterFactory;
import static org.n52.io.generalize.DouglasPeuckerGeneralizer.createNonConfigGeneralizer;
import static org.n52.io.img.RenderingContext.createContextForSingleTimeseries;
import static org.n52.io.v1.data.UndesignedParameterSet.createForSingleTimeseries;
import static org.n52.io.v1.data.UndesignedParameterSet.createFromDesignedParameters;
import static org.n52.web.v1.ctrl.QueryMap.createFromQuery;
import static org.n52.web.v1.ctrl.RestfulUrls.COLLECTION_TIMESERIES;
import static org.n52.web.v1.ctrl.RestfulUrls.DEFAULT_PATH;
import static org.n52.web.v1.ctrl.Stopwatch.startStopwatch;
import static org.n52.web.v1.srv.GeneralizingTimeseriesDataService.composeDataService;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.n52.io.IOFactory;
import org.n52.io.IOHandler;
import org.n52.io.TimeseriesIOException;
import org.n52.io.format.TimeseriesDataFormatter;
import org.n52.io.format.TvpDataCollection;
import org.n52.io.generalize.Generalizer;
import org.n52.io.img.RenderingContext;
import org.n52.io.v1.data.DesignedParameterSet;
import org.n52.io.v1.data.TimeseriesDataCollection;
import org.n52.io.v1.data.TimeseriesMetadataOutput;
import org.n52.io.v1.data.UndesignedParameterSet;
import org.n52.web.BadRequestException;
import org.n52.web.BaseController;
import org.n52.web.InternalServerException;
import org.n52.web.ResourceNotFoundException;
import org.n52.web.task.PreRenderingTask;
import org.n52.web.v1.srv.ServiceParameterService;
import org.n52.web.v1.srv.TimeseriesDataService;
import org.n52.web.v1.srv.TimeseriesMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = DEFAULT_PATH + "/" + COLLECTION_TIMESERIES, produces = {"application/json"})
public class TimeseriesDataController extends BaseController {

    private final static Logger LOGGER = LoggerFactory.getLogger(TimeseriesDataController.class);

    private ServiceParameterService serviceParameterService;

    private TimeseriesMetadataService timeseriesMetadataService;

    private TimeseriesDataService timeseriesDataService;

    private PreRenderingTask preRenderingTask;

    private String requestIntervalRestriction;

    @RequestMapping(value = "/getData", produces = {"application/json"}, method = POST)
    public ModelAndView getTimeseriesCollectionData(HttpServletResponse response, @RequestBody
    UndesignedParameterSet parameters) throws Exception {

        checkIfUnknownTimeseries(parameters.getTimeseries());

        TvpDataCollection timeseriesData = getTimeseriesData(parameters);
        if (parameters.isGeneralize()) {
            Generalizer generalizer = createNonConfigGeneralizer(timeseriesData);
            timeseriesData = generalizer.generalize();
        }

        TimeseriesDataCollection< ? > formattedDataCollection = format(timeseriesData, parameters.getFormat());
        return new ModelAndView().addObject(formattedDataCollection.getTimeseriesOutput());
    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {"application/json"}, method = GET)
    public ModelAndView getTimeseriesData(HttpServletResponse response, @PathVariable
    String timeseriesId, @RequestParam(required = false)
    MultiValueMap<String, String> query) {

        checkIfUnknownTimeseries(timeseriesId);

        QueryMap map = createFromQuery(query);
        UndesignedParameterSet parameters = createForSingleTimeseries(timeseriesId, map.getTimespan());
        checkAgainstTimespanRestriction(map.getTimespan());

        parameters.setGeneralize(map.isGeneralize());

        // TODO add paging

        TvpDataCollection timeseriesData = getTimeseriesData(parameters);
        TimeseriesDataCollection< ? > formattedDataCollection = format(timeseriesData, map.getFormat());
        Object formattedTimeseries = formattedDataCollection.getAllTimeseries().get(timeseriesId);
        return new ModelAndView().addObject(formattedTimeseries);
    }

    private TimeseriesDataCollection< ? > format(TvpDataCollection timeseriesData, String format) {
        TimeseriesDataFormatter< ? > formatter = createFormatterFactory(format).create();
        return formatter.format(timeseriesData);
    }

    @RequestMapping(value = "/getData", produces = {"application/pdf"}, method = POST)
    public void getTimeseriesCollectionReport(HttpServletResponse response, @RequestBody
    DesignedParameterSet requestParameters) throws Exception {

        checkIfUnknownTimeseries(requestParameters.getTimeseries());

        QueryMap map = createFromQuery(requestParameters);
        UndesignedParameterSet parameters = createFromDesignedParameters(requestParameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());

        String[] timeseriesIds = parameters.getTimeseries();
        TimeseriesMetadataOutput[] timeseriesMetadatas = timeseriesMetadataService.getParameters(timeseriesIds, map);
        RenderingContext context = RenderingContext.createContextWith(requestParameters, timeseriesMetadatas);

        IOHandler renderer = IOFactory.create().forMimeType(APPLICATION_PDF).withLocale(map.getLocale()).createIOHandler(context);

        handleBinaryResponse(response, parameters, renderer);

    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {"application/pdf"}, method = GET)
    public void getTimeseriesReport(HttpServletResponse response, @PathVariable
    String timeseriesId, @RequestParam(required = false)
    MultiValueMap<String, String> query) throws Exception {

        checkIfUnknownTimeseries(timeseriesId);

        QueryMap map = createFromQuery(query);
        TimeseriesMetadataOutput metadata = timeseriesMetadataService.getParameter(timeseriesId, map);
        RenderingContext context = createContextForSingleTimeseries(metadata, map.getStyle(), map.getTimespan());
        UndesignedParameterSet parameters = createForSingleTimeseries(timeseriesId, map.getTimespan());
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());

        IOHandler renderer = IOFactory.create().forMimeType(APPLICATION_PDF).withLocale(map.getLocale()).createIOHandler(context);

        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/getData", produces = {"image/png"}, method = POST)
    public void getTimeseriesCollectionChart(HttpServletResponse response, @RequestBody
    DesignedParameterSet requestParameters) throws Exception {

        checkIfUnknownTimeseries(requestParameters.getTimeseries());

        QueryMap map = createFromQuery(requestParameters);
        UndesignedParameterSet parameters = createFromDesignedParameters(requestParameters);
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());

        String[] timeseriesIds = parameters.getTimeseries();
        TimeseriesMetadataOutput[] timeseriesMetadatas = timeseriesMetadataService.getParameters(timeseriesIds, map);
        RenderingContext context = RenderingContext.createContextWith(requestParameters, timeseriesMetadatas);
        IOHandler renderer = IOFactory.create()
                .withLocale(map.getLocale())
                .showGrid(map.isGrid())
                .withLegend(map.isLegend())
                .createIOHandler(context);

        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/{timeseriesId}/getData", produces = {"image/png"}, method = GET)
    public void getTimeseriesChart(HttpServletResponse response, @PathVariable
    String timeseriesId, @RequestParam(required = false)
    MultiValueMap<String, String> query) throws Exception {

        checkIfUnknownTimeseries(timeseriesId);

        QueryMap map = createFromQuery(query);
        TimeseriesMetadataOutput metadata = timeseriesMetadataService.getParameter(timeseriesId, map);
        RenderingContext context = createContextForSingleTimeseries(metadata, map.getStyle(), map.getTimespan());
        context.setDimensions(map.getWidth(), map.getHeight());

        UndesignedParameterSet parameters = createForSingleTimeseries(timeseriesId, map.getTimespan());
        checkAgainstTimespanRestriction(parameters.getTimespan());
        parameters.setGeneralize(map.isGeneralize());

        parameters.setBase64(map.isBase64());
        IOHandler renderer = IOFactory.create()
                .withLocale(map.getLocale())
                .showGrid(map.isGrid())
                .withLegend(map.isLegend())
                .createIOHandler(context);
        handleBinaryResponse(response, parameters, renderer);
    }

    @RequestMapping(value = "/{timeseriesId}/{interval}", produces = {"image/png"}, method = GET)
    public void getTimeseriesChartByInterval(HttpServletResponse response, @PathVariable
    String timeseriesId, @PathVariable
    String interval, @RequestParam(required = false)
    MultiValueMap<String, String> query) throws Exception {
        // use a configurable pre rendering of some images, so the prerendering task delivers the images
        if (preRenderingTask.hasPrerenderedImage(timeseriesId, interval)) {
            preRenderingTask.writeToOS(timeseriesId, interval, response.getOutputStream());
        }
        else {
            checkIfUnknownTimeseries(timeseriesId);
            QueryMap map = createFromQuery(query);

            String timespan = preRenderingTask.createTimespanFromInterval(timeseriesId, interval);
            TimeseriesMetadataOutput metadata = timeseriesMetadataService.getParameter(timeseriesId, map);
            RenderingContext context = createContextForSingleTimeseries(metadata, map.getStyle(), timespan);
            context.setDimensions(map.getWidth(), map.getHeight());

            UndesignedParameterSet parameters = createForSingleTimeseries(timeseriesId, map.getTimespan());
            checkAgainstTimespanRestriction(parameters.getTimespan());

            parameters.setBase64(map.isBase64());
            IOHandler renderer = IOFactory.create().withLocale(map.getLocale()).showGrid(map.isGrid()).createIOHandler(context);
            handleBinaryResponse(response, parameters, renderer);
        }
    }

    private void checkAgainstTimespanRestriction(String timespan) {
        Duration duration = Period.parse(requestIntervalRestriction).toDurationFrom(new DateTime());
        if (duration.getMillis() < Interval.parse(timespan).toDurationMillis()) {
            throw new BadRequestException("Requested timespan is to long, please use a period shorter than '"
                    + requestIntervalRestriction + "'");
        }
    }

    private void checkIfUnknownTimeseries(String... timeseriesIds) {
        for (String timeseriesId : timeseriesIds) {
            if ( !serviceParameterService.isKnownTimeseries(timeseriesId)) {
                throw new ResourceNotFoundException("The timeseries with id '" + timeseriesId + "' was not found.");
            }
        }
    }

    /**
     * @param response
     *        the response to write binary on.
     * @param parameters
     *        the timeseries parameter to request raw data.
     * @param renderer
     *        an output renderer.
     * @throws InternalServerException
     *         if data processing fails for some reason.
     */
    private void handleBinaryResponse(HttpServletResponse response,
                                      UndesignedParameterSet parameters,
                                      IOHandler renderer) {
        try {
            renderer.generateOutput(getTimeseriesData(parameters));
            if (parameters.isBase64()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                renderer.encodeAndWriteTo(baos);
                byte[] imageData = baos.toByteArray();
                byte[] encode = Base64.encodeBase64(imageData);
                response.getOutputStream().write(encode);
            }
            else {
                renderer.encodeAndWriteTo(response.getOutputStream());
            }
        }
        catch (IOException e) { // handled by BaseController
            throw new InternalServerException("Error handling output stream.", e);
        }
        catch (TimeseriesIOException e) { // handled by BaseController
            throw new InternalServerException("Could not write binary to stream.", e);
        }
    }

    private TvpDataCollection getTimeseriesData(UndesignedParameterSet parameters) {
        Stopwatch stopwatch = startStopwatch();
        TvpDataCollection timeseriesData = parameters.isGeneralize() 
                ? composeDataService(timeseriesDataService).getTimeseriesData(parameters)
                : timeseriesDataService.getTimeseriesData(parameters);
        LOGGER.debug("Processing request took {} seconds.", stopwatch.stopInSeconds());
        return timeseriesData;
    }

    public ServiceParameterService getServiceParameterService() {
        return serviceParameterService;
    }

    public void setServiceParameterService(ServiceParameterService serviceParameterService) {
        this.serviceParameterService = serviceParameterService;
    }

    public TimeseriesMetadataService getTimeseriesMetadataService() {
        return timeseriesMetadataService;
    }

    public void setTimeseriesMetadataService(TimeseriesMetadataService timeseriesMetadataService) {
        this.timeseriesMetadataService = timeseriesMetadataService;
    }

    public TimeseriesDataService getTimeseriesDataService() {
        return timeseriesDataService;
    }

    public void setTimeseriesDataService(TimeseriesDataService timeseriesDataService) {
        this.timeseriesDataService = timeseriesDataService;
    }

    public PreRenderingTask getPreRenderingTask() {
        return preRenderingTask;
    }

    public void setPreRenderingTask(PreRenderingTask prerenderingTask) {
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
