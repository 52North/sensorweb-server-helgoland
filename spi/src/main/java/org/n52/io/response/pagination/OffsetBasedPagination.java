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
package org.n52.io.response.pagination;

import java.util.Objects;
import java.util.Optional;

/**
 *
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
        this.limit = limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        this.offset = offset <= 0 ? 0 : offset * limit;
        this.start = this.offset;
        this.end = this.offset + this.limit;
    }

    @Override
    public long getStart() {
        return this.start;
    }

    @Override
    public long getEnd() {
        return this.end;
    }

    @Override
    public long getOffset() {
        return this.offset;
    }

    @Override
    public long getLimit() {
        return this.limit;
    }

    @Override
    public Optional<Pagination> first(long elements) {
        if (offset >= elements) {
            return Optional.empty();
        } else {
            return Optional.<Pagination>of(new OffsetBasedPagination(0, limit));
        }
    }

    @Override
    public Optional<Pagination> previous(long elements) {
        if (offset >= elements || offset == 0 || offset >= elements) {
            return Optional.empty();
        } else {
            return Optional.<Pagination>of(new OffsetBasedPagination(offset / limit - 1, limit));
        }
    }

    @Override
    public Optional<Pagination> next(long elements) {
        if (offset >= elements || offset + limit  >= elements) {
            return Optional.empty();
        } else {
            return Optional.<Pagination>of(new OffsetBasedPagination(offset / limit + 1, limit));
        }
    }

    @Override
    public Optional<Pagination> last(long elements) {
        long maxOffset = elements - ((elements % limit == 0) ? limit : (elements % limit));
        if (offset >= elements) {
            return Optional.empty();
        } else {
            return Optional.<Pagination>of(new OffsetBasedPagination(maxOffset / limit, limit));
        }
    }

        @Override
    public int hashCode() {
        return Objects.hash(this.offset / this.limit, this.limit);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OffsetBasedPagination) {
            OffsetBasedPagination that = (OffsetBasedPagination) obj;
            return this.getOffset() == that.getOffset() &&
                   this.getLimit() == that.getLimit();
        }
        return false;
    }

    @Override
    public String toString() {
        return "offset=" + this.offset / this.limit + "&limit=" + this.limit;
    }

}
