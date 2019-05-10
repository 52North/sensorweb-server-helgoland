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

import static java.util.stream.Collectors.toSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormatter;
import org.n52.io.IntervalWithTimeZone;
import org.n52.io.IoParseException;
import org.n52.io.crs.BoundingBox;
import org.n52.io.crs.CRSUtils;
import org.n52.io.geojson.old.GeojsonPoint;
import org.n52.io.response.PlatformType;
import org.n52.io.response.dataset.ValueType;
import org.n52.io.style.LineStyle;
import org.n52.io.style.Style;
import org.n52.shetland.ogc.filter.Filter;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.odata.ODataFesParser;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.vividsolutions.jts.geom.Point;

public final class IoParameters implements Parameters {

    private static final Logger LOGGER = LoggerFactory.getLogger(IoParameters.class);

    private static final String DEFAULT_CONFIG_FILE = "config-general.json";

    // TODO use global object mapper
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final ODataFesParser ODATA_PARSER = new ODataFesParser();

    private final MultiValueMap<String, JsonNode> query;

    private final FilterResolver filterResolver;

    private boolean behaveBackwardsCompatible;

    private BiConsumer<String, IoParseException> parseExceptionHandle;

    protected IoParameters() {
        this(Collections.<String, JsonNode> emptyMap());
    }

    protected IoParameters(IoParameters other) {
        this(other.query);
    }

    protected IoParameters(Map<String, JsonNode> queryParameters) {
        this(queryParameters, (File) null);
    }

    protected IoParameters(Map<String, JsonNode> queryParameters, File defaults) {
        this(defaults);
        query.setAll(mergeToLowerCasedKeys(queryParameters));
    }

    protected IoParameters(MultiValueMap<String, JsonNode> queryParameters) {
        this(queryParameters, (File) null);
    }

    protected IoParameters(MultiValueMap<String, JsonNode> queryParameters, File defaults) {
        this(defaults);
        if (queryParameters != null) {
            query.putAll(mergeToLowerCasedKeys(queryParameters));
        }
    }

    private IoParameters(File defaultConfig) {
        MultiValueMap<String, JsonNode> config = new LinkedMultiValueMap<>();
        config.setAll(readDefaultConfig(defaultConfig));
        query = mergeToLowerCasedKeys(config);
        filterResolver = new FilterResolver(this);
    }

    private Map<String, JsonNode> readDefaultConfig(File config) {
        try (InputStream stream = config == null
                ? getDefaultConfigFile()
                : new FileInputStream(config)) {
            return OBJECT_MAPPER.readValue(stream, TypeFactory.defaultInstance()
                                                              .constructMapLikeType(HashMap.class,
                                                                                    String.class,
                                                                                    JsonNode.class));
        } catch (IOException e) {
            LOGGER.info("Could not load '{}'. Using empty config.", DEFAULT_CONFIG_FILE, e);
            return new HashMap<>();
        }
    }

    private static InputStream getDefaultConfigFile() {
        try {
            Path path = Paths.get(IoParameters.class.getResource("/")
                                                    .toURI());
            File config = path.resolve(DEFAULT_CONFIG_FILE)
                              .toFile();
            final String fallbackPath = "/" + DEFAULT_CONFIG_FILE;
            return config.exists()
                    ? new FileInputStream(config)
                    : IoParameters.class.getClassLoader()
                                        .getResourceAsStream(fallbackPath);
        } catch (URISyntaxException | IOException e) {
            LOGGER.debug("Could not find default config under '{}'", DEFAULT_CONFIG_FILE, e);
            return null;
        }
    }

    public boolean shallBehaveBackwardsCompatible() {
        return behaveBackwardsCompatible;
    }

    private IoParameters setBehaveBackwardsCompatible(boolean behaveBackwardsCompatible) {
        this.behaveBackwardsCompatible = behaveBackwardsCompatible;
        return this;
    }

    public IoParameters setParseExceptionHandle(BiConsumer<String, IoParseException> handle) {
        this.parseExceptionHandle = handle;
        return this;
    }

    private BiConsumer<String, IoParseException> getParseExceptionHandle() {
        return parseExceptionHandle;
    }

    /**
     * @return the value of {@value #OFFSET} parameter. If not present, the default {@value #DEFAULT_OFFSET}
     *         is returned.
     * @throws IoParseException
     *         if parameter could not be parsed.
     */
    public int getOffset() {
        return getAsInteger(OFFSET, DEFAULT_OFFSET);
    }

    /**
     * @return the value of {@value #LIMIT} parameter. If not present, the default {@value #DEFAULT_LIMIT} is
     *         returned.
     * @throws IoParseException
     *         if parameter could not be parsed.
     */
    public int getLimit() {
        return getAsInteger(LIMIT, DEFAULT_LIMIT);
    }

    /**
     * @return the requested chart width in pixels or the default {@value #DEFAULT_WIDTH}.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public int getWidth() {
        return getAsInteger(WIDTH, DEFAULT_WIDTH);
    }

    /**
     * Returns the requested chart height in pixels.
     *
     * @return the requested chart height in pixels or the default {@value #DEFAULT_HEIGHT}.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public int getHeight() {
        return getAsInteger(HEIGHT, DEFAULT_HEIGHT);
    }

    /**
     * Indicates if rendered chart shall be returned as Base64 encoded string.
     *
     * @return the value of parameter {@value #BASE_64} or the default {@value #DEFAULT_BASE_64}.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public boolean isBase64() {
        return getAsBoolean(BASE_64, DEFAULT_BASE_64);
    }

    /**
     * @return <code>true</code> if timeseries chart shall include a background grid.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public boolean isGrid() {
        return getAsBoolean(GRID, DEFAULT_GRID);
    }

    /**
     * @return <code>true</code> if timeseries data shall be generalized.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public boolean isGeneralize() {
        return getAsBoolean(GENERALIZE, DEFAULT_GENERALIZE);
    }

    /**
     * @return <code>true</code> if a legend shall be included when rendering a chart, <code>false</code>
     *         otherwise.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public boolean isLegend() {
        return getAsBoolean(LEGEND, DEFAULT_LEGEND);
    }

    /**
     * @return the value of {@value #LOCALE} parameter. If not present, the default {@value #DEFAULT_LOCALE}
     *         is returned.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public String getLocale() {
        return getAsString(LOCALE, DEFAULT_LOCALE);
    }

    /**
     * @return the value of {@value #STYLE} parameter. If not present, the default styles are returned.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public StyleProperties getSingleStyle() {
        return containsParameter(STYLE)
                ? parseStyleProperties()
                : StyleProperties.createDefaults();
    }

    /**
     * @return the value of {@value #STYLES} parameter.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    public Map<String, StyleProperties> getReferencedStyles() {
        return containsParameter(STYLES)
                ? parseMultipleStyleProperties()
                : Collections.emptyMap();
    }

    /**
     * @return in case of either <tt>style</tt> for single or <tt>styles</tt> for multiple datasets are
     *         available.
     */
    public boolean hasStyles() {
        return getSingleStyle() != null || !getReferencedStyles().isEmpty();
    }

    /**
     * Creates a generic {@link StyleProperties} instance which can be used to create more concrete
     * {@link Style}s. For example use {@link LineStyle#createLineStyle(StyleProperties)} which gives you a
     * style view which can be used for lines.
     *
     * @return a parsed {@link StyleProperties} instance.
     * @throws IoParseException
     *         if parsing parameter fails.
     */
    private StyleProperties parseStyleProperties() {
        return handleJsonValueParseException(STYLE, StyleProperties.class, this::parseJson);
    }

    private Map<String, StyleProperties> parseMultipleStyleProperties() {
        return handleJsonValueParseException(STYLES,
                                             new TypeReference<HashMap<String, StyleProperties>>() {},
                                             this::parseJson);
    }

    public String getFormat() {
        return getAsString(FORMAT, DEFAULT_FORMAT);
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

    public String getTimeFormat() {
        return getAsString(TIME_FORMAT, DEFAULT_TIME_FORMAT);
    }

    /**
     * @return the value of {@value #TIMESPAN} parameter. If not present, the default timespan is returned.
     * @throws IoParseException
     *         if timespan could not be parsed.
     */
    public IntervalWithTimeZone getTimespan() {
        return containsParameter(TIMESPAN)
                ? validateTimespan(getNormalizedTimespan())
                : createDefaultTimespan();
    }

    private String getNormalizedTimespan() {
        return getNormalizedTimespan(null);
    }

    protected String getNormalizedTimespan(DateTimeFormatter dateFormat) {
        String parameterValue = getAsString(TIMESPAN);
        String now = dateFormat == null
                ? new DateTime().toString()
                : dateFormat.print(new DateTime());
        return parameterValue.replaceAll("(?i)now", now);
    }

    public static IntervalWithTimeZone createDefaultTimespan() {
        DateTime now = new DateTime();
        // TODO make this configurable
        DateTime lastWeek = now.minusWeeks(1);
        String interval = lastWeek.toString()
                                  .concat("/")
                                  .concat(now.toString());
        return new IntervalWithTimeZone(interval);
    }

    private IntervalWithTimeZone validateTimespan(String timespan) {
        try {
            return new IntervalWithTimeZone(timespan);
        } catch (IllegalArgumentException e) {
            IoParseException ex = new IoParseException(e.getMessage(), e);
            throw ex.addHint("Valid timespans have to be in ISO8601 period format.")
                    .addHint("Valid examples: 'PT6H/2013-08-13TZ' or '2013-07-13TZ/2013-08-13TZ'.");
        }
    }

    public String getOutputTimezone() {
        if (!containsParameter(OUTPUT_TIMEZONE)) {
            return DEFAULT_OUTPUT_TIMEZONE;
        }
        String timezone = getAsString(OUTPUT_TIMEZONE);
        Set<String> availableIDs = DateTimeZone.getAvailableIDs();
        DateTimeZone zone = availableIDs.contains(timezone)
                ? DateTimeZone.forID(timezone)
                : DateTimeZone.UTC;
        return zone.toString();
    }

    public Instant getResultTime() {
        if (!containsParameter(RESULTTIME)) {
            return null;
        }
        return validateTimestamp(getAsString(RESULTTIME));
    }

    public boolean shallClassifyByResultTimes() {
        return isAllResultTimes() || !getResultTimes().isEmpty();
    }

    public boolean isAllResultTimes() {
        Set<String> resultTimes = csvToLowerCasedSet(getAsString(RESULTTIMES));
        return resultTimes.contains(RESULT_TIMES_VALUE_ALL);
    }

    /**
     * parses csv parameter {@link Parameters#RESULTTIMES} to a set of values. Validates each result time
     * value after removing special value <tt>all</tt>. Use {@link #isAllResultTimes()} to check if client
     * requests all result times available.
     *
     * @return a (probably empty) set of result times.
     */
    public Set<String> getResultTimes() {
        Set<String> resultTimes = csvToSet(getAsString(RESULTTIMES));
        if (resultTimes.contains(RESULT_TIMES_VALUE_ALL)) {
            resultTimes.remove(RESULT_TIMES_VALUE_ALL);
        }
        resultTimes.stream().forEach(this::validateTimestamp);
        Instant fromOldParameter = getResultTime();
        if (fromOldParameter != null) {
            resultTimes.add(fromOldParameter.toString());
        }
        return resultTimes;
    }

    private Instant validateTimestamp(String timestamp) {
        return handleSimpleValueParseException(timestamp, Instant::parse);
    }


    public Optional<Filter<?>> getODataFilter() {
        if (!containsParameter(ODATA_FILTER)) {
            return Optional.empty();
        }
        String parameter = getAsString(ODATA_FILTER);
        if (parameter.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(ODATA_PARSER.decode(parameter));
        } catch (DecodingException ex) {
            handleIoParseException(ODATA_FILTER, createIoParseException(ODATA_FILTER, ex));
            return Optional.empty();
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
        Set<String> values = getValuesOf(CATEGORIES);
        values.addAll(getValuesOf(CATEGORY));
        return values;
    }

    public Set<String> getServices() {
        Set<String> values = getValuesOf(SERVICES);
        values.addAll(getValuesOf(SERVICE));
        return values;
    }

    public Set<String> getOfferings() {
        Set<String> values = getValuesOf(OFFERINGS);
        values.addAll(getValuesOf(OFFERING));
        return values;
    }

    public Set<String> getFeatures() {
        Set<String> values = getValuesOf(FEATURES);
        values.addAll(getValuesOf(FEATURE));
        return values;
    }

    public Set<String> getProcedures() {
        Set<String> values = getValuesOf(PROCEDURES);
        values.addAll(getValuesOf(PROCEDURE));
        return values;
    }

    public Set<String> getPhenomena() {
        Set<String> values = getValuesOf(PHENOMENA);
        values.addAll(getValuesOf(PHENOMENON));
        return values;
    }

    public Set<String> getStations() {
        Set<String> values = getValuesOf(STATIONS);
        values.addAll(getValuesOf(STATION));
        return values;
    }

    public Set<String> getPlatforms() {
        return getValuesOf(PLATFORMS);
    }

    public Set<String> getTimeseries() {
        return getSeries();
    }

    public Set<String> getSeries() {
        Set<String> values = getValuesOf(SERIES);
        values.addAll(getValuesOf(TIMESERIES));
        return values;
    }

    public Set<String> getDatasets() {
        Set<String> values = getSeries();
        values.addAll(getValuesOf(DATASETS));
        return values;
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

    public Set<String> getValueTypes() {
        return getValuesOf(FILTER_VALUE_TYPES);
    }

    public Set<String> getSearchTerms() {
        return getValuesOf(SEARCH_TERM);
    }

    public Set<String> getGeometryTypes() {
        return getValuesOf(GEOMETRY_TYPES);
    }

    Set<String> getValuesOf(String parameterName) {
        return containsParameter(parameterName)
                ? new HashSet<>(csvToLowerCasedSet(getAsString(parameterName)))
                : new HashSet<>(0);
    }

    private Set<String> csvToLowerCasedSet(String csv) {
        return csvToSet(csv, String::toLowerCase);
    }

    private Set<String> csvToSet(String csv) {
        return csvToSet(csv, Function.identity());
    }

    private <O> Set<O> csvToSet(String csv, Function<String, O> c) {
        return Optional.ofNullable(csv)
                .map(str -> str.split(","))
                .flatMap(values -> Optional.ofNullable(c).map(f -> Arrays.stream(values).map(f).collect(toSet())))
                .orElseGet(HashSet::new);
    }

    public FilterResolver getFilterResolver() {
        return filterResolver;
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
        if (!containsParameter(NEAR) && !containsParameter(BBOX)) {
            return null;
        }

        BoundingBox bboxBounds = createBbox();
        BoundingBox bounds = parseBoundsFromVicinity();
        return mergeBounds(bounds, bboxBounds);
    }

    private BoundingBox mergeBounds(BoundingBox bounds, BoundingBox bboxBounds) {
        if (bboxBounds == null) {
            // nothing to merge
            return bounds;
        }
        Point lowerLeft = bboxBounds.getLowerLeft();
        Point upperRight = bboxBounds.getUpperRight();
        if (bounds == null) {
            BoundingBox parsed = new BoundingBox(lowerLeft, upperRight, CRSUtils.DEFAULT_CRS);
            LOGGER.debug("Parsed bbox bounds: {}", parsed.toString());
            return parsed;
        } else {
            extendBy(lowerLeft, bounds);
            extendBy(upperRight, bounds);
            LOGGER.debug("Merged bounds: {}", bounds.toString());
            return bounds;
        }
    }

    /**
     * Extends the bounding box with the given point. If point is contained by this instance nothing is
     * changed.
     *
     * @param point
     *        the point in CRS:84 which shall extend the bounding box.
     */
    private void extendBy(Point point, BoundingBox bbox) {
        bbox.extendBy(point);
    }

    /**
     * @return a {@link BBox} instance or <code>null</code> if no {@link #BBOX} parameter is present.
     * @throws IoParseException
     *         if parsing parameter fails.
     * @throws IoParseException
     *         if a requested {@value #CRS} object could not be created
     */
    private BoundingBox createBbox() {
        if (!containsParameter(BBOX)) {
            return null;
        }
        String bboxValue = getAsString(BBOX);
        CRSUtils crsUtils = CRSUtils.createEpsgForcedXYAxisOrder();

        // Check if supplied in minx,miny,maxx,maxy format - else assume json
        if (bboxValue.matches("^(-?\\d*\\.?\\d*\\,\\s*){3}(-?\\d*\\.?\\d*)\\s*$")) {
            String[] coordArray = bboxValue.split("\\,");
            Point lowerLeft = crsUtils.createPoint(Double.valueOf(coordArray[0].trim()),
                                                   Double.valueOf(coordArray[1].trim()),
                                                   CRSUtils.DEFAULT_CRS);
            Point upperRight = crsUtils.createPoint(Double.valueOf(coordArray[2].trim()),
                                                    Double.valueOf(coordArray[3].trim()),
                                                    CRSUtils.DEFAULT_CRS);
            return new BoundingBox(lowerLeft, upperRight, CRSUtils.DEFAULT_CRS);
        }

        try {
            BBox bbox = handleJsonValueParseException(BBOX, BBox.class, this::parseJson);
            return new BoundingBox(crsUtils.convertToPointFrom(bbox.getLl(), CRSUtils.DEFAULT_CRS),
                                   crsUtils.convertToPointFrom(bbox.getUr(), CRSUtils.DEFAULT_CRS),
                                   CRSUtils.DEFAULT_CRS);
        } catch (IoParseException e) {
            throw e.addHint(createInvalidParameterMessage(Parameters.BBOX))
                   .addHint("Check http://epsg-registry.org for EPSG CRS definitions and codes.")
                   .addHint("(alternate format of 'llLon,llLat,urLon,urLat' couldn't be detected)");
        }

    }

    private BoundingBox parseBoundsFromVicinity() {
        if (!containsParameter(NEAR)) {
            return null;
        }
        Vicinity vicinity = handleJsonValueParseException(NEAR, Vicinity.class, this::parseJson);
        if (containsParameter(CRS)) {
            vicinity.setCenter(convertToCrs84(vicinity.getCenter()));
        }
        BoundingBox bounds = vicinity.calculateBounds();
        LOGGER.debug("Parsed vicinity bounds: {}", bounds.toString());
        return bounds;
    }

    /**
     * @param parameter
     *        the parameter name.
     * @param clazz
     *        the type to serialize given parameter to.
     * @return a mapped instance parsed from given paramter's value.
     * @throws IoParseException
     *         if parameter is invalid JSON or does not map to given type.
     */
    private <T> T parseJson(String parameter, Class<T> clazz) {
        try {
            String value = getAsString(parameter);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(value, clazz);
        } catch (JsonParseException | JsonMappingException e) {
            throw createInvalidJsonValueException(parameter, e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @param parameter
     *        the parameter name.
     * @param typeReference
     *        the type reference to serialize given parameter to.
     * @return a mapped instance parsed from given paramter's value.
     * @throws IoParseException
     *         if parameter is invalid JSON or does not map to given type.
     */
    private <T> T parseJson(String parameter, TypeReference<T> typeReference) {
        try {
            Optional<JsonNode> value = getAsNode(parameter);
            return value.isPresent()
                    ? new ObjectMapper().readerFor(typeReference)
                                        .readValue(value.get())
                    : null;
        } catch (JsonParseException | JsonMappingException e) {
            throw createInvalidJsonValueException(parameter, e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private IoParseException createInvalidJsonValueException(String nodeValue, Exception e) {
        return new IoParseException("The given JSON value is invalid: " + nodeValue, e);
    }

    private GeojsonPoint convertToCrs84(GeojsonPoint point) {
        // is strict XY axis order?!
        return isForceXY()
                ? transformToInnerCrs(point, CRSUtils.createEpsgForcedXYAxisOrder())
                : transformToInnerCrs(point, CRSUtils.createEpsgStrictAxisOrder());
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
    private GeojsonPoint transformToInnerCrs(GeojsonPoint point,
                                             CRSUtils crsUtils) {
        try {
            Point toTransformed = crsUtils.convertToPointFrom(point, getCrs());
            Point crs84Point = (Point) crsUtils.transformOuterToInner(toTransformed, getCrs());
            return crsUtils.convertToGeojsonFrom(crs84Point);
        } catch (TransformException e) {
            throw new IoParseException("Could not transform to internally used CRS:84.", e);
        } catch (FactoryException e) {
            throw new IoParseException("Check if 'crs' parameter is a valid EPSG CRS. Was: '"
                    + getCrs()
                    + "'.", e);
        }
    }

    /**
     * @return the requested reference context, or the default ({@value CRSUtils#DEFAULT_CRS}) which will be
     *         interpreted as lon/lat ordered axes).
     */
    public String getCrs() {
        return getAsString(CRS, CRSUtils.DEFAULT_CRS);
    }

    public boolean isForceXY() {
        return getAsBoolean(FORCE_XY, DEFAULT_FORCE_XY);
    }

    public boolean isMatchDomainIds() {
        return getAsBoolean(MATCH_DOMAIN_IDS, DEFAULT_MATCH_DOMAIN_IDS);
    }

    /**
     * @return the value of {@value #EXPANDED} parameter.
     * @throws IoParseException
     *         if parameter could not be parsed.
     */
    public boolean isExpanded() {
        return getAsBoolean(EXPANDED, DEFAULT_EXPANDED);
    }

    public boolean isForceLatestValueRequests() {
        return getAsBoolean(FORCE_LATEST_VALUE, DEFAULT_FORCE_LATEST_VALUE);
    }

    /**
     * @return if status intervals shall be serialized with (timeseries) output
     * @throws IoParseException
     *         if parameter could not be parsed.
     * @deprecated since v2.0 covered by extras endpoint
     */
    @Deprecated
    public boolean isStatusIntervalsRequests() {
        return getAsBoolean(STATUS_INTERVALS, DEFAULT_STATUS_INTERVALS);
    }

    /**
     * @return if rendering hints shall be serialized with (timeseries) output
     * @throws IoParseException
     *         if parameter could not be parsed.
     * @deprecated since v2.0 covered by extras endpoint
     */
    @Deprecated
    public boolean isRenderingHintsRequests() {
        return getAsBoolean(RENDERING_HINTS, DEFAULT_RENDERING_HINTS);
    }

    public String getHrefBase() {
        return getAsString(Parameters.HREF_BASE);
    }

    public boolean isShowTimeIntervals() {
        return getAsBoolean(SHOW_TIME_INTERVALS, DEFAULT_SHOW_TIME_INTERVALS);
    }

    public boolean isShowVerticalIntervals() {
        return getAsBoolean(SHOW_VERTICAL_INTERVALS, DEFAULT_SHOW_VERTICAL_INTERVALS);
    }

    public boolean containsParameter(String parameter) {
        return query.containsKey(parameter.toLowerCase())
                || query.containsKey(parameter);
    }

    public String getOther(String parameter) {
        return getAsString(parameter);
    }

    public String getAsString(String parameter, String defaultValue) {
        return containsParameter(parameter)
                ? getAsString(parameter)
                : defaultValue;
    }

    public String getAsString(String parameter) {
        if (!containsParameter(parameter)) {
            return null;
        }
        return asCsv(getAsNodes(parameter));
    }

    private Optional<JsonNode> getAsNode(String parameter) {
        return getAsNodes(parameter).stream()
                                    .findFirst();
    }

    private List<JsonNode> getAsNodes(String parameter) {
        return query.get(parameter) == null
                ? query.get(parameter.toLowerCase())
                : query.get(parameter);
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

    public int getAsInteger(String parameter, int defaultValue) {
        return containsParameter(parameter)
                ? handleSimpleValueParseException(parameter, this::getAsInteger)
                : defaultValue;
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
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw createIoParseException(parameter).addHint("Value must be an integer!");
        }
    }

    public boolean getAsBoolean(String parameter, boolean defaultValue) {
        return containsParameter(parameter)
                ? handleSimpleValueParseException(parameter, this::getAsBoolean)
                : defaultValue;
    }

    /**
     * @param parameter
     *        the parameter to parse to <code>boolean</code>.
     * @return <code>true</code> or <code>false</code> as <code>boolean</code>.
     * @throws IoParseException
     *         if parsing to <code>boolean</code> fails.
     */
    public boolean getAsBoolean(String parameter) {
        String value = getAsString(parameter);
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        } else {
            IoParseException ex = createIoParseException(parameter).addHint("Value must be either 'false' or 'true'!");
            return handleIoParseException(parameter, ex);
        }
    }

    private IoParseException createIoParseException(String parameter) {
        return createIoParseException(parameter, null);
    }

    private IoParseException createIoParseException(String parameter, Exception e) {
        return e != null
                ? new IoParseException(createInvalidParameterMessage(parameter), e)
                : new IoParseException(createInvalidParameterMessage(parameter));
    }

    private String createInvalidParameterMessage(String parameter) {
        return "The parameter '" + parameter + "' is invalid.";
    }

    private <R> R handleSimpleValueParseException(String parameter, Function<String, R> supplier) {
        try {
            return supplier.apply(parameter);
        } catch (IoParseException e) {
            return handleIoParseException(parameter, e);
        }
    }

    private <R, T> R handleJsonValueParseException(String parameter,
                                                   Class<T> clazz,
                                                   BiFunction<String, Class<T>, R> supplier) {
        try {
            return supplier.apply(parameter, clazz);
        } catch (IoParseException e) {
            return handleIoParseException(parameter, e);
        }
    }

    private <R, T> R handleJsonValueParseException(String parameter,
                                                   TypeReference<T> reference,
                                                   BiFunction<String, TypeReference<T>, R> supplier) {
        try {
            return supplier.apply(parameter, reference);
        } catch (IoParseException e) {
            return handleIoParseException(parameter, e);
        }
    }

    /**
     * @param parameter
     *        the parameter causing the parse exception.
     * @param e
     *        the parse exception to handle
     * @return nothing, as {@link #parseExceptionHandle} is expected to handle the exception.
     */
    private <R> R handleIoParseException(String parameter, IoParseException e) {
        if (parseExceptionHandle != null) {
            parseExceptionHandle.accept(parameter, e);
            return null;
        } else {
            throw e;
        }
    }

    public static JsonNode getJsonNodeFrom(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(object));
        } catch (IOException e) {
            LOGGER.error("Could not parse parameter", e);
            return null;
        }
    }

    /**
     * Creates a new instance based on the current one and removes given parameter with the all its values. As
     * a new instance is created the current one stays unchanged (unmutable instance) and can be reused as is.
     *
     * @param key
     *        the name of the parameter to remove all its values
     * @return a new instance with extended key/values
     */
    public IoParameters removeAllOf(String key) {
        MultiValueMap<String, JsonNode> newValues = new LinkedMultiValueMap<>(query);
        newValues.remove(key.toLowerCase());
        return new IoParameters(newValues).setParseExceptionHandle(parseExceptionHandle);
    }

    /**
     * Creates a new instance based on the current one and adds parameter with the given values. As a new
     * instance is created the current one stays unchanged (unmutable instance) and can be reused as is.
     *
     * @param key
     *        the parameter name
     * @param values
     *        the parameter values
     * @return a new instance with extended key/values
     */
    public IoParameters extendWith(String key, String... values) {
        MultiValueMap<String, String> newValues = new LinkedMultiValueMap<>();
        newValues.put(key.toLowerCase(), Arrays.asList(values));

        MultiValueMap<String, JsonNode> mergedValues = new LinkedMultiValueMap<>(query);
        mergedValues.putAll(convertToJsonNodes(newValues));
        return new IoParameters(mergedValues).setParseExceptionHandle(parseExceptionHandle);
    }

    public IoParameters replaceWith(String key, String... values) {
        return removeAllOf(key).extendWith(key, values);
    }

    protected static Map<String, JsonNode> convertValuesToJsonNodes(Map<String, String> queryParameters) {
        Map<String, JsonNode> parameters = new HashMap<>();
        for (Entry<String, String> entry : queryParameters.entrySet()) {
            String key = entry.getKey();
            parameters.put(key.toLowerCase(), getJsonNodeFrom(entry.getValue()));
        }
        return parameters;
    }

    protected static MultiValueMap<String, JsonNode> convertToJsonNodes(MultiValueMap<String, String> queryParameters) {
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
        return "IoParameters{ behaveBackwardsCompatible: " + behaveBackwardsCompatible + ", query=" + query + '}';
    }

    protected Map<String, JsonNode> mergeToLowerCasedKeys(Map<String, JsonNode> parameters) {
        Map<String, JsonNode> queryParameters = new HashMap<>();
        for (Entry<String, JsonNode> entry : parameters.entrySet()) {
            String parameter = entry.getKey();
            String lowerCasedKey = parameter.toLowerCase();
            queryParameters.put(lowerCasedKey, parameters.get(parameter));
        }
        return queryParameters;
    }

    protected MultiValueMap<String, JsonNode> mergeToLowerCasedKeys(MultiValueMap<String, JsonNode> parameters) {
        MultiValueMap<String, JsonNode> queryParameters = new LinkedMultiValueMap<>();
        for (Entry<String, List<JsonNode>> entry : parameters.entrySet()) {
            String parameter = entry.getKey();
            String lowerCasedKey = parameter.toLowerCase();
            List<JsonNode> values = parameters.get(parameter);
            if (!queryParameters.containsKey(lowerCasedKey)) {
                queryParameters.put(lowerCasedKey, values);
            } else {
                queryParameters.get(lowerCasedKey)
                               .addAll(values);
            }
        }
        return queryParameters;
    }

    /*
     * ********************* FACTORY METHODS ***************************
     */
    public static IoParameters createDefaults() {
        return createDefaults(null);
    }

    static IoParameters createDefaults(File defaultConfig) {
        return new IoParameters(Collections.<String, JsonNode> emptyMap(), defaultConfig);
    }

    public static IoParameters createFromMultiValueMap(MultiValueMap<String, String> query) {
        return createFromMultiValueMap(query, null);
    }

    private static IoParameters createFromMultiValueMap(MultiValueMap<String, String> query, File defaultConfig) {
        return createFromMultiJsonValueMap(convertToJsonNodes(query), defaultConfig);
    }

    private static IoParameters createFromMultiJsonValueMap(MultiValueMap<String, JsonNode> query, File defaultConfig) {
        return new IoParameters(query, defaultConfig);
    }

    public static IoParameters createFromSingleValueMap(Map<String, String> query) {
        return createFromSingleValueMap(query, null);
    }

    private static IoParameters createFromSingleValueMap(Map<String, String> query, File defaultConfig) {
        return createFromSingleJsonValueMap(convertValuesToJsonNodes(query), defaultConfig);
    }

    static IoParameters createFromSingleJsonValueMap(Map<String, JsonNode> query) {
        return new IoParameters(query, null);
    }

    private static IoParameters createFromSingleJsonValueMap(Map<String, JsonNode> query, File defaultConfig) {
        return new IoParameters(query, defaultConfig);
    }

    public IoParameters respectBackwardsCompatibility() {
        String[] platformTypes = {
            PlatformType.PLATFORM_TYPE_STATIONARY,
            PlatformType.PLATFORM_TYPE_INSITU
        };

        return filterResolver.shallBehaveBackwardsCompatible()
                ? removeAllOf(Parameters.HREF_BASE).extendWith(Parameters.FILTER_PLATFORM_TYPES, platformTypes)
                                                   .extendWith(Parameters.FILTER_VALUE_TYPES,
                                                               ValueType.DEFAULT_VALUE_TYPE)
                                                   // set backwards compatibility at the end
                                                   .setBehaveBackwardsCompatible(true)
                : this;
    }

    public boolean isPureStationaryInsituQuery() {
        Set<String> platformTypes = getPlatformTypes();
        Set<String> datasetTypes = getValueTypes();
        return isStationaryInsituOnly(platformTypes)
                && isQuantityOnly(datasetTypes);
    }

    private boolean isStationaryInsituOnly(Set<String> platformTypes) {
        return platformTypes.size() == 2
                && platformTypes.contains(PlatformType.PLATFORM_TYPE_STATIONARY)
                && platformTypes.contains(PlatformType.PLATFORM_TYPE_INSITU);
    }

    private boolean isQuantityOnly(Set<String> valueTypes) {
        return valueTypes.size() == 1
                && valueTypes.contains(ValueType.DEFAULT_VALUE_TYPE);
    }

}
