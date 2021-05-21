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

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.n52.io.Constants;
import org.n52.io.HrefHelper;
import org.n52.io.I18N;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.ParameterOutput;
import org.n52.series.spi.srv.CountingMetadataService;
import org.n52.series.spi.srv.ParameterService;
import org.n52.series.spi.srv.RawFormats;
import org.n52.web.ctrl.ResourcesController.ResourceCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping(method = RequestMethod.GET)
public abstract class ParameterRequestMappingAdapter<T extends ParameterOutput> extends ParameterController<T>
        implements ResoureControllerConstants {

    private final CountingMetadataService counter;

    @Autowired
    public ParameterRequestMappingAdapter(CountingMetadataService counter, ParameterService<T> parameterService) {
        super(parameterService);
        this.counter = counter;
    }

    @Override
    @RequestMapping(path = "", produces = Constants.APPLICATION_JSON)
    public ModelAndView getCollection(HttpServletResponse response,
                                      @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                          required = false) String httpLocale,
                                      @RequestParam MultiValueMap<String, String> query) {
        return super.getCollection(response, httpLocale, addAdditionalParameter(query));
    }

    @Override
    @RequestMapping(value = "/{item}", produces = Constants.APPLICATION_JSON)
    public ModelAndView getItem(@PathVariable("item") String id,
                                @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                    required = false) String httpLocale,
                                @RequestParam MultiValueMap<String, String> query,
                                HttpServletResponse response) {
        return super.getItem(id, httpLocale, addAdditionalParameter(query), response);
    }

    @Override
    @RequestMapping(value = "/{item}", produces = Constants.APPLICATION_JSON, params = {
                                                                                        RawFormats.RAW_FORMAT
    })
    public void getRawData(HttpServletResponse response,
                           @PathVariable("item") String id,
                           @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                               required = false) String httpLocale,
                           @RequestParam MultiValueMap<String, String> query) {
        super.getRawData(response, id, httpLocale, addAdditionalParameter(query));
    }

    @Override
    @RequestMapping(value = "/{item}/extras", produces = Constants.APPLICATION_JSON)
    public Map<String, Object> getExtras(HttpServletResponse response,
                                         @PathVariable("item") String resourceId,
                                         @RequestHeader(value = Parameters.HttpHeader.ACCEPT_LANGUAGE,
                                             required = false) String httpLocale,
                                         @RequestParam(required = false) MultiValueMap<String, String> query) {
        return super.getExtras(response, resourceId, httpLocale, addAdditionalParameter(query));
    }

    @Override
    protected MultiValueMap<String, String> addAdditionalParameter(MultiValueMap<String, String> query) {
        return addHrefBase(query);
    }

    protected CountingMetadataService getEntityCounter() {
        return counter;
    }

    @Override
    protected Long getElementCount(IoParameters parameters) {
        return getEntityCounter().getServiceCount(parameters);
    }

    public ResourceCollection getResourceCollection(I18N i18n, IoParameters parameters) {
        ResourceCollection resourceCollection = ResourceCollection.createResource(getResource())
                .withDescription(getDescription(i18n)).withLabel(getLabel()).withHref(getHref(parameters));
        if (parameters.isExpanded()) {
            resourceCollection.setSize(getSize(parameters));
        }
        return resourceCollection;
    }

    protected abstract String getResource();

    protected abstract String getLabel();

    protected abstract String getDescription(I18N i18n);

    protected  String getHref(IoParameters parameters) {
        return HrefHelper.constructHref(parameters.getHrefBase(), getResource());
    }

    protected abstract Long getSize(IoParameters parameters);

    protected Long countDatasets(IoParameters parameters, String datasetType) {
        String filterName = IoParameters.FILTER_DATASET_TYPES;
        IoParameters filter = parameters.extendWith(filterName, datasetType);
        return getEntityCounter().getDatasetCount(filter);
    }
}
