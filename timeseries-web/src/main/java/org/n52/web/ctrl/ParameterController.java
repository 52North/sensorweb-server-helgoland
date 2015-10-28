/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.web.ctrl;

import org.n52.web.common.Stopwatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.n52.io.ConfigApplier;
import static org.n52.web.common.Stopwatch.startStopwatch;

import org.n52.io.request.IoParameters;
import static org.n52.io.request.QueryParameters.createFromQuery;
import org.n52.io.response.ParameterOutput;
import org.n52.web.exception.ResourceNotFoundException;
import org.n52.sensorweb.spi.LocaleAwareSortService;
import org.n52.sensorweb.spi.ParameterService;
import org.n52.sensorweb.spi.ServiceParameterService;
import org.n52.web.exception.WebExceptionAdapter;
import org.n52.io.extension.MetadataExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping(produces = {"application/json"})
public abstract class ParameterController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterController.class);

    private List<MetadataExtension<ParameterOutput>> metadataExtensions = new ArrayList<>();

    private List<ConfigApplier<ParameterOutput>> configAppliers = new ArrayList<>();

    private ServiceParameterService serviceParameterService;

    private ParameterService<ParameterOutput> parameterService;
    @RequestMapping(value = "/{item}/extras", method = GET)
    public Map<String, Object> getExtras(@PathVariable("item") String timeseriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query) {
        IoParameters queryMap = createFromQuery(query);
        Map<String, Object> extras = new HashMap<>();
        for (MetadataExtension<?> extension : metadataExtensions) {
            final Map<String, Object> furtherExtras = extension.getData(queryMap, timeseriesId);
            Collection<String> overridableKeys = checkForOverridingData(extras, furtherExtras);
            if ( !overridableKeys.isEmpty()) {
                String[] keys = overridableKeys.toArray(new String[0]);
                LOGGER.warn("Metadata extension overrides existing extra data: {}", Arrays.toString(keys));
            }
            extras.putAll(furtherExtras);
        }
        return extras;
    }

    private Collection<String> checkForOverridingData(Map<String, Object> data, Map<String, Object> dataToAdd) {
        Map<String, Object> currentData = new HashMap<>(data);
        Set<String> overridableKeys = currentData.keySet();
        overridableKeys.retainAll(dataToAdd.keySet());
        return overridableKeys;
    }

    @RequestMapping(method = GET)
    public ModelAndView getCollection(@RequestParam(required=false) MultiValueMap<String, String> query) {
        IoParameters queryMap = createFromQuery(query);

        if (queryMap.isExpanded()) {
            Stopwatch stopwatch = startStopwatch();
            ParameterOutput[] result = doPostProcessOn(parameterService.getExpandedParameters(queryMap));
            LOGGER.debug("Processing request took {} seconds.", stopwatch.stopInSeconds());

            // TODO add paging

            return new ModelAndView().addObject(result);
        }
        else {
            ParameterOutput[] results = parameterService.getCondensedParameters(queryMap);

            // TODO add paging

            return new ModelAndView().addObject(results);
        }
    }

    @RequestMapping(value = "/{item}", method = GET)
    public ModelAndView getItem(@PathVariable("item") String id,
                                @RequestParam(required = false) MultiValueMap<String, String> query) {
        IoParameters queryMap = createFromQuery(query);
        ParameterOutput parameter = doPostProcessOn(parameterService.getParameter(id, queryMap));

        if (parameter == null) {
            throw new ResourceNotFoundException("Found no parameter for id '" + id + "'.");
        }

        return new ModelAndView().addObject(parameter);
    }

    protected ParameterOutput[] doPostProcessOn(ParameterOutput[] toBeProcessed) {

        for (ParameterOutput parameterOutput : toBeProcessed) {
            doPostProcessOn(parameterOutput);
        }

        return toBeProcessed;
    }

    protected ParameterOutput doPostProcessOn(ParameterOutput toBeProcessed) {
        for (ConfigApplier<ParameterOutput> applier : configAppliers) {
            applier.applyConfigOn(toBeProcessed);
        }
        return toBeProcessed;
    }


    public ServiceParameterService getServiceParameterService() {
        return serviceParameterService;
    }

    public void setServiceParameterService(ServiceParameterService serviceParameterService) {
        this.serviceParameterService = serviceParameterService;
    }

    public ParameterService<ParameterOutput> getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService<ParameterOutput> parameterService) {
        ParameterService<ParameterOutput> service = new WebExceptionAdapter<>(parameterService);
        this.parameterService = new LocaleAwareSortService<>(service);
    }

    public List<ConfigApplier<ParameterOutput>> getConfigAppliers() {
        return configAppliers;
    }

    public void setConfigAppliers(List<ConfigApplier<ParameterOutput>> configAppliers) {
        this.configAppliers = configAppliers;
    }

    public List<MetadataExtension<ParameterOutput>> getMetadataExtensions() {
        return metadataExtensions;
    }

    public void setMetadataExtensions(List<MetadataExtension<ParameterOutput>> metadataExtensions) {
        this.metadataExtensions = metadataExtensions;
    }
}
