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
package org.n52.web.ctrl;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import org.n52.io.extension.RenderingHintsExtension;
import org.n52.io.extension.StatusIntervalsExtension;
import org.n52.io.request.IoParameters;
import org.n52.io.request.RequestSimpleParameterSet;
import org.n52.io.request.RequestStyledParameterSet;
import org.n52.io.request.StyleProperties;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.StatusInterval;
import org.n52.io.response.dataset.TimeseriesMetadataOutput;
import org.n52.io.response.extension.MetadataExtension;

@Deprecated
@RestController
@RequestMapping(value = UrlSettings.COLLECTION_TIMESERIES)
public class TimeseriesMetadataController extends ParameterRequestMappingAdapter<TimeseriesMetadataOutput> {

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

    private TimeseriesMetadataOutput addRenderingHints(TimeseriesMetadataOutput output, IoParameters parameters) {
        if (parameters.isRenderingHintsRequests()) {
            getMetadataExtensionExtras(output, parameters, RenderingHintsExtension.class).ifPresent(extras
                    -> output.setValue(TimeseriesMetadataOutput.RENDERING_HINTS,
                                       (StyleProperties) extras.get(TimeseriesMetadataOutput.RENDERING_HINTS),
                                       parameters,
                                       output::setRenderingHints));

        }
        return output;
    }

    @SuppressWarnings("unchecked")
    private TimeseriesMetadataOutput addStatusIntervals(TimeseriesMetadataOutput output, IoParameters parameters) {
        if (parameters.isStatusIntervalsRequests()) {
            getMetadataExtensionExtras(output, parameters, StatusIntervalsExtension.class).ifPresent(extras
                    -> output.setValue(TimeseriesMetadataOutput.STATUS_INTERVALS,
                                       (Collection<StatusInterval>) extras
                                               .get(TimeseriesMetadataOutput.STATUS_INTERVALS),
                                       parameters,
                                       output::setStatusIntervals));

        }
        return output;
    }

    @Override
    protected IoParameters createParameters(RequestSimpleParameterSet query, String locale) {
        return super.createParameters(query, locale).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(RequestStyledParameterSet query, String locale) {
        return super.createParameters(query, locale).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(MultiValueMap<String, String> query, String locale) {
        return super.createParameters(query, locale).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(String datasetId, MultiValueMap<String, String> query, String locale) {
        return super.createParameters(datasetId, query, locale).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(Map<String, String> query, String locale) {
        return super.createParameters(query, locale).respectBackwardsCompatibility();
    }

    @Override
    protected IoParameters createParameters(String datasetId, Map<String, String> query, String locale) {
        return super.createParameters(datasetId, query, locale).respectBackwardsCompatibility();
    }

    @Override
    public String getCollectionPath(String hrefBase) {
        UrlHelper urlhelper = new UrlHelper();
        return urlhelper.constructHref(hrefBase, UrlSettings.COLLECTION_TIMESERIES);
    }

    @Override
    protected int getElementCount(IoParameters queryMap) {
        return super.getEntityCounter().getTimeseriesCount();
    }

    @SuppressWarnings("unchecked")
    protected <T extends ParameterOutput> Optional<Map<String, Object>> getMetadataExtensionExtras(
            T output, IoParameters parameters, Class<? extends MetadataExtension<T>> clazz) {
        return getMetadataExtensions().stream()
                .map(x -> (MetadataExtension<?>) x)
                .filter(x -> clazz.isInstance(x))
                .map(x -> (MetadataExtension<T>) x)
                .map(x -> x.getExtras(output, parameters))
                .findFirst();
    }
}
