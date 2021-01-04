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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.extension.MetadataExtension;
import org.n52.series.spi.srv.ParameterService;
import org.n52.web.common.OffsetBasedPagination;
import org.n52.web.common.PageLinkUtil;
import org.n52.web.common.Paginated;
import org.n52.web.common.Stopwatch;
import org.n52.web.exception.BadRequestException;
import org.n52.web.exception.InternalServerException;
import org.n52.web.exception.ResourceNotFoundException;
import org.n52.web.exception.SpiAssertionExceptionAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.ModelAndView;

public abstract class ParameterController<T extends ParameterOutput>
        extends BaseController implements ResourceController, RawDataController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterController.class);

    private List<MetadataExtension<T>> metadataExtensions = new ArrayList<>();

    private final ParameterService<T> parameterService;

    public ParameterController(ParameterService<T> parameterService) {
        this.parameterService = new SpiAssertionExceptionAdapter<>(parameterService);
    }

    @Override
    public void getRawData(HttpServletResponse response,
                           String id,
                           String locale,
                           MultiValueMap<String, String> query) {
        if (!parameterService.supportsRawData()) {
            throw new BadRequestException("Querying raw procedure data is not supported!");
        }

        IoParameters queryMap = createParameters(query, locale, response);
        LOGGER.debug("getRawData() with id '{}' and query '{}'", id, queryMap);

        try (InputStream inputStream = parameterService.getRawDataService()
                                                            .getRawData(id, queryMap)) {
            IOUtils.copyLarge(inputStream, response.getOutputStream());
        } catch (IOException e) {
            throw new InternalServerException("Error while querying raw data", e);
        }
    }

    @Override
    public Map<String, Object> getExtras(HttpServletResponse response, String resourceId, String locale,
            MultiValueMap<String, String> query) {
        IoParameters map = createParameters(query, locale, response);
        LOGGER.debug("getExtras() with id '{}' and query '{}'", resourceId, map);

        Map<String, Object> extras = new HashMap<>();
        T from = parameterService.getParameter(resourceId, map);
        for (MetadataExtension<T> extension : metadataExtensions) {
            final Map<String, Object> furtherExtras = extension.getExtras(from, map);
            Collection<String> overridableKeys = checkForOverridingData(extras, furtherExtras);
            if (!overridableKeys.isEmpty()) {
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

    protected ModelAndView createModelAndView(OutputCollection<T> items, IoParameters parameters) {
        return new ModelAndView()
                .addObject(items.getItems() != null && !items.getItems().isEmpty() ? items.getItems() : new String[0]);
    }

    protected ModelAndView createModelAndView(T item, IoParameters parameters) {
        return new ModelAndView().addObject(item);
    }

    @Override
    public ModelAndView getCollection(HttpServletResponse response,
                                      String locale,
                                      MultiValueMap<String, String> query) {
        Stopwatch stopwatch = Stopwatch.startStopwatch();
        IoParameters parameters = createParameters(query, locale, response);
        try {
            LOGGER.debug("getCollection() with query '{}'", parameters);
            preparePagingHeaders(parameters, response);
            return createModelAndView(getCollection(parameters), parameters);
        } finally {
            LOGGER.debug("Processing request took {} seconds.", stopwatch.stopInSeconds());
        }
    }

    private OutputCollection<T> getCollection(IoParameters parameters) {
        return parameters.isExpanded()
                ? addExtensionInfos(parameterService.getExpandedParameters(parameters), parameters)
                : parameterService.getCondensedParameters(parameters);
    }

    private void preparePagingHeaders(IoParameters parameters, HttpServletResponse response) {
        if (parameters.containsParameter(Parameters.LIMIT) || parameters.containsParameter(Parameters.OFFSET)) {
            Long elementcount = this.getElementCount(parameters.removeAllOf(Parameters.LIMIT)
                                                                  .removeAllOf(Parameters.OFFSET));
            if (elementcount > 0) {
                int limit = parameters.getLimit();
                int offset = parameters.getOffset();
                OffsetBasedPagination obp = new OffsetBasedPagination(offset, limit);
                Paginated paginated = new Paginated(obp, elementcount);
                PageLinkUtil.addPagingHeaders(createCollectionUrl(getCollectionName()), response, paginated);
            }
        }
    }

    @Override
    public ModelAndView getItem(String id, String locale, MultiValueMap<String, String> query,
            HttpServletResponse response) {
        IoParameters parameters = createParameters(query, locale, response);
        LOGGER.debug("getItem() with id '{}' and query '{}'", id, parameters);
        return createModelAndView(getItem(id, parameters), parameters);
    }

    private T getItem(String id, IoParameters parameters) {
        T item = parameterService.getParameter(id, parameters);
        if (item == null) {
            throw new ResourceNotFoundException("Resource with id '" + id + "' not found.");
        }
        return addExtensionInfos(item, parameters);
    }

    protected T addExtensionInfos(T output, IoParameters parameters) {
        Collection<String> extras = metadataExtensions.stream()
                                                      .map(e -> e.getExtraMetadataFieldNames(output))
                                                      .flatMap(c -> c.stream())
                                                      .collect(Collectors.toList());
        output.setValue(ParameterOutput.EXTRAS, extras, parameters, output::setExtras);
        return output;
    }

    private OutputCollection<T> addExtensionInfos(OutputCollection<T> toBeProcessed, IoParameters ioParameters) {
        for (T parameterOutput : toBeProcessed) {
            addExtensionInfos(parameterOutput, ioParameters);
        }
        return toBeProcessed;
    }

    public void addMetadataExtension(MetadataExtension<T> extension) {
        if (metadataExtensions != null) {
            metadataExtensions.add(extension);
        }
    }

    public List<MetadataExtension<T>> getMetadataExtensions() {
        return metadataExtensions;
    }

    public void setMetadataExtensions(List<MetadataExtension<T>> metadataExtensions) {
        this.metadataExtensions = metadataExtensions;
    }

    /**
     * @param queryMap
     *        the query map
     * @return the number of elements available, or negative number if paging is not supported.
     */
    protected abstract Long getElementCount(IoParameters queryMap);

    @Override
    protected void addCacheHeader(IoParameters parameter, HttpServletResponse response) {
        if (parameter.hasCache()
                && parameter.getCache().get().has(getResourcePathFrom(getCollectionName()))) {
            addCacheHeader(response, parameter.getCache().get()
                    .get(getResourcePathFrom(getCollectionName())).asLong(0));
        }
    }

}
