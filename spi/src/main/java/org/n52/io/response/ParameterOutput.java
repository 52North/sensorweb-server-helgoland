/*
 * Copyright (C) 2013-2021 52°North Spatial Information Research GmbH
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

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.n52.io.HrefHelper;
import org.n52.io.request.IoParameters;
import org.n52.series.spi.srv.RawFormats;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class ParameterOutput extends SelfSerializedOutput implements RawFormats, Comparable<ParameterOutput> {

    public static final String ID = "id";
    public static final String HREF = "href";
    public static final String HREF_BASE = HREF;
    public static final String DOMAIN_ID = "domainId";
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
        consumer.accept(OptionalOutput.of(value));
    }

    public String getId() {
        return id;
    }

    public ParameterOutput setId(String id) {
        this.id = id;
        return this;
    }

    public String getHref() {
        if ((getHrefBase() == null) && (href == null)) {
            return null;
        }
        return !isSet(href) && (getHrefBase() != null)
                ? HrefHelper.constructHref(getHrefBase(), getCollectionName()) + "/" + getId()
                : isSet(href)
                    ? href.getValue()
                    : HrefHelper.constructHref(".", getCollectionName()) + "/" + getId();
    }

    @JsonIgnore
    public String getCollectionName() {
        return "";
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
     * @return a list of extra identifiers available via <code>/{resource}/extras</code>
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
    public int hashCode() {
        return Objects.hash(id, domainId, label);
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof ParameterOutput)) {
            return false;
        }
        ParameterOutput other = (ParameterOutput) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(domainId, other.domainId)
                && Objects.equals(label, other.label);
    }

    @Override
    public int compareTo(ParameterOutput o) {
        return Objects.compare(this, o, idComparator());
    }

    /**
     * Takes the default comparator to compare.
     *
     * @param <T>
     *        the actual type.
     * @return a label or id comparing {@link Comparator}
     */
    public static <T extends ParameterOutput> Comparator<T> defaultComparator() {
        return labelComparator();
    }

    /**
     * Takes the labels to compare.
     *
     * @param <T>
     *        the actual type.
     * @return a label comparing {@link Comparator}
     */
    public static <T extends ParameterOutput> Comparator<T> labelComparator() {
        return (T o1, T o2) -> {
            // some outputs don't have labels, e.g. GeometryInfo
            Comparator<String> nullsFirst = Comparator.nullsFirst(Comparator.naturalOrder());
            return Comparator.comparing(ParameterOutput::getLabel, nullsFirst)
                             .thenComparing(ParameterOutput::getId)
                             .compare(o1, o2);
        };
    }


    /**
     * Takes the ids to compare.
     *
     * @param <T>
     *        the actual type.
     * @return a id comparing {@link Comparator}
     */
    public static <T extends ParameterOutput> Comparator<T> idComparator() {
        return (T o1, T o2) -> {
            Comparator<String> nullsFirst = Comparator.nullsFirst(Comparator.naturalOrder());
            return Comparator.comparing(ParameterOutput::getId, nullsFirst).compare(o1, o2);
        };
    }

}
