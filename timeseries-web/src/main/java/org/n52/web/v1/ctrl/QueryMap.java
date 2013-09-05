/**
 * ﻿Copyright (C) 2012
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

import static org.n52.io.crs.CRSUtils.DEFAULT_CRS;
import static org.n52.io.crs.CRSUtils.createEpsgForcedXYAxisOrder;
import static org.n52.io.crs.CRSUtils.createEpsgStrictAxisOrder;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.n52.io.crs.BoundingBox;
import org.n52.io.crs.CRSUtils;
import org.n52.io.geojson.GeojsonPoint;
import org.n52.io.v1.data.BBox;
import org.n52.io.v1.data.DesignedParameterSet;
import org.n52.io.v1.data.StyleProperties;
import org.n52.io.v1.data.Vicinity;
import org.n52.web.BadRequestException;
import org.n52.web.InternalServerException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Point;

public class QueryMap {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(QueryMap.class);

    // XXX refactor ParameterSet, DesignedParameterSet, UndesingedParameterSet and QueryMap

    /**
     * How detailed the output shall be.
     */
    private static final String EXPANDED = "expanded";

    /**
     * The default expansion of collection items.
     * 
     * @see #EXPANDED
     */
    private static final boolean DEFAULT_EXPANDED = false;

    /**
     * Determines the index of the first member of the response page (a.k.a. page offset).
     */
    private static final String OFFSET = "offset";

    /**
     * The default page offset.
     * 
     * @see #OFFSET
     */
    private static final int DEFAULT_OFFSET = 0;

    /**
     * Determines the limit of the page to be returned.
     */
    private static final String LIMIT = "limit";

    /**
     * The default page size limit.
     * 
     * @see #LIMIT
     */
    private static final int DEFAULT_LIMIT = 100;

    /**
     * Determines the locale the output shall have.
     */
    private static final String LOCALE = "locale";

    /**
     * The default locale.
     * 
     * @see #LOCALE
     */
    private static final String DEFAULT_LOCALE = "en";

    /**
     * Determines the timespan parameter
     */
    private static final String TIMESPAN = "timespan";

    /**
     * The width in px of the image to be rendered.
     */
    private static final String WIDTH = "width";

    /**
     * The default width of the chart image to render.
     */
    private static final int DEFAULT_WIDTH = 800;

    /**
     * The height in px of the image to be rendered.
     */
    private static final String HEIGHT = "height";

    /**
     * The default height of the chart image to render.
     */
    private static final int DEFAULT_HEIGHT = 500;

    /**
     * If a chart shall be rendered with a background grid.
     */
    private static final String GRID = "grid";

    /**
     * Defaults to a background grid in a rendered chart.
     */
    private static final boolean DEFAULT_GRID = true;

    /**
     * If a legend shall be drawn on the chart.
     */
    private static final String LEGEND = "legend";

    /**
     * Defaults to a not drawn legend.
     */
    private static final boolean DEFAULT_LEGEND = false;

    /**
     * If a rendered chart shall be written as base64 encoded string.
     */
    private static final String BASE_64 = "base64";

    /**
     * Defaults to binary output.
     */
    private static final boolean DEFAULT_BASE_64 = false;

    /**
     * Determines the generalize flag.
     */
    private static final String GENERALIZE = "generalize";

    /**
     * The default (no generalization) behaviour.
     */
    private static final boolean DEFAULT_GENERALIZE = false;

    /**
     * Determines how raw data shall be formatted.
     */
    private static final String FORMAT = "format";

    /**
     * The default format for raw data output.
     */
    private static final String DEFAULT_FORMAT = "tvp";

    /**
     * Determines the style parameter
     */
    private static final String STYLE = "style";

    /**
     * Determines the service filter
     */
    private static final String SERVICE = "service";

    /**
     * Determines the feature filter
     */
    private static final String FEATURE = "feature";

    /**
     * Determines the service filter
     */
    private static final String OFFERING = "offering";

    /**
     * Determines the procedure filter
     */
    private static final String PROCEDURE = "procedure";

    /**
     * Determines the phenomenon filter
     */
    private static final String PHENOMENON = "phenomenon";

    /**
     * Determines the category filter
     */
    private static final String CATEGORY = "category";

    /**
     * Determines the reference system to be used for input/output coordinates.
     */
    private static final String CRS = "crs";

    /**
     * Determines if CRS axes order shall always be XY, i.e. lon/lat.
     */
    private static final String FORCE_XY = "forceXY";

    /**
     * Default axes order respects EPSG axes ordering.
     */
    private static final boolean DEFAULT_FORCE_XY = false;

    /**
     * Determines the within filter
     */
    private static final String NEAR = "near";

    /**
     * Determines the bbox filter
     */
    private static final String BBOX = "bbox";

    private MultiValueMap<String, String> query;

    /**
     * Use static constructor {@link #createFromQuery(MultiValueMap)}.
     * 
     * @param queryParameters
     *        containing query parameters. If <code>null</code>, all parameters are returned with default
     *        values.
     */
    private QueryMap(MultiValueMap<String, String> queryParameters) {
        if (queryParameters == null) {
            query = new LinkedMultiValueMap<String, String>();
        }
        query = queryParameters;
    }

    /**
     * @return the value of {@value #OFFSET} parameter. If not present, the default {@value #DEFAULT_OFFSET}
     *         is returned.
     * @throws BadRequestException
     *         if parameter could not be parsed.
     */
    public int getOffset() {
        if ( !query.containsKey(OFFSET)) {
            return DEFAULT_OFFSET;
        }
        return parseFirstIntegerOfParameter(OFFSET);
    }

    /**
     * @return the value of {@value #LIMIT} parameter. If not present, the default {@value #DEFAULT_LIMIT} is
     *         returned.
     * @throws BadRequestException
     *         if parameter could not be parsed.
     */
    public int getLimit() {
        if ( !query.containsKey(LIMIT)) {
            return DEFAULT_LIMIT;
        }
        return parseFirstIntegerOfParameter(LIMIT);
    }

    public int getWidth() {
        if ( !query.containsKey(WIDTH)) {
            return DEFAULT_WIDTH;
        }
        return parseFirstIntegerOfParameter(WIDTH);
    }

    public int getHeight() {
        if ( !query.containsKey(HEIGHT)) {
            return DEFAULT_HEIGHT;
        }
        return parseFirstIntegerOfParameter(HEIGHT);
    }

    public boolean isBase64() {
        if ( !query.containsKey(BASE_64)) {
            return DEFAULT_BASE_64;
        }
        return parseFirstBooleanOfParameter(BASE_64);
    }

    public boolean isGrid() {
        if ( !query.containsKey(GRID)) {
            return DEFAULT_GRID;
        }
        return parseFirstBooleanOfParameter(GRID);
    }

    public boolean isGeneralize() {
        if ( !query.containsKey(GENERALIZE)) {
            return DEFAULT_GENERALIZE;
        }
        return parseFirstBooleanOfParameter(GENERALIZE);
    }

    public boolean isLegend() {
        if ( !query.containsKey(LEGEND)) {
            return DEFAULT_LEGEND;
        }
        return parseFirstBooleanOfParameter(LEGEND);
    }

    /**
     * @return the value of {@value #LOCALE} parameter. If not present, the default {@value #DEFAULT_LOCALE}
     *         is returned.
     */
    public String getLocale() {
        if ( !query.containsKey(LOCALE)) {
            return DEFAULT_LOCALE;
        }
        return query.getFirst(LOCALE);
    }

    /**
     * @return the value of {@value #STYLE} parameter. If not present, the default styles are returned.
     */
    public StyleProperties getStyle() {
        if ( !query.containsKey(STYLE)) {
            return StyleProperties.createDefaults();
        }
        return parseStyleProperties(query.getFirst(STYLE));
    }

    private StyleProperties parseStyleProperties(String style) {
        try {
            return style == null ? StyleProperties.createDefaults()
                                : new ObjectMapper().readValue(style, StyleProperties.class);
        }
        catch (JsonMappingException e) {
            throw new BadRequestException("Could not read style properties: " + style, e);
        }
        catch (JsonParseException e) {
            throw new BadRequestException("Could not parse style properties: " + style, e);
        }
        catch (IOException e) {
            throw new InternalServerException("An error occured during request handling.", e);
        }
    }

    public String getFormat() {
        if ( !query.containsKey(FORMAT)) {
            return DEFAULT_FORMAT;
        }
        return query.getFirst(FORMAT);
    }

    /**
     * @return the value of {@value #TIMESPAN} parameter. If not present, the default timespan is returned.
     */
    public String getTimespan() {
        if ( !query.containsKey(TIMESPAN)) {
            return createDefaultTimespan();
        }
        return validateTimespan(query.getFirst(TIMESPAN));
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
            String message = "Could not parse timespan parameter: " + timespan;
            BadRequestException badRequest = new BadRequestException(message, e);
            badRequest.addHint("Valid timespans have to be in ISO8601 period format.");
            badRequest.addHint("Valid examples: 'PT6H/2013-08-13TZ' or '2013-07-13TZ/2013-08-13TZ'.");
            throw badRequest;
        }
    }

    public String getCategory() {
        return query.getFirst(CATEGORY);
    }

    public String getService() {
        return query.getFirst(SERVICE);
    }

    public String getOffering() {
        return query.getFirst(OFFERING);
    }

    public String getFeature() {
        return query.getFirst(FEATURE);
    }

    public String getProcedure() {
        return query.getFirst(PROCEDURE);
    }

    public String getPhenomenon() {
        return query.getFirst(PHENOMENON);
    }

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

    private BBox createBbox() {
        if ( !query.containsKey(BBOX)) {
            return null;
        }
        String bboxValue = query.getFirst(BBOX);
        BBox bbox = parseJson(bboxValue, BBox.class);
        bbox.setLl(convertToCrs84(bbox.getLl()));
        bbox.setUr(convertToCrs84(bbox.getUr()));
        return bbox;
    }

    private BoundingBox parseBoundsFromVicinity() {
        if ( !query.containsKey(NEAR)) {
            return null;
        }
        String vicinityValue = query.getFirst(NEAR);
        Vicinity vicinity = parseJson(vicinityValue, Vicinity.class);
        if (query.containsKey(CRS)) {
            vicinity.setCenter(convertToCrs84(vicinity.getCenter()));
        }
        BoundingBox bounds = vicinity.calculateBounds();
        LOGGER.debug("Parsed vicinity bounds: {}", bounds.toString());
        return bounds;
    }

    private <T> T parseJson(String jsonString, Class<T> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, clazz);
        }
        catch (IOException e) {
            return null;
        }
    }

    private GeojsonPoint convertToCrs84(GeojsonPoint point) {
        return isForceXY() // is strict XY axis order?!
              ? transformToInnerCrs(point, createEpsgForcedXYAxisOrder())
              : transformToInnerCrs(point, createEpsgStrictAxisOrder());
    }

    private GeojsonPoint transformToInnerCrs(GeojsonPoint center, CRSUtils crsUtils) {
        try {
            Point point = crsUtils.convertToPointFrom(center, getCrs());
            Point crs84Point = crsUtils.transformOuterToInner(point, getCrs());
            return crsUtils.convertToGeojsonFrom(crs84Point);
        }
        catch (TransformException e) {
            BadRequestException ex = new BadRequestException("Transformation could not be performed.", e);
            ex.addHint("Check the 'crs' parameter determines a valid EPSG CRS. Was: '" + getCrs() + "'.");
            ex.addHint("Check http://epsg-registry.org for EPSG CRS definitions and codes.");
            throw ex;
        }
        catch (FactoryException e) {
            throw new InternalServerException("Could not handle CRS.", e);
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
        return query.getFirst(CRS);
    }

    public boolean isForceXY() {
        if ( !query.containsKey(FORCE_XY)) {
            return DEFAULT_FORCE_XY;
        }
        return parseFirstBooleanOfParameter(FORCE_XY);
    }

    /**
     * @return the value of {@value #EXPANDED} parameter.
     * @throws BadRequestException
     *         if parameter could not be parsed.
     */
    public boolean shallExpand() {
        if ( !query.containsKey(EXPANDED)) {
            return DEFAULT_EXPANDED;
        }
        return parseFirstBooleanOfParameter(EXPANDED);
    }

    public boolean containsParameter(String parameter) {
        return query.containsKey(parameter);
    }

    public String[] getOther(String parameter) {
        return query.get(parameter).toArray(new String[0]);
    }

    private int parseFirstIntegerOfParameter(String parameter) {
        try {
            String value = query.getFirst(parameter);
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            throw new BadRequestException("Parameter '" + parameter + "' has to be an integer!");
        }
    }

    private boolean parseFirstBooleanOfParameter(String parameter) {
        try {
            String value = query.getFirst(parameter);
            return Boolean.parseBoolean(value);
        }
        catch (NumberFormatException e) {
            throw new BadRequestException("Parameter '" + parameter + "' has to be 'false' or 'true'!");
        }
    }

    /**
     * @param queryParameters
     *        the parameters sent via GET payload.
     * @return a query map for convenient parameter access plus validation.
     */
    public static QueryMap createFromQuery(MultiValueMap<String, String> queryParameters) {
        return new QueryMap(queryParameters);
    }

    /**
     * @param parameters
     *        the parameters sent via POST payload.
     * @return a query map for convenient parameter access plus validation.
     */
    public static QueryMap createFromQuery(DesignedParameterSet parameters) {
        LinkedMultiValueMap<String, String> queryParameters = new LinkedMultiValueMap<String, String>();
        queryParameters.add(LOCALE, parameters.getLanguage());
        queryParameters.add(TIMESPAN, parameters.getTimespan());
        queryParameters.add(WIDTH, parameters.getWidth() + "");
        queryParameters.add(HEIGHT, parameters.getHeight() + "");
        queryParameters.add(GRID, Boolean.toString(parameters.isGrid()));

        // TODO add further parameters

        return new QueryMap(queryParameters);
    }

    public static QueryMap createDefaults() {
        return new QueryMap(null);
    }
}
