/*
 * Copyright (C) 2013-2021 52°North Initiative for Geospatial Open Source
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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javax.servlet.http.HttpServletResponse;

import org.n52.io.I18N;
import org.n52.io.extension.RenderingHintsExtension;
import org.n52.io.extension.StatusIntervalsExtension;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.request.StyleProperties;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.StatusInterval;
import org.n52.io.response.dataset.TimeseriesMetadataOutput;
import org.n52.io.response.extension.MetadataExtension;
import org.n52.series.spi.srv.CountingMetadataService;
import org.n52.series.spi.srv.ParameterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping(value = UrlSettings.COLLECTION_TIMESERIES)
public class TimeseriesMetadataController extends ParameterRequestMappingAdapter<TimeseriesMetadataOutput> {

    @Autowired
    public TimeseriesMetadataController(CountingMetadataService counter,
                                        ParameterService<TimeseriesMetadataOutput> service) {
        super(counter, service);
    }

    @Override
    protected ModelAndView createModelAndView(OutputCollection<TimeseriesMetadataOutput> items,
                                              IoParameters parameters) {
        items.stream()
             .forEach(e -> {
                 addRenderingHints(e, parameters);
                 addStatusIntervals(e, parameters);
             });
        return super.createModelAndView(items, parameters);
    }

    @Override
    protected ModelAndView createModelAndView(TimeseriesMetadataOutput item, IoParameters parameters) {
        addRenderingHints(item, parameters);
        addStatusIntervals(item, parameters);
        return super.createModelAndView(item, parameters);
    }

    // stay backwards compatible
    @SuppressWarnings("deprecation")
    private TimeseriesMetadataOutput addRenderingHints(TimeseriesMetadataOutput output, IoParameters parameters) {
        final String valueName = TimeseriesMetadataOutput.RENDERING_HINTS;
        Predicate<MetadataExtension<?>> filter = RenderingHintsExtension.class::isInstance;
        Optional<Map<String, Object>> extras = getExtras(output, parameters, filter);
        extras.ifPresent(it -> {
            StyleProperties value = (StyleProperties) it.get(valueName);
            output.setValue(valueName, value, parameters, output::setRenderingHints);
        });
        return output;
    }

    // stay backwards compatible
    @SuppressWarnings(value = {"unchecked", "deprecation"})
    private TimeseriesMetadataOutput addStatusIntervals(TimeseriesMetadataOutput output, IoParameters parameters) {
        final String valueName = TimeseriesMetadataOutput.STATUS_INTERVALS;
        Predicate<MetadataExtension<?>> filter = StatusIntervalsExtension.class::isInstance;
        Optional<Map<String, Object>> extras = getExtras(output, parameters, filter);
        extras.ifPresent(it -> {
            Collection<StatusInterval> value = (Collection<StatusInterval>) it.get(valueName);
            output.setValue(valueName, value, parameters, output::setStatusIntervals);
        });
        return output;
    }

    @Override
    protected IoParameters createParameters(RequestSimpleParameterSet query, String locale,
            HttpServletResponse response) {
        return super.createParameters(query, locale, response).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(RequestStyledParameterSet query, String locale,
            HttpServletResponse response) {
        return super.createParameters(query, locale, response).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(MultiValueMap<String, String> query, String locale,
            HttpServletResponse response) {
        return super.createParameters(query, locale, response).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(String datasetId, MultiValueMap<String, String> query, String locale,
            HttpServletResponse response) {
        return super.createParameters(datasetId, query, locale, response).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(Map<String, String> query, String locale, HttpServletResponse response) {
        return super.createParameters(query, locale, response).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(String datasetId, Map<String, String> query, String locale,
            HttpServletResponse response) {
        return super.createParameters(datasetId, query, locale, response).respectBackwardsCompatibility();
    }

    @Override
    public String getCollectionName() {
        return UrlSettings.COLLECTION_TIMESERIES;
    }

    @SuppressWarnings("unchecked")
    private Optional<Map<String, Object>> getExtras(
                                                    TimeseriesMetadataOutput output,
                                                    IoParameters parameters,
                                                    Predicate<MetadataExtension< ? >> isExtension) {
        return getMetadataExtensions().stream()
                                      .map(x -> (MetadataExtension< ? >) x)
                                      .filter(isExtension)
                                      .map(x -> (MetadataExtension<TimeseriesMetadataOutput>) x)
                                      .map(x -> x.getExtras(output, parameters))
                                      .findFirst();
    }

    @Override
    protected String getResource() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getDescription(I18N i18n) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Long getSize(IoParameters parameters) {
        // TODO Auto-generated method stub
        return null;
    }

}
