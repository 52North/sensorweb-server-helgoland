/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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

import static org.n52.io.crs.CRSUtils.DEFAULT_CRS;
import static org.n52.io.crs.CRSUtils.createEpsgForcedXYAxisOrder;
import static org.n52.io.crs.CRSUtils.createEpsgStrictAxisOrder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.xmlbeans.impl.tool.Extension.Param;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.n52.io.IntervalWithTimeZone;
import org.n52.io.IoParseException;
import org.n52.io.crs.BoundingBox;
import org.n52.io.crs.CRSUtils;
import org.n52.io.geojson.old.GeojsonPoint;
import org.n52.io.measurement.img.ChartDimension;
import org.n52.io.response.BBox;
import org.n52.io.style.LineStyle;
import org.n52.io.style.Style;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.vividsolutions.jts.geom.Point;

public class IoParameters implements Parameters {

    private final static Logger LOGGER = LoggerFactory.getLogger(IoParameters.class);

    private final static String DEFAULT_CONFIG_FILE = "config-general.json";

    private static final ObjectMapper om = new ObjectMapper(); // TODO use global object mapper

    private final MultiValueMap<String, JsonNode> query;

    private static InputStream getDefaultConfigFile() {
        try {
            Path path = Paths.get(IoParameters.class.getResource("/").toURI());
            File config = path.resolve(DEFAULT_CONFIG_FILE).toFile();
            return config.exists()
                ? new FileInputStream(config)
                : IoParameters.class.getClassLoader().getResourceAsStream("/" + DEFAULT_CONFIG_FILE);
        } catch (URISyntaxException | IOException e) {
            LOGGER.debug("Could not find default config under '{}'", DEFAULT_CONFIG_FILE, e);
            return null;
        }
    }

    protected IoParameters(IoParameters parameters) {
        this((File) null);
        if (parameters != null) {
            query.putAll(parameters.query);
        }
    }

    protected IoParameters(MultiValueMap<String, JsonNode> queryParameters) {
        this(queryParameters, (File) null);
    }

    protected IoParameters(MultiValueMap<String, JsonNode> queryParameters, File defaults) {
        this(defaults);
        if (queryParameters != null) {
            query.putAll(queryParameters);
        }
    }

    protected IoParameters(Map<String, JsonNode> queryParameters) {
        this(queryParameters, (File) null);
    }

    protected IoParameters(Map<String, JsonNode> queryParameters, File defaults) {
        this(defaults);
        query.setAll(queryParameters);
    }

    private IoParameters(File defaultConfig) {
        query = new LinkedMultiValueMap<>();
        query.setAll(readDefaultConfig(defaultConfig));
    }

    private Map<String, JsonNode> readDefaultConfig(File config) {
        try (InputStream stream = config == null
                ? getDefaultConfigFile()
                : new FileInputStream(config)) {
            return om.readValue(stream, TypeFactory
                    .defaultInstance()
                    .constructMapLikeType(HashMap.class, String.class, JsonNode.class));
        } catch (IOException e) {
            LOGGER.info("Could not load '{}'. Using empty config.", DEFAULT_CONFIG_FILE, e);
            return new HashMap<>();
        }
    }

    /**
     * @return the value of {@value #OFFSET} parameter. If not present, the
     * default {@value #DEFAULT_OFFSET} is returned.
     * @throws IoParseException if parameter could not be parsed.
     */
    public int getOffset() {
        if (!containsParameter(OFFSET)) {
            return DEFAULT_OFFSET;
        }
        return getAsInteger(OFFSET);
    }

    /**
     * @return the value of {@value #LIMIT} parameter. If not present, the
     * default {@value #DEFAULT_LIMIT} is returned.
     * @throws IoParseException if parameter could not be parsed.
     */
    public int getLimit() {
        if (!containsParameter(LIMIT)) {
            return DEFAULT_LIMIT;
        }
        return getAsInteger(LIMIT);
    }

    /**
     * @return the chart dimensions. If {@value #WIDTH} and {@value #HEIGHT}
     * parameters are missing the defaults are used:
     * <code>width=</code>{@value #DEFAULT_WIDTH}, <code>height=</code>
     * {@value #DEFAULT_HEIGHT}
     * @throws IoParseException if parsing parameter fails.
     */
    public ChartDimension getChartDimension() {
        return new ChartDimension(getWidth(), getHeight());
    }

    /**
     * @return the requested chart width in pixels or the default
     * {@value #DEFAULT_WIDTH}.
     * @throws IoParseException if parsing parameter fails.
     */
    private int getWidth() {
        if (!containsParameter(WIDTH)) {
            return DEFAULT_WIDTH;
        }
        return getAsInteger(WIDTH);
    }

    /**
     * Returns the requested chart height in pixels.
     *
     * @return the requested chart height in pixels or the default
     * {@value #DEFAULT_HEIGHT}.
     * @throws IoParseException if parsing parameter fails.
     */
    private int getHeight() {
        if (!containsParameter(HEIGHT)) {
            return DEFAULT_HEIGHT;
        }
        return getAsInteger(HEIGHT);
    }

    /**
     * Indicates if rendered chart shall be returned as Base64 encoded string.
     *
     * @return the value of parameter {@value #BASE_64} or the default
     * {@value #DEFAULT_BASE_64}.
     * @throws IoParseException if parsing parameter fails.
     */
    public boolean isBase64() {
        if (!containsParameter(BASE_64)) {
            return DEFAULT_BASE_64;
        }
        return getAsBoolean(BASE_64);
    }

    /**
     * @return <code>true</code> if timeseries chart shall include a background
     * grid.
     * @throws IoParseException if parsing parameter fails.
     */
    public boolean isGrid() {
        if (!containsParameter(GRID)) {
            return DEFAULT_GRID;
        }
        return getAsBoolean(GRID);
    }

    /**
     * @return <code>true</code> if timeseries data shall be generalized.
     * @throws IoParseException if parsing parameter fails.
     */
    public boolean isGeneralize() throws IoParseException {
        if (!containsParameter(GENERALIZE)) {
            return DEFAULT_GENERALIZE;
        }
        return getAsBoolean(GENERALIZE);
    }

    /**
     * @return <code>true</code> if a legend shall be included when rendering a
     * chart, <code>false</code> otherwise.
     * @throws IoParseException if parsing parameter fails.
     */
    public boolean isLegend() {
        if (!containsParameter(LEGEND)) {
            return DEFAULT_LEGEND;
        }
        return getAsBoolean(LEGEND);
    }

    /**
     * @return the value of {@value #LOCALE} parameter. If not present, the
     * default {@value #DEFAULT_LOCALE} is returned.
     */
    public String getLocale() {
        if (!containsParameter(LOCALE)) {
            return DEFAULT_LOCALE;
        }
        return getAsString(LOCALE);
    }

    /**
     * @return the value of {@value #STYLE} parameter. If not present, the
     * default styles are returned.
     * @throws IoParseException if parsing style parameter failed.
     */
    public StyleProperties getStyle() {
        if (!containsParameter(STYLE)) {
            return StyleProperties.createDefaults();
        }
        return parseStyleProperties(getAsString(STYLE));
    }

    /**
     * Creates a generic {@link StyleProperties} instance which can be used to
     * create more concrete {@link Style}s. For example use
     * {@link LineStyle#createLineStyle(StyleProperties)} which gives you a
     * style view which can be used for lines.
     *
     * @param style the JSON style parameter to parse.
     * @return a parsed {@link StyleProperties} instance.
     * @throws IoParseException if parsing parameter fails.
     */
    private StyleProperties parseStyleProperties(String style) {
        try {
            return style == null ? StyleProperties.createDefaults()
                    : new ObjectMapper().readValue(style, StyleProperties.class);
        } catch (JsonMappingException e) {
            throw new IoParseException("Could not read style properties: " + style, e);
        } catch (JsonParseException e) {
            throw new IoParseException("Could not parse style properties: " + style, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("An error occured during request handling.", e);
        }

    }

    public String getFormat() {
        if (!containsParameter(FORMAT)) {
            return DEFAULT_FORMAT;
        }
        return getAsString(FORMAT);
    }

    public boolean isSetRawFormat() {
        return containsParameter(RAW_FORMAT);
    }

    public String getRawFormat() {
        if (isSetRawFormat()) {
            final JsonNode value = query.getFirst(RAW_FORMAT);
            return value != null
                    ? value.asText()
                    : null;
        }
        return null;
    }

    /**
     * @return the value of {@value #TIMESPAN} parameter. If not present, the
     * default timespan is returned.
     * @throws IoParseException if timespan could not be parsed.
     */
    public IntervalWithTimeZone getTimespan() {
        if (!containsParameter(TIMESPAN)) {
            return createDefaultTimespan();
        }
        return validateTimespan(getAsString(TIMESPAN));
    }

    private IntervalWithTimeZone createDefaultTimespan() {
        DateTime now = new DateTime();
        DateTime lastWeek = now.minusWeeks(1);
        String interval = lastWeek
                .toString()
                .concat("/")
                .concat(now.toString());
        return new IntervalWithTimeZone(interval);
    }

    private IntervalWithTimeZone validateTimespan(String timespan) {
        return new IntervalWithTimeZone(timespan);
    }

    public Instant getResultTime() {
        if (!containsParameter(RESULTTIME)) {
            return null;
        }
        return validateTimestamp(getAsString(RESULTTIME));
    }

    private Instant validateTimestamp(String timestamp) {
        try {
            return Instant.parse(timestamp);
        } catch (Exception e) {
            String message = "Could not parse result time parameter." + timestamp;
            throw new IoParseException(message, e);
        }
    }

    /**
     * @return the category filter
     * @deprecated use {@link #getCategories()}
     */
    @Deprecated
    public String getCategory() {
        return getAsString(CATEGORY);
    }

    /**
     * @return the service filter
     * @deprecated use {@link #getServices()}
     */
    @Deprecated
    public String getService() {
        return getAsString(SERVICE);
    }

    /**
     * @return the offering filter
     * @deprecated use {@link #getOfferings()}
     */
    @Deprecated
    public String getOffering() {
        return getAsString(OFFERING);
    }

    /**
     * @return the feature filter
     * @deprecated use {@link #getFeatures()}
     */
    @Deprecated
    public String getFeature() {
        return getAsString(FEATURE);
    }

    /**
     * @return the procedure filter
     * @deprecated use {@link #getProcedures()}
     */
    @Deprecated
    public String getProcedure() {
        return getAsString(PROCEDURE);
    }

    /**
     * @return the phenomenon filter
     * @deprecated use {@link #getPhenomena()}
     */
    @Deprecated
    public String getPhenomenon() {
        return getAsString(PHENOMENON);
    }

    @Deprecated
    public String getStation() {
        return getAsString(STATION);
    }

    public Set<String> getCategories() {
        return getValuesOf(CATEGORIES);
    }

    public Set<String> getServices() {
        return getValuesOf(SERVICES);
    }

    public Set<String> getOfferings() {
        return getValuesOf(OFFERINGS);
    }

    public Set<String> getFeatures() {
        return getValuesOf(FEATURES);
    }

    public Set<String> getProcedures() {
        return getValuesOf(PROCEDURES);
    }

    public Set<String> getPhenomena() {
        return getValuesOf(PHENOMENA);
    }

    public Set<String> getPlatforms() {
        return getValuesOf(PLATFORMS);
    }

    public Set<String> getSeries() {
        return getValuesOf(SERIES);
    }

    public Set<String> getDatasets() {
        return getValuesOf(DATASETS);
    }

    public Set<String> getFields() {
        return getValuesOf(FILTER_FIELDS);
    }

    public Set<String> getPlatformTypes() {
        return getValuesOf(FILTER_PLATFORM_TYPES);
    }

    public Set<String> getPlatformGeometryTypes() {
        return getValuesOf(FILTER_PLATFORM_GEOMETRIES);
    }

    public Set<String> getObservedGeometryTypes() {
        return getValuesOf(FILTER_OBSERVED_GEOMETRIES);
    }

    public Set<String> getDatasetTypes() {
        return getValuesOf(FILTER_DATASET_TYPES);
    }

    public Set<String> getSearchTerms() {
        return getValuesOf(SEARCH_TERM);
    }

    Set<String> getValuesOf(String parameterName) {
        return containsParameter(parameterName)
                ? new HashSet<>(csvToLowerCasedSet(getAsString(parameterName)))
                : Collections.<String>emptySet();
    }

    private Set<String> csvToLowerCasedSet(String csv) {
        String[] values = csv.split(",");
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].toLowerCase();
        }
        return new HashSet<>(Arrays.asList(values));
    }

    public FilterResolver getFilterResolver() {
        return new FilterResolver(this);
    }

    /**
     * Creates a {@link BoundingBox} instance from given spatial request
     * parameters. The resulting bounding box is the merged extent of all
     * spatial filters given. For example if {@value #NEAR} and {@value #BBOX}
     * exist, the returned bounding box includes both extents.
     *
     * @return a spatial filter created from given spatial parameters.
     * @throws IoParseException if parsing parameters fails, or if a requested
     * {@value #CRS} object could not be created.
     */
    public BoundingBox getSpatialFilter() {
        if (!containsParameter(NEAR) && !containsParameter(BBOX)) {
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
        } else {
            extendBy(lowerLeft, bounds);
            extendBy(upperRight, bounds);
            LOGGER.debug("Merged bounds: {}", bounds.toString());
        }
        return bounds;
    }

    /**
     * Extends the bounding box with the given point. If point is contained by
     * this instance nothing is changed.
     *
     * @param point the point in CRS:84 which shall extend the bounding box.
     */
    private void extendBy(Point point, BoundingBox bbox) {
        if (bbox.contains(point)) {
            return;
        }
        double llX = Math.min(point.getX(), bbox.getLowerLeft().getX());
        double llY = Math.max(point.getX(), bbox.getUpperRight().getX());
        double urX = Math.min(point.getY(), bbox.getLowerLeft().getY());
        double urY = Math.max(point.getY(), bbox.getUpperRight().getY());

        CRSUtils crsUtils = createEpsgForcedXYAxisOrder();
        bbox.setLl(crsUtils.createPoint(llX, llY, bbox.getSrs()));
        bbox.setUr(crsUtils.createPoint(urX, urY, bbox.getSrs()));
    }

    /**
     * @return a {@link BBox} instance or <code>null</code> if no {@link #BBOX}
     * parameter is present.
     * @throws IoParseException if parsing parameter fails.
     * @throws IoParseException if a requested {@value #CRS} object could not be
     * created
     */
    private BBox createBbox() {
        if (!containsParameter(BBOX)) {
            return null;
        }
        String bboxValue = getAsString(BBOX);
        BBox bbox = parseJson(bboxValue, BBox.class);
        bbox.setLl(convertToCrs84(bbox.getLl()));
        bbox.setUr(convertToCrs84(bbox.getUr()));
        return bbox;
    }

    private BoundingBox parseBoundsFromVicinity() {
        if (!containsParameter(NEAR)) {
            return null;
        }
        String vicinityValue = getAsString(NEAR);
        Vicinity vicinity = parseJson(vicinityValue, Vicinity.class);
        if (containsParameter(CRS)) {
            vicinity.setCenter(convertToCrs84(vicinity.getCenter()));
        }
        BoundingBox bounds = vicinity.calculateBounds();
        LOGGER.debug("Parsed vicinity bounds: {}", bounds.toString());
        return bounds;
    }

    /**
     * @param jsonString the JSON string to parse.
     * @param clazz the type to serialize given JSON string to.
     * @return a mapped instance parsed from JSON.
     * @throws IoParseException if JSON is invalid or does not map to given
     * type.
     */
    private <T> T parseJson(String jsonString, Class<T> clazz) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonString, clazz);
        } catch (JsonParseException e) {
            throw new IoParseException("The given parameter is invalid JSON." + jsonString, e);
        } catch (JsonMappingException e) {
            throw new IoParseException("The given parameter could not been read: " + jsonString, e);
        } catch (IOException e) {
            throw new RuntimeException("Could not handle input to parse.", e);
        }
    }

    private GeojsonPoint convertToCrs84(GeojsonPoint point) {
        return isForceXY() // is strict XY axis order?!
                ? transformToInnerCrs(point, createEpsgForcedXYAxisOrder())
                : transformToInnerCrs(point, createEpsgStrictAxisOrder());
    }

    /**
     * @param point a GeoJSON point to be transformed to internally used CRS:84.
     * @param crsUtils a reference helper.
     * @return a transformed GeoJSON instance.
     * @throws IoParseException if point could not be transformed, or if
     * requested CRS object could not be created.
     */
    private GeojsonPoint transformToInnerCrs(GeojsonPoint point, CRSUtils crsUtils) {
        try {
            Point toTransformed = crsUtils.convertToPointFrom(point, getCrs());
            Point crs84Point = (Point) crsUtils.transformOuterToInner(toTransformed, getCrs());
            return crsUtils.convertToGeojsonFrom(crs84Point);
        } catch (TransformException e) {
            throw new IoParseException("Could not transform to internally used CRS:84.", e);
        } catch (FactoryException e) {
            throw new IoParseException("Check if 'crs' parameter is a valid EPSG CRS. Was: '" + getCrs() + "'.", e);
        }
    }

    /**
     * @return the requested reference context, or the default
     * ({@value CRSUtils#DEFAULT_CRS}) which will be interpreted as lon/lat
     * ordered axes).
     */
    public String getCrs() {
        if (!containsParameter(CRS)) {
            return DEFAULT_CRS;
        }
        return getAsString(CRS);
    }

    public boolean isForceXY() {
        if (!containsParameter(FORCE_XY)) {
            return DEFAULT_FORCE_XY;
        }
        return getAsBoolean(FORCE_XY);
    }

    public boolean isMatchDomainIds() {
        if (!containsParameter(MATCH_DOMAIN_IDS)) {
            return DEFAULT_MATCH_DOMAIN_IDS;
        }
        return getAsBoolean(MATCH_DOMAIN_IDS);
    }

    /**
     * @return the value of {@value #EXPANDED} parameter.
     * @throws IoParseException if parameter could not be parsed.
     */
    public boolean isExpanded() {
        if (!containsParameter(EXPANDED)) {
            return DEFAULT_EXPANDED;
        }
        return getAsBoolean(EXPANDED);
    }

    public boolean isForceLatestValueRequests() {
        if (!containsParameter(FORCE_LATEST_VALUE)) {
            return DEFAULT_FORCE_LATEST_VALUE;
        }
        return getAsBoolean(FORCE_LATEST_VALUE);
    }

    public boolean isStatusIntervalsRequests() {
        if (!containsParameter(STATUS_INTERVALS)) {
            return DEFAULT_STATUS_INTERVALS;
        }
        return getAsBoolean(STATUS_INTERVALS);
    }

    public boolean isRenderingHintsRequests() {
        if (!containsParameter(RENDERING_HINTS)) {
            return DEFAULT_RENDERING_HINTS;
        }
        return getAsBoolean(RENDERING_HINTS);
    }

    public boolean containsParameter(String parameter) {
        return query.containsKey(parameter.toLowerCase());
    }

    public String getOther(String parameter) {
        return getAsString(parameter);
    }

    public String getAsString(String parameter) {
        return containsParameter(parameter)
                ? asCsv(query.get(parameter.toLowerCase()))
                : null;
    }

    private String asCsv(List<JsonNode> list) {
        StringBuilder sb = new StringBuilder();
        for (JsonNode jsonNode : list) {
            if (sb.length() != 0) {
                sb.append(",");
            }
            sb.append(jsonNode.asText());
        }
        return sb.toString();
    }

    /**
     * @param parameter
     *        the parameter to parse to an <code>int</code> value.
     * @return an integer value.
     * @throws IoParseException
     *         if parsing to <code>int</code> fails.
     */
    public int getAsInteger(String parameter) {
        try {
            String value = getAsString(parameter);
            Integer.parseInt(value);
            return query.getFirst(parameter.toLowerCase()).asInt();
        } catch (NumberFormatException e) {
            throw new IoParseException("Parameter '" + parameter + "' has to be an integer!", e);
        }
    }

    /**
     * @param parameter the parameter to parse to <code>boolean</code>.
     * @return <code>true</code> or <code>false</code> as <code>boolean</code>.
     * @throws IoParseException if parsing to <code>boolean</code> fails.
     */
    public boolean getAsBoolean(String parameter) {
        try {
            String value = getAsString(parameter);
            Boolean.parseBoolean(value);
            return query.getFirst(parameter.toLowerCase()).asBoolean();
        } catch (NumberFormatException e) {
            throw new IoParseException("Parameter '" + parameter + "' has to be 'false' or 'true'!", e);
        }
    }

    public RequestSimpleParameterSet toSimpleParameterSet() {
        RequestSimpleParameterSet parameterSet = new RequestSimpleParameterSet();
        addValuesToParameterSet(parameterSet);
        return parameterSet;
    }

    public RequestStyledParameterSet toRequestStyledParameterSet() {
        RequestStyledParameterSet parameterSet = new RequestStyledParameterSet();
        addValuesToParameterSet(parameterSet);
        return parameterSet;
    }

    private RequestParameterSet addValuesToParameterSet(RequestParameterSet parameterSet) {
        // TODO check value object
        // TODO keep multi value map
        for (Entry<String, List<JsonNode>> entry : query.entrySet()) {
            List<JsonNode> values = entry.getValue();
            String lowercasedKey = entry.getKey().toLowerCase();
            if (values.size() == 1) {
                parameterSet.addParameter(lowercasedKey, values.get(0));
            } else {
                parameterSet.addParameter(lowercasedKey, getJsonNodeFrom(values));
            }
        }
        return parameterSet;
    }

    public static JsonNode getJsonNodeFrom(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return om.readTree(om.writeValueAsString(object));
        } catch (IOException e) {
            LOGGER.error("Could not parse parameter", e);
            return null;
        }
    }

    public IoParameters removeAllOf(String key) {
        MultiValueMap<String, JsonNode> newValues = new LinkedMultiValueMap<>(query);
        newValues.remove(key.toLowerCase());
        return new IoParameters(newValues);
    }

    public IoParameters extendWith(String key, String... values) {
        MultiValueMap<String, String> newValues = new LinkedMultiValueMap<>();
        newValues.put(key.toLowerCase(), Arrays.asList(values));

        MultiValueMap<String, JsonNode> mergedValues = new LinkedMultiValueMap<>(query);
        mergedValues.putAll(convertValuesToJsonNodes(newValues));
        return new IoParameters(mergedValues);
    }

    protected static Map<String, JsonNode> convertValuesToJsonNodes(Map<String, String> queryParameters) {
        Map<String, JsonNode> parameters = new HashMap<>();
        for (Entry<String, String> entry : queryParameters.entrySet()) {
            String key = entry.getKey().toLowerCase();
            parameters.put(key, getJsonNodeFrom(entry.getValue()));
        }
        return parameters;
    }

    protected static MultiValueMap<String, JsonNode> convertValuesToJsonNodes(MultiValueMap<String, String> queryParameters) {
        MultiValueMap<String, JsonNode> parameters = new LinkedMultiValueMap<>();
        final Set<Entry<String, List<String>>> entrySet = queryParameters.entrySet();
        for (Entry<String, List<String>> entry : entrySet) {
            for (String value : entry.getValue()) {
                final String key = entry.getKey().toLowerCase();
                parameters.add(key, getJsonNodeFrom(value));
            }
        }
        return parameters;
    }

    @Override
    public String toString() {
        return "IoParameters{" + "query=" + query + '}';
    }

    public String getHrefBase() {
        return getAsString(Parameters.HREF_BASE);
    }

    /* ****************************************************************
     *                    FACTORY METHODS
     * ************************************************************** */

    public static IoParameters createDefaults() {
        return createDefaults(null);
    }

    static IoParameters createDefaults(File defaultConfig) {
        return new IoParameters(Collections.<String, JsonNode>emptyMap(), defaultConfig);
    }

    static IoParameters createFromMultiValueMap(MultiValueMap<String, String> query) {
        return createFromMultiValueMap(query, null);
    }

    static IoParameters createFromMultiValueMap(MultiValueMap<String, String> query, File defaultConfig) {
        return new IoParameters(convertValuesToJsonNodes(query), defaultConfig);
    }

    static IoParameters createFromSingleValueMap(Map<String, String> query) {
        return createFromSingleValueMap(query, null);
    }

    static IoParameters createFromSingleValueMap(Map<String, String> query, File defaultConfig) {
        return new IoParameters(convertValuesToJsonNodes(query), defaultConfig);
    }

    /**
     * @param parameters the parameters sent via POST payload.
     * @return a query map for convenient parameter access plus validation.
     */
    public static IoParameters createFromQuery(RequestParameterSet parameters) {
        return createFromQuery(parameters, null);
    }

    public static IoParameters createFromQuery(RequestParameterSet parameters, File defaultConfig) {
        Map<String, JsonNode> queryParameters = new HashMap<>();
        for (String parameter : parameters.availableParameterNames()) {
            JsonNode value = parameters.getParameterValue(parameter);
            queryParameters.put(parameter.toLowerCase(), value);
        }
        return new IoParameters(queryParameters, defaultConfig);
    }

    public static IoParameters ensureBackwardsCompatibility(IoParameters parameters) {
        return isBackwardsCompatibilityRequest(parameters)
                ? parameters
                    .extendWith(Parameters.FILTER_PLATFORM_TYPES, "stationary", "insitu")
                    .extendWith(Parameters.FILTER_DATASET_TYPES, "measurement")
                    .removeAllOf(Parameters.HREF_BASE)
                : parameters;
    }

    private static boolean isBackwardsCompatibilityRequest(IoParameters parameters) {
        return !(parameters.containsParameter(Parameters.FILTER_PLATFORM_TYPES)
                || parameters.containsParameter(Parameters.FILTER_DATASET_TYPES));
    }

    public boolean isPureStationaryInsituQuery() {
        Set<String> platformTypes = getPlatformTypes();
        Set<String> datasetTypes = getDatasetTypes();
        return isStationaryInsituOnly(platformTypes)
                && isMeasurementOnly(datasetTypes);
    }

    private boolean isStationaryInsituOnly(Set<String> platformTypes) {
        return platformTypes.size() == 2
                && platformTypes.contains("stationary")
                && platformTypes.contains("insitu");
    }

    private boolean isMeasurementOnly(Set<String> datasetTypes) {
        return datasetTypes.size() == 1
                && datasetTypes.contains("measurement");
    }

}
