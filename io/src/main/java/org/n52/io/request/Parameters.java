package org.n52.io.request;

import org.n52.io.v1.data.RawFormats;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public interface Parameters {

    public static final String SEARCH_TERM = "q";

    /**
     * How detailed the output shall be.
     */
    public static final String EXPANDED = "expanded";

    /**
     * The default expansion of collection items.
     *
     * @see #EXPANDED
     */
    public static final boolean DEFAULT_EXPANDED = false;

    /**
     * If latest values shall be requested in a bulk timeseries request.
     */
    public static final String FORCE_LATEST_VALUE = "force_latest_values";

    /**
     * The default behaviour if latest value requests shall be invoked during a
     * timeseries collection request.
     */
    public static final boolean DEFAULT_FORCE_LATEST_VALUE = false;

    /**
     * If status intervals section is requested.
     */
    public static final String STATUS_INTERVALS = "status_intervals";

    /**
     * The default behaviour for status intervals.
     */
    public static final boolean DEFAULT_STATUS_INTERVALS = false;

    /**
     * If rendering hints are requested for a timeseries
     */
    public static final String RENDERING_HINTS = "rendering_hints";

    /**
     * The default behaviour for rendering hints.
     */
    public static final boolean DEFAULT_RENDERING_HINTS = false;

    /**
     * Determines the index of the first member of the response page (a.k.a.
     * page offset).
     */
    public static final String OFFSET = "offset";

    /**
     * The default page offset.
     *
     * @see #OFFSET
     */
    public static final int DEFAULT_OFFSET = -1;

    /**
     * Determines the limit of the page to be returned.
     */
    public static final String LIMIT = "limit";

    /**
     * The default page size limit.
     *
     * @see #LIMIT
     */
    public static final int DEFAULT_LIMIT = -1;

    /**
     * Determines the locale the output shall have.
     */
    public static final String LOCALE = "locale";

    /**
     * The default locale.
     *
     * @see #LOCALE
     */
    public static final String DEFAULT_LOCALE = "en";

    /**
     * Determines the timespan parameter
     */
    public static final String TIMESPAN = "timespan";

    /**
     * Parameter to specify the timeseries data with a result time
     */
    public static final String RESULTTIME = "resultTime";

    /**
     * The width in px of the image to be rendered.
     */
    public static final String WIDTH = "width";

    /**
     * The default width of the chart image to render.
     */
    public static final int DEFAULT_WIDTH = 800;

    /**
     * The height in px of the image to be rendered.
     */
    public static final String HEIGHT = "height";

    /**
     * The default height of the chart image to render.
     */
    public static final int DEFAULT_HEIGHT = 500;

    /**
     * If a chart shall be rendered with a background grid.
     */
    public static final String GRID = "grid";

    /**
     * Defaults to a background grid in a rendered chart.
     */
    public static final boolean DEFAULT_GRID = true;

    /**
     * If a legend shall be drawn on the chart.
     */
    public static final String LEGEND = "legend";

    /**
     * Defaults to a not drawn legend.
     */
    public static final boolean DEFAULT_LEGEND = false;

    /**
     * If a rendered chart shall be written as base64 encoded string.
     */
    public static final String BASE_64 = "base64";

    /**
     * Defaults to binary output.
     */
    public static final boolean DEFAULT_BASE_64 = false;

    /**
     * Determines the generalize flag.
     */
    public static final String GENERALIZE = "generalize";

    /**
     * The default (no generalization) behaviour.
     */
    public static final boolean DEFAULT_GENERALIZE = false;

    /**
     * Determines how raw data shall be formatted.
     */
    public static final String FORMAT = "format";

    /**
     * Determines how raw data shall be queried from service.
     */
    public static final String RAW_FORMAT = RawFormats.RAW_FORMAT;

    /**
     * The default format for raw data output.
     */
    public static final String DEFAULT_FORMAT = "tvp";

    /**
     * Determines the style parameter
     */
    public static final String STYLE = "style";

    /**
     * Determines the service filter
     */
    public static final String SERVICE = "service";

    /**
     * Determines the feature filter
     */
    public static final String FEATURE = "feature";

    /**
     * Determines the service filter
     */
    public static final String OFFERING = "offering";

    /**
     * Determines the procedure filter
     */
    public static final String PROCEDURE = "procedure";

    /**
     * Determines the phenomenon filter
     */
    public static final String PHENOMENON = "phenomenon";

    /**
     * Determines the station filter
     */
    public static final String STATION = "station";

    public static final String PLATFORMS = "platforms";

    /**
     * Determines the category filter
     */
    public static final String CATEGORY = "category";

    /**
     * Determines the reference system to be used for input/output coordinates.
     */
    public static final String CRS = "crs";

    /**
     * Determines if CRS axes order shall always be XY, i.e. lon/lat.
     */
    public static final String FORCE_XY = "forceXY";

    /**
     * Default axes order respects EPSG axes ordering.
     */
    public static final boolean DEFAULT_FORCE_XY = false;

    /**
     * Determines if filter shall match domain ids instead of global ids
     */
    public static final String MATCH_DOMAIN_IDS = "matchDomainIds";

    /**
     * Default filter match property.
     */
    public static final boolean DEFAULT_MATCH_DOMAIN_IDS = false;

    /**
     * Determines the within filter
     */
    public static final String NEAR = "near";

    /**
     * Determines the bbox filter
     */
    public static final String BBOX = "bbox";

    /**
     * Determines the fields filter
     */
    public static final String FIELDS = "fields";

    public static final String TYPE = "type";

    public static final String HREF_BASE = "internal.href.base";

    public static final String PURE_STATION_INSITU_CONCEPT = "internal.station.insitu.concept";

    public static final String PLATFORMS_INCLUDE_INSITU = "internal.include.insitu";

    public static final String PLATFORMS_INCLUDE_REMOTE = "internal.include.remote";

    public static final String PLATFORMS_INCLUDE_STATIONARY = "internal.include.stationary";

    public static final String PLATFORMS_INCLUDE_MOBILE = "internal.include.mobile";

}
