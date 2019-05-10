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
package org.n52.io.request;

import org.n52.series.spi.srv.RawFormats;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public interface Parameters {

    // TODO separate public parameters from internal parameters (e.g. HREF_BASE)

    String SEARCH_TERM = "q";

    /**
     * How detailed the output shall be.
     */
    String EXPANDED = "expanded";

    /**
     * The default expansion of collection items. Refer to {@link #EXPANDED}
     */
    boolean DEFAULT_EXPANDED = false;

    /**
     * If latest values shall be requested in a bulk timeseries request.
     */
    String FORCE_LATEST_VALUE = "force_latest_values";

    /**
     * The default behaviour if latest value requests shall be invoked during a
     * timeseries collection request.
     */
    boolean DEFAULT_FORCE_LATEST_VALUE = false;

    /**
     * Determines the index of the first member of the response page (a.k.a.
     * page offset).
     */
    String OFFSET = "offset";

    /**
     * The default page offset. Refer to {@link #OFFSET}
     */
    int DEFAULT_OFFSET = -1;

    /**
     * Determines the limit of the page to be returned.
     */
    String LIMIT = "limit";

    /**
     * The default page size limit. Refer to {@link #LIMIT}
     */
    int DEFAULT_LIMIT = -1;

    /**
     * Determines the locale the output shall have.
     */
    String LOCALE = "locale";

    /**
     * The default locale. Refer to {@link #LOCALE}
     */
    String DEFAULT_LOCALE = "en";

    /**
     * Determines the timespan parameter
     */
    String TIMESPAN = "timespan";

    /**
     * Determines the timezone output parameter
     */
    String OUTPUT_TIMEZONE = "outputTimezone";

    /**
     * The default output timezone
     */
    String DEFAULT_OUTPUT_TIMEZONE = "UTC";

    /**
     * Parameter to specify data with a result time
     * @deprecated use {@link #RESULTTIMES} instead
     */
    @Deprecated
    String RESULTTIME = "resultTime";

    /**
     * Parameter to specify data for particular result times
     */
    String RESULTTIMES = "resultTimes";

    String RESULT_TIMES_VALUE_ALL = "all";

    /**
     * The width in px of the image to be rendered.
     */
    String WIDTH = "width";

    /**
     * The default width of the chart image to render.
     */
    int DEFAULT_WIDTH = 800;

    /**
     * The height in px of the image to be rendered.
     */
    String HEIGHT = "height";

    /**
     * The default height of the chart image to render.
     */
    int DEFAULT_HEIGHT = 500;

    /**
     * If a chart shall be rendered with a background grid.
     */
    String GRID = "grid";

    /**
     * Defaults to a background grid in a rendered chart.
     */
    boolean DEFAULT_GRID = true;

    /**
     * If a legend shall be drawn on the chart.
     */
    String LEGEND = "legend";

    /**
     * Defaults to a not drawn legend.
     */
    boolean DEFAULT_LEGEND = false;

    /**
     * If a rendered chart shall be written as base64 encoded string.
     */
    String BASE_64 = "base64";

    /**
     * Defaults to binary output.
     */
    boolean DEFAULT_BASE_64 = false;

    /**
     * Determines the generalize flag.
     */
    String GENERALIZE = "generalize";

    /**
     * The default (no generalization) behaviour.
     */
    boolean DEFAULT_GENERALIZE = false;

    /**
     * Determines how raw data shall be queried from service.
     */
    String RAW_FORMAT = RawFormats.RAW_FORMAT;

    /**
     * Determines how raw data shall be formatted.
     */
    String FORMAT = "format";

    /**
     * The default format for raw data output.
     */
    String DEFAULT_FORMAT = "tvp";

    /**
     * Determines how dates should be formatted (in charts).
     */
    String TIME_FORMAT = "timeformat";

    /**
     * The default timeformat.
     */
    String DEFAULT_TIME_FORMAT = "yyyy-MM-dd, HH:mm";

    /**
     * Determines if what event causes a rendering task.
     */
    String RENDERING_TRIGGER = "rendering_trigger";

    /**
     * Default event causing a rendering task.
     */
    String DEFAULT_RENDERING_TRIGGER = "request";

    /**
     * Determines that output shall be zipped (if possible).
     */
    String ZIP = "zip";

    /**
     * Flag to indicate if CSV encoding shall start with a Byte-Order-Mark
     */
    String BOM = "bom";

    /**
     * A token separator to separate CSV values
     */
    String TOKEN_SEPARATOR = "tokenSeparator";

    /**
     * Determines the style parameter for a single dataset
     */
    String STYLE = "style";

    /**
     * Determines the styles parameter for multiple datasets
     */
    String STYLES = "styles";

    /**
     * Determines the services filter
     */
    String SERVICES = "services";

    /**
     * Determines the features filter
     */
    String FEATURES = "features";

    /**
     * Determines the offerings filter
     */
    String OFFERINGS = "offerings";

    /**
     * Determines the procedures filter
     */
    String PROCEDURES = "procedures";

    /**
     * Determines the phenomena filter
     */
    String PHENOMENA = "phenomena";

    /**
     * Determines the stations filter
     */
    String STATIONS = "stations";

    String PLATFORMS = "platforms";

    String TIMESERIES = "timeseries";

    String DATASETS = "datasets";

    String HANDLE_AS_VALUE_TYPE = "handleAs";

    /**
     * Determines the categories filter
     */
    String CATEGORIES = "categories";

    /**
     * Determines the reference system to be used for input/output coordinates.
     */
    String CRS = "crs";

    /**
     * Determines if CRS axes order shall always be XY, i.e. lon/lat.
     */
    String FORCE_XY = "forceXY";

    /**
     * Default axes order respects EPSG axes ordering.
     */
    boolean DEFAULT_FORCE_XY = false;

    /**
     * Determines if filter shall match domain ids instead of global ids
     */
    String MATCH_DOMAIN_IDS = "matchDomainIds";

    /**
     * Default filter match property.
     */
    boolean DEFAULT_MATCH_DOMAIN_IDS = false;

    /**
     * Determines the within filter
     */
    String NEAR = "near";

    /**
     * Determines the bbox filter
     */
    String BBOX = "bbox";

    /**
     * Determines the fields filter
     */
    String FILTER_FIELDS = "fields";

    String FILTER_PLATFORM_TYPES = "platformTypes";

    String FILTER_VALUE_TYPES = "valueTypes";

    String FILTER_PLATFORM_GEOMETRIES = "platformGeometries";

    String FILTER_OBSERVED_GEOMETRIES = "observedGeometries";

    /**
     * SimpleFeature types e.g. POINT, LINESTRING, ...
     */
    String GEOMETRY_TYPES = "geometryTypes";

    /**
     * If observation time shall be shown within intervals.
     */
    String SHOW_TIME_INTERVALS = "showTimeIntervals";

    /**
     * Default for {@link #SHOW_TIME_INTERVALS}
     */
    boolean DEFAULT_SHOW_TIME_INTERVALS = false;

    /**
     * If verticals shall be shown within intervals.
     */
    String SHOW_VERTICAL_INTERVALS = "showVerticalIntervals";

    /**
     * Default for {@link #SHOW_VERTICAL_INTERVALS}
     */
    boolean DEFAULT_SHOW_VERTICAL_INTERVALS = false;

    /*
     *############### INTERNAL CONSTANTS
     */

    /**
     * Internally set via 2.x interface to create href links.
     * Will be overridden, if it has been set externally.
     */
    String HREF_BASE = "internal.href.base";

    /**
     * Internally set to refer to parents in complex observations.
     */
    String COMPLEX_PARENT = "internal.complex.parent";


    /**
     * The OData filter parameter: {@value}.
     */
    String ODATA_FILTER = "$filter";

    /*
     *############### DEPRECATED CONSTANTS
     */

    /**
     * Determines the service filter
     * @deprecated use {@link #SERVICES}
     */
    @Deprecated
    String SERVICE = "service";

    /**
     * Determines the feature filter
     * @deprecated use {@link #FEATURES}
     */
    @Deprecated
    String FEATURE = "feature";

    /**
     * Determines the service filter
     * @deprecated use {@link #OFFERINGS}
     */
    @Deprecated
    String OFFERING = "offering";

    /**
     * Determines the procedure filter
     * @deprecated use {@link #PROCEDURES}
     */
    @Deprecated
    String PROCEDURE = "procedure";

    /**
     * Determines the phenomenon filter
     * @deprecated use {@link #PHENOMENA}
     */
    @Deprecated
    String PHENOMENON = "phenomenon";

    /**
     * Determines the station filter
     * @deprecated since 2.0.0
     */
    @Deprecated
    String STATION = "station";

    /**
     * @deprecated since 2.0.0
     */
    @Deprecated
    String SERIES = "series";

    /**
     * Determines the category filter
     * @deprecated use {@link #CATEGORIES}
     */
    @Deprecated
    String CATEGORY = "category";

    /**
     * If status intervals section is requested.
     * @deprecated since v2.0 covered by extras endpoint
     */
    @Deprecated
    String STATUS_INTERVALS = "status_intervals";

    /**
     * The default behaviour for status intervals.
     * @deprecated since v2.0 covered by extras endpoint
     */
    @Deprecated
    boolean DEFAULT_STATUS_INTERVALS = false;

    /**
     * If rendering hints are requested for a timeseries
     * @deprecated since v2.0 covered by extras endpoint
     */
    @Deprecated
    String RENDERING_HINTS = "rendering_hints";

    /**
     * The default behaviour for rendering hints.
     * @deprecated since v2.0 covered by extras endpoint
     */
    @Deprecated
    boolean DEFAULT_RENDERING_HINTS = false;


    /**
     * Determines the language the output shall have.
     * @deprecated since v2.0 covered by {@link #LOCALE}}
     */
    @Deprecated
    String LANGUAGE = "language";

    interface HttpHeader {
        String ACCEPT_LANGUAGE = "accept-language";
    }

}
