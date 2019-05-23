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

package org.n52.web.common;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Christian Autermann
 * @author Jan Speckamp
 */

public class OffsetBasedPagination implements Pagination {

    private final long limit;
    private final long offset;
    private final long start;
    private final long end;

    public OffsetBasedPagination() {
        this(0, 0);
    }

    public OffsetBasedPagination(long offset, long limit) {
        this.limit = limit <= 0
                ? DEFAULT_LIMIT
                : Math.min(limit, MAX_LIMIT);
        this.offset = offset <= 0
                ? 0
                : offset * limit;
        this.start = this.offset;
        this.end = this.offset + this.limit;
    }

    @Override
    public long getStart() {
        return start;
    }

    @Override
    public long getEnd() {
        return end;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public long getLimit() {
        return limit;
    }

    @Override
    public Optional<Pagination> first(long elements) {
        return offset >= elements
                ? Optional.empty()
                : Optional.of(new OffsetBasedPagination(0, limit));
    }

    @Override
    public Optional<Pagination> previous(long elements) {
        return offset >= elements || offset == 0 || offset >= elements
                ? Optional.empty()
                : Optional.of(new OffsetBasedPagination(offset / limit - 1, limit));
    }

    @Override
    public Optional<Pagination> next(long elements) {
        return offset >= elements || offset + limit >= elements
                ? Optional.empty()
                : Optional.of(new OffsetBasedPagination(offset / limit + 1, limit));
    }

    @Override
    public Optional<Pagination> last(long elements) {
        long maxOffset = calcMaxOffset(elements);
        return offset >= elements
                ? Optional.empty()
                : Optional.of(new OffsetBasedPagination(maxOffset / limit, limit));
    }

    private long calcMaxOffset(long elements) {
        long size = (elements % limit != 0)
                ? (elements % limit)
                : limit;
        return elements - size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(offset / limit, limit);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OffsetBasedPagination) {
            OffsetBasedPagination that = (OffsetBasedPagination) obj;
            return getOffset() == that.getOffset()
                    && getLimit() == that.getLimit();
        }
        return false;
    }

    @Override
    public String toString() {
        return "offset=" + offset / limit + "&limit=" + limit;
    }

}
