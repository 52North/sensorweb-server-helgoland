/**
 * ﻿Copyright (C) 2013
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

package org.n52.io;

import static org.n52.io.crs.CRSUtils.DEFAULT_CRS;
import static org.n52.io.crs.CRSUtils.createEpsgForcedXYAxisOrder;
import static org.n52.io.crs.CRSUtils.createEpsgStrictAxisOrder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.n52.io.crs.BoundingBox;
import org.n52.io.crs.CRSUtils;
import org.n52.io.geojson.GeojsonPoint;
import org.n52.io.img.ChartDimension;
import org.n52.io.style.LineStyle;
import org.n52.io.style.Style;
import org.n52.io.v1.data.BBox;
import org.n52.io.v1.data.DesignedParameterSet;
import org.n52.io.v1.data.StyleProperties;
import org.n52.io.v1.data.Vicinity;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Point;

public class IoParameters {

    private final static Logger LOGGER = LoggerFactory.getLogger(IoParameters.class);

    // XXX refactor ParameterSet, DesignedParameterSet, UndesingedParameterSet and QueryMap

    /**
     * How detailed the output shall be.
     */
    static final String EXPANDED = "expanded";

    /**
     * The default expansion of collection items.
     * 
     * @see #EXPANDED
     */
    private static final boolean DEFAULT_EXPANDED = false;

    /**
     * Determines the index of the first member of the response page (a.k.a. page offset).
     */
    static final String OFFSET = "offset";

    /**
     * The default page offset.
     * 
     * @see #OFFSET
     */
    private static final int DEFAULT_OFFSET = 0;

    /**
     * Determines the limit of the page to be returned.
     */
    static final String LIMIT = "limit";

    /**
     * The default page size limit.
     * 
     * @see #LIMIT
     */
    private static final int DEFAULT_LIMIT = 100;

    /**
     * Determines the locale the output shall have.
     */
    static final String LOCALE = "locale";

    /**
     * The default locale.
     * 
     * @see #LOCALE
     */
    private static final String DEFAULT_LOCALE = "en";

    /**
     * Determines the timespan parameter
     */
    static final String TIMESPAN = "timespan";

    /**
     * The width in px of the image to be rendered.
     */
    static final String WIDTH = "width";

    /**
     * The default width of the chart image to render.
     */
    private static final int DEFAULT_WIDTH = 800;

    /**
     * The height in px of the image to be rendered.
     */
    static final String HEIGHT = "height";

    /**
     * The default height of the chart image to render.
     */
    private static final int DEFAULT_HEIGHT = 500;

    /**
     * If a chart shall be rendered with a background grid.
     */
    static final String GRID = "grid";

    /**
     * Defaults to a background grid in a rendered chart.
     */
    private static final boolean DEFAULT_GRID = true;

    /**
     * If a legend shall be drawn on the chart.
     */
    static final String LEGEND = "legend";

    /**
     * Defaults to a not drawn legend.
     */
    private static final boolean DEFAULT_LEGEND = false;

    /**
     * If a rendered chart shall be written as base64 encoded string.
     */
    static final String BASE_64 = "base64";

    /**
     * Defaults to binary output.
     */
    private static final boolean DEFAULT_BASE_64 = false;

    /**
     * Determines the generalize flag.
     */
    static final String GENERALIZE = "generalize";

    /**
     * The default (no generalization) behaviour.
     */
    private static final boolean DEFAULT_GENERALIZE = false;

    /**
     * Determines how raw data shall be formatted.
     */
    static final String FORMAT = "format";

    /**
     * The default format for raw data output.
     */
    private static final String DEFAULT_FORMAT = "tvp";

    /**
     * Determines the style parameter
     */
    static final String STYLE = "style";

    /**
     * Determines the service filter
     */
    static final String SERVICE = "service";

    /**
     * Determines the feature filter
     */
    static final String FEATURE = "feature";

    /**
     * Determines the service filter
     */
    static final String OFFERING = "offering";

    /**
     * Determines the procedure filter
     */
    static final String PROCEDURE = "procedure";

    /**
     * Determines the phenomenon filter
     */
    static final String PHENOMENON = "phenomenon";

    /**
     * Determines the category filter
     */
    static final String CATEGORY = "category";

    /**
     * Determines the reference system to be used for input/output coordinates.
     */
    static final String CRS = "crs";

    /**
     * Determines if CRS axes order shall always be XY, i.e. lon/lat.
     */
    static final String FORCE_XY = "forceXY";

    /**
     * Default axes order respects EPSG axes ordering.
     */
    private static final boolean DEFAULT_FORCE_XY = false;

    /**
     * Determines the within filter
     */
    static final String NEAR = "near";

    /**
     * Determines the bbox filter
     */
    static final String BBOX = "bbox";

    private Map<String, String> query;

    /**
     * Use static constructor {@link #createFromQuery(MultiValueMap)}.
     * 
     * @param queryParameters
     *        containing query parameters. If <code>null</code>, all parameters are returned with default
     *        values.
     */
    protected IoParameters(Map<String, String> queryParameters) {
        if (queryParameters == null) {
            query = new HashMap<String, String>();
        }
        query = queryParameters;
    }

    /**
     * @return the value of {@value #OFFSET} parameter. If not present, the default {@value #DEFAULT_OFFSET}
     *         is returned.
     * @throws IoParseException
     *         if parameter could not be parsed.
     */
    public int getOffset() {
        if ( !query.containsKey(OFFSET)) {
            return DEFAULT_OFFSET;
        }
        return parseInteger(OFFSET);
    }

    /**
     * @return the value of {@value #LIMIT} parameter. If not present, the default {@value #DEFAULT_LIMIT} is
     *         returned.
     * @throws IoParseException
     *         if parameter could not be parsed.
     */
    public int getLimit() {
        if ( !query.containsKey(LIMIT)) {
            return DEFAULT_LIMIT;
        }
        return parseInteger(LIMIT);
    }

    /**
     * @return the chart dimensions. If {@value #WIDTH} and {@value #HEIGHT} parameters are missing the
     *         defaults are used: <code>width=</code>{@value #DEFAULT_WIDTH}, <code>height=</code>
     *         {@value #DEFAULT_HEIGHT}
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public ChartDimension getChartDimension() {
        return new ChartDimension(getWidth(), getHeight());
    }

    /**
     * @return the requested chart width in pixels or the default {@value #DEFAULT_WIDTH}.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    private int getWidth() {
        if ( !query.containsKey(WIDTH)) {
            return DEFAULT_WIDTH;
        }
        return parseInteger(WIDTH);
    }

    /**
     * Returns the requested chart height in pixels.
     * 
     * @return the requested chart height in pixels or the default {@value #DEFAULT_HEIGHT}.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    private int getHeight() {
        if ( !query.containsKey(HEIGHT)) {
            return DEFAULT_HEIGHT;
        }
        return parseInteger(HEIGHT);
    }

    /**
     * Indicates if rendered chart shall be returned as Base64 encoded string.
     * 
     * @return the value of parameter {@value #BASE_64} or the default {@value #DEFAULT_BASE_64}.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public boolean isBase64() {
        if ( !query.containsKey(BASE_64)) {
            return DEFAULT_BASE_64;
        }
        return parseBoolean(BASE_64);
    }

    /**
     * @return <code>true</code> if timeseries chart shall include a background grid.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public boolean isGrid() {
        if ( !query.containsKey(GRID)) {
            return DEFAULT_GRID;
        }
        return parseBoolean(GRID);
    }

    /**
     * @return <code>true</code> if timeseries data shall be generalized.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public boolean isGeneralize() throws IoParseException {
        if ( !query.containsKey(GENERALIZE)) {
            return DEFAULT_GENERALIZE;
        }
        return parseBoolean(GENERALIZE);
    }

    /**
     * @return <code>true</code> if a legend shall be included when rendering a chart, <code>false</code>
     *         otherwise.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public boolean isLegend() {
        if ( !query.containsKey(LEGEND)) {
            return DEFAULT_LEGEND;
        }
        return parseBoolean(LEGEND);
    }

    /**
     * @return the value of {@value #LOCALE} parameter. If not present, the default {@value #DEFAULT_LOCALE}
     *         is returned.
     */
    public String getLocale() {
        if ( !query.containsKey(LOCALE)) {
            return DEFAULT_LOCALE;
        }
        return query.get(LOCALE);
    }

    /**
     * @return the value of {@value #STYLE} parameter. If not present, the default styles are returned.
     * @throws IoParseException
     *         if parsing style parameter failed.
     */
    public StyleProperties getStyle() {
        if ( !query.containsKey(STYLE)) {
            return StyleProperties.createDefaults();
        }
        return parseStyleProperties(query.get(STYLE));
    }

    /**
     * Creates a generic {@link StyleProperties} instance which can be used to create more concrete
     * {@link Style}s. For example use {@link LineStyle#createLineStyle(StyleProperties)} which gives you a
     * style view which can be used for lines.
     * 
     * @param style
     *        the JSON style parameter to parse.
     * @return a parsed {@link StyleProperties} instance.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    private StyleProperties parseStyleProperties(String style) {
        try {
            return style == null ? StyleProperties.createDefaults()
                : new ObjectMapper().readValue(style, StyleProperties.class);
        }
        catch (JsonMappingException e) {
            throw new IoParseException("Could not read style properties: " + style, e);
        }
        catch (JsonParseException e) {
            throw new IoParseException("Could not parse style properties: " + style, e);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("An error occured during request handling.", e);
        }

    }

    public String getFormat() {
        if ( !query.containsKey(FORMAT)) {
            return DEFAULT_FORMAT;
        }
        return query.get(FORMAT);
    }

    /**
     * @return the value of {@value #TIMESPAN} parameter. If not present, the default timespan is returned.
     * @throws IoParseException
     *         if timespan could not be parsed.
     */
    public String getTimespan() {
        if ( !query.containsKey(TIMESPAN)) {
            return createDefaultTimespan();
        }
        return validateTimespan(query.get(TIMESPAN));
    }

    private String createDefaultTimespan() {
        DateTime now = new DateTime();
        DateTime lastWeek = now.minusWeeks(1);
        return new Interval(lastWeek, now).toString();
    }

    private String validateTimespan(String timespan) {
        try {
            return Interval.parse(timespan).toString();
        }
        catch (IllegalArgumentException e) {
            String message = "Could not parse timespan parameter." + timespan;
            throw new IoParseException(message, e);
        }
    }

    public String getCategory() {
        return query.get(CATEGORY);
    }

    public String getService() {
        return query.get(SERVICE);
    }

    public String getOffering() {
        return query.get(OFFERING);
    }

    public String getFeature() {
        return query.get(FEATURE);
    }

    public String getProcedure() {
        return query.get(PROCEDURE);
    }

    public String getPhenomenon() {
        return query.get(PHENOMENON);
    }

    /**
     * Creates a {@link BoundingBox} instance from given spatial request parameters. The resulting bounding
     * box is the merged extent of all spatial filters given. For example if {@value #NEAR} and {@value #BBOX}
     * exist, the returned bounding box includes both extents.
     * 
     * @return a spatial filter created from given spatial parameters.
     * @throws IoParseException
     *         if parsing parameters fails, or if a requested {@value #CRS} object could not be created.
     */
    public BoundingBox getSpatialFilter() {
        if ( !query.containsKey(NEAR) && !query.containsKey(BBOX)) {
            return null;
        }

        BBox bboxBounds = createBbox();
        BoundingBox bounds = parseBoundsFromVicinity();
        return mergeBounds(bounds, bboxBounds);
    }

    private BoundingBox mergeBounds(BoundingBox bounds, BBox bboxBounds) {
        if (bboxBounds == null) {
            // nothing to merge
            return bounds;
        }
        CRSUtils crsUtils = createEpsgForcedXYAxisOrder();
        Point lowerLeft = crsUtils.convertToPointFrom(bboxBounds.getLl());
        Point upperRight = crsUtils.convertToPointFrom(bboxBounds.getUr());
        if (bounds == null) {
            bounds = new BoundingBox(lowerLeft, upperRight, DEFAULT_CRS);
            LOGGER.debug("Parsed bbox bounds: {}", bounds.toString());
        }
        else {
            bounds.extendBy(lowerLeft);
            bounds.extendBy(upperRight);
            LOGGER.debug("Merged bounds: {}", bounds.toString());
        }
        return bounds;
    }

    /**
     * @return a {@link BBox} instance or <code>null</code> if no {@link #BBOX} parameter is present.
     * @throws IoParseException
     *         if parsing parameter fails.
     * @throws IoParseException
     *         if a requested {@value #CRS} object could not be created
     */
    private BBox createBbox() {
        if ( !query.containsKey(BBOX)) {
            return null;
        }
        String bboxValue = query.get(BBOX);
        BBox bbox = parseJson(bboxValue, BBox.class);
        bbox.setLl(convertToCrs84(bbox.getLl()));
        bbox.setUr(convertToCrs84(bbox.getUr()));
        return bbox;
    }

    private BoundingBox parseBoundsFromVicinity() {
        if ( !query.containsKey(NEAR)) {
            return null;
        }
        String vicinityValue = query.get(NEAR);
        Vicinity vicinity = parseJson(vicinityValue, Vicinity.class);
        if (query.containsKey(CRS)) {
            vicinity.setCenter(convertToCrs84(vicinity.getCenter()));
        }
        BoundingBox bounds = vicinity.calculateBounds();
        LOGGER.debug("Parsed vicinity bounds: {}", bounds.toString());
        return bounds;
    }

    /**
     * @param jsonString
     *        the JSON string to parse.
     * @param clazz
     *        the type to serialize given JSON string to.
     * @return a mapped instance parsed from JSON.
     * @throws IoParseException
     *         if JSON is invalid or does not map to given type.
     */
    private <T> T parseJson(String jsonString, Class<T> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, clazz);
        }
        catch (JsonParseException e) {
            throw new IoParseException("The given parameter is invalid JSON." + jsonString, e);
        }
        catch (JsonMappingException e) {
            throw new IoParseException("The given parameter could not been read: " + jsonString, e);
        }
        catch (IOException e) {
            throw new RuntimeException("Could not handle input to parse.", e);
        }
    }

    private GeojsonPoint convertToCrs84(GeojsonPoint point) {
        return isForceXY() // is strict XY axis order?!
            ? transformToInnerCrs(point, createEpsgForcedXYAxisOrder())
            : transformToInnerCrs(point, createEpsgStrictAxisOrder());
    }

    /**
     * @param point
     *        a GeoJSON point to be transformed to internally used CRS:84.
     * @param crsUtils
     *        a reference helper.
     * @return a transformed GeoJSON instance.
     * @throws IoParseException
     *         if point could not be transformed, or if requested CRS object could not be created.
     */
    private GeojsonPoint transformToInnerCrs(GeojsonPoint point, CRSUtils crsUtils) {
        try {
            Point toTransformed = crsUtils.convertToPointFrom(point, getCrs());
            Point crs84Point = crsUtils.transformOuterToInner(toTransformed, getCrs());
            return crsUtils.convertToGeojsonFrom(crs84Point);
        }
        catch (TransformException e) {
            throw new IoParseException("Could not transform to internally used CRS:84.", e);
        }
        catch (FactoryException e) {
            throw new IoParseException("Check if 'crs' parameter is a valid EPSG CRS. Was: '" + getCrs() + "'.", e);
        }
    }

    /**
     * @return the requested reference context, or the default ({@value #DEFAULT_CRS} which will be
     *         interpreted as lon/lat ordered axes).
     */
    public String getCrs() {
        if ( !query.containsKey(CRS)) {
            return DEFAULT_CRS;
        }
        return query.get(CRS);
    }

    public boolean isForceXY() {
        if ( !query.containsKey(FORCE_XY)) {
            return DEFAULT_FORCE_XY;
        }
        return parseBoolean(FORCE_XY);
    }

    /**
     * @return the value of {@value #EXPANDED} parameter.
     * @throws IoParseException
     *         if parameter could not be parsed.
     */
    public boolean isExpanded() {
        if ( !query.containsKey(EXPANDED)) {
            return DEFAULT_EXPANDED;
        }
        return parseBoolean(EXPANDED);
    }

    public boolean containsParameter(String parameter) {
        return query.containsKey(parameter);
    }

    public String getOther(String parameter) {
        return query.get(parameter);
    }

    /**
     * @param parameter
     *        the parameter to parse to an <code>int</code> value.
     * @return an integer value.
     * @throws IoParseException
     *         if parsing to <code>int</code> fails.
     */
    private int parseInteger(String parameter) {
        try {
            String value = query.get(parameter);
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            throw new IoParseException("Parameter '" + parameter + "' has to be an integer!");
        }
    }

    /**
     * @param parameter
     *        the parameter to parse to <code>boolean</code>.
     * @return <code>true</code> or <code>false</code> as <code>boolean</code>.
     * @throws IoParseException
     *         if parsing to <code>boolean</code> fails.
     */
    private boolean parseBoolean(String parameter) {
        try {
            String value = query.get(parameter);
            return Boolean.parseBoolean(value);
        }
        catch (NumberFormatException e) {
            throw new IoParseException("Parameter '" + parameter + "' has to be 'false' or 'true'!");
        }
    }

    public static IoParameters createDefaults() {
        return new IoParameters(null);
    }

    /**
     * @param queryParameters
     *        the parameters sent via GET payload.
     * @return a query map for convenient parameter access plus validation.
     */
    public static IoParameters createFromQuery(Map<String, String> queryParameters) {
        return new IoParameters(queryParameters);
    }
    
    /**
     * @param parameters
     *        the parameters sent via POST payload.
     * @return a query map for convenient parameter access plus validation.
     */
    public static IoParameters createFromQuery(DesignedParameterSet parameters) {

        // TODO consolidate undesigned/desigend paramter sets
        
        Map<String, String> queryParameters = new HashMap<String, String>();
        queryParameters.put(LOCALE, parameters.getLanguage());
        queryParameters.put(TIMESPAN, parameters.getTimespan());
        queryParameters.put(GRID, Boolean.toString(parameters.isGrid()));
        queryParameters.put(EXPANDED, Boolean.toString(parameters.isExpanded()));
        queryParameters.put(HEIGHT, Integer.toString(parameters.getHeight()));
        queryParameters.put(WIDTH, Integer.toString(parameters.getWidth()));

        return createFromQuery(queryParameters);
    }
    
}
