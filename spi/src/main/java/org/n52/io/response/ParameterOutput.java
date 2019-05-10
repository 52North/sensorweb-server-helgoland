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
package org.n52.io.response;

import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.n52.io.request.IoParameters;
import org.n52.series.spi.srv.RawFormats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class ParameterOutput implements CollatorComparable<ParameterOutput>, RawFormats {

    public static final String ID = "id";
    public static final String HREF = "href";
    public static final String HREF_BASE = HREF;
    public static final String DOMAIN_ID = "domainid";
    public static final String LABEL = "label";
    public static final String EXTRAS = "extras";
    public static final String RAWFORMATS = "service";

    private String id;

    private OptionalOutput<String> href;

    private OptionalOutput<String> hrefBase;

    private OptionalOutput<String> domainId;

    private OptionalOutput<String> label;

    @Deprecated
    private OptionalOutput<String> license;

    private OptionalOutput<Collection<String>> extras;

    private OptionalOutput<Set<String>> rawFormats;

    public <T> void setValue(String parameter,
                             T value,
                             IoParameters parameters,
                             Consumer<OptionalOutput<T>> consumer) {
        Set<String> fields = parameters.getFields();
        boolean serialize = fields.isEmpty() || fields.contains(parameter);
        consumer.accept(OptionalOutput.of(value, serialize));
    }

    protected <T> T getIfSerialized(OptionalOutput<T> optional) {
        return getIfSet(optional, false);
    }

    protected <T extends Collection<E>, E> T getIfSerializedCollection(OptionalOutput<T> optional) {
        return getIfSetCollection(optional, false);
    }

    protected <K, T> Map<K, T> getIfSerializedMap(OptionalOutput<Map<K, T>> optional) {
        return getIfSetMap(optional, false);
    }

    protected <T> T getIfSet(OptionalOutput<T> optional, boolean forced) {
        return isSet(optional)
                ? optional.getValue(forced)
                : null;
    }

    protected <T extends Collection<E>, E> T getIfSetCollection(OptionalOutput<T> optional, boolean forced) {
        return resolvesToNonNullValue(optional) && !optional.getValue()
                                                            .isEmpty()
                                                                    ? optional.getValue(forced)
                                                                    : null;
    }

    protected <K, T> Map<K, T> getIfSetMap(OptionalOutput<Map<K, T>> optional, boolean forced) {
        return resolvesToNonNullValue(optional) && !optional.getValue()
                                                            .isEmpty()
                                                                    ? optional.getValue(forced)
                                                                    : null;
    }

    protected <T> boolean isSet(OptionalOutput<T> optional) {
        return optional != null && optional.isPresent();
    }

    protected <T> boolean resolvesToNonNullValue(OptionalOutput<T> optional) {
        return isSet(optional) && optional.isSerialize();
    }

    public String getId() {
        return id;
    }

    public ParameterOutput setId(String id) {
        this.id = id;
        return this;
    }

    public String getHref() {
        if (getHrefBase() == null && href == null) {
            return null;
        }
        return !isSet(href) && getHrefBase() != null
                ? getHrefBase() + "/" + getId()
                : href.getValue();
    }

    public ParameterOutput setHref(OptionalOutput<String> href) {
        this.href = href;
        return this;
    }

    @JsonIgnore
    public String getHrefBase() {
        return getIfSerialized(hrefBase);
    }

    public ParameterOutput setHrefBase(OptionalOutput<String> hrefBase) {
        this.hrefBase = hrefBase;
        return this;
    }

    /**
     * Returns the domain id of the parameter, e.g. a natural id (not arbitrarily generated) or the original
     * id actually being used by proxied data sources.
     *
     * @return the domain id
     */
    public String getDomainId() {
        return getIfSerialized(domainId);
    }

    /**
     * Sets the domain id of the parameter, e.g. a natural (not arbitrarily generated) id or the original id
     * actually being used by proxied data sources.
     *
     * @param domainId
     *        the domain id of the parameter
     * @return the instance to enable chaining
     */
    public ParameterOutput setDomainId(OptionalOutput<String> domainId) {
        this.domainId = domainId;
        return this;
    }

    /**
     * @return the label. Returns null if label is not set.
     */
    public String getLabel() {
        return getIfSerialized(label);
    }

    public ParameterOutput setLabel(OptionalOutput<String> label) {
        this.label = label;
        return this;
    }

    @Deprecated
    public String getLicense() {
        return getIfSerialized(license);
    }

    @Deprecated
    public ParameterOutput setLicense(OptionalOutput<String> license) {
        this.license = license;
        return this;
    }

    /**
     * @return a list of extra identifiers available via <tt>/{resource}/extras</tt>
     */
    public Collection<String> getExtras() {
        return getIfSerializedCollection(extras);
    }

    public ParameterOutput setExtras(OptionalOutput<Collection<String>> extras) {
        this.extras = extras;
        return this;
    }

    @Override
    public Set<String> getRawFormats() {
        return getIfSerializedCollection(rawFormats);
    }

    @Override
    public ParameterOutput setRawFormats(OptionalOutput<Set<String>> formats) {
        this.rawFormats = formats;
        return this;
    }

    @Override
    public int compare(Collator collator, ParameterOutput o) {
        String thisLabel = getLabel();
        String otherLabel = o.getLabel();
        return collator.compare(thisLabel.toLowerCase(), otherLabel.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, domainId, label);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ParameterOutput)) {
            return false;
        }
        ParameterOutput other = (ParameterOutput) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(domainId, other.domainId)
                && Objects.equals(label, other.label);
    }

    /**
     * Takes the labels to compare.
     *
     * @param <T>
     *        the actual type.
     * @return a label comparing {@link Comparator}
     */
    public static <T extends ParameterOutput> Comparator<T> defaultComparator() {
        return (T o1, T o2) -> {
            // some outputs don't have labels, e.g. GeometryInfo
            Comparator<String> nullsFirst = Comparator.nullsFirst(Comparator.naturalOrder());
            return Comparator.comparing(ParameterOutput::getLabel, nullsFirst)
                             .thenComparing(ParameterOutput::getId)
                             .compare(o1, o2);
        };
    }

}
