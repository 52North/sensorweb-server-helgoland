/*
 * Copyright (C) 2013-2020 52°North Initiative for Geospatial Open Source
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

import java.util.Comparator;
import java.util.Optional;
import java.util.StringJoiner;
import org.n52.io.response.OutputCollection;


/**
 *
 *@author Christian Autermann autermann@uni-muenster.de
 *@author Jan Speckamp
 */
public class Paginated<T> extends OutputCollection<T> {
    private final Optional<Pagination> current;
    private final Optional<Pagination> last;
    private final Optional<Pagination> first;
    private final Optional<Pagination> prev;
    private final Optional<Pagination> next;
    private final long elements;

    public Paginated(Pagination current, long elements) {
        this.current = Optional.ofNullable(current);
        this.elements = elements;

        if (this.current.isPresent()) {
            this.last = this.current.get().last(this.elements);
            this.first = this.current.get().first(this.elements);
            this.prev = this.current.get().previous(this.elements);
            this.next = this.current.get().next(this.elements);
        } else {
            Optional<Pagination> absent = Optional.empty();
            this.last = absent;
            this.first = absent;
            this.prev = absent;
            this.next = absent;
        }

    }

    public Optional<Pagination> getLast() {
        return last;
    }

    public boolean hasLast() {
        return this.last.isPresent();
    }

    public Optional<Pagination> getNext() {
        return this.next;
    }

    public boolean hasNext() {
        return this.next.isPresent();
    }

    public Optional<Pagination> getCurrent() {
        return this.current;
    }

    public Optional<Pagination> getPrevious() {
        return this.prev;
    }

    public boolean hasPrevious() {
        return this.prev.isPresent();
    }

    public Optional<Pagination> getFirst() {
        return this.first;
    }

    public boolean hasFirst() {
        return this.first.isPresent();
    }

    public boolean isPaginated() {
        return this.current.isPresent();
    }

    public long getTotalCount() {
        return this.elements;
    }

    // TODO(specki):
    @Override
    public String toString() {
        return new StringJoiner(", ", Paginated.class.getSimpleName() + "[", "]")
            .add("first=" + getFirst().orElse(null))
            .add("previous=" + getPrevious().orElse(null))
            .add("current=" + getCurrent().orElse(null))
            .add("next=" + getNext().orElse(null))
            .add("last=" + getLast().orElse(null))
            .toString();
    }

    @Override
    protected Comparator<T> getComparator() {
        //TODO(specki): Implementation
        return null;
    }

}
