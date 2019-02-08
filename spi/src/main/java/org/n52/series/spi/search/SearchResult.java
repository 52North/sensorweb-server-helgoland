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
package org.n52.series.spi.search;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class SearchResult {

    private final String id;

    private final String label;

    private final String baseUrl;

    public SearchResult(String id, String label) {
        this(id, label, null);
    }

    public SearchResult(String id, String label, String baseUrl) {
        this.id = id;
        this.label = label;
        this.baseUrl = baseUrl != null && !baseUrl.endsWith("/")
                ? baseUrl.concat("/")
                : baseUrl;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public boolean hasBaseUrl() {
        return baseUrl != null;
    }

    @JsonIgnore
    public String getBaseUrl() {
        return baseUrl;
    }

    protected String createFullHref() {
        return getBaseUrl() + getId();
    }

    public abstract String getHref();

    public abstract String getType();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SearchResult other = (SearchResult) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
