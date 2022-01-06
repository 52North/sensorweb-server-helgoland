/*
 * Copyright (C) 2013-2022 52°North Spatial Information Research GmbH
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class OutputCollection<T extends ParameterOutput> implements Iterable<T> {

    private final List<T> items;

    protected OutputCollection() {
        this(new ArrayList<>());
    }

    public OutputCollection(List<T> items) {
        this.items = new ArrayList<>(items);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public final void addItem(T item) {
        items.add(item);
    }

    public final void addItems(Collection<T> toAdd) {
        this.items.addAll(toAdd);
    }

    public void removeItem(T item) {
        items.remove(item);
    }

    public T getItem(int i) {
        return items.get(i);
    }

    public List<T> getItems() {
        Collections.sort(items, getComparator());
        return Collections.unmodifiableList(items);
    }

    public int size() {
        return items.size();
    }

    protected Comparator<T> getComparator() {
        return ParameterOutput.defaultComparator();
    }

    public boolean containsItem(T item) {
        return items.contains(item);
    }

    public Stream<T> stream() {
        return items.stream();
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }

}
