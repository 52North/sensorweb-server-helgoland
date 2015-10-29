/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2 as publishedby the Free
 * Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of the
 * following licenses, the combination of the program with the linked library is
 * not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed under
 * the aforementioned licenses, is permitted by the copyright holders if the
 * distribution is compliant with both the GNU General Public License version 2
 * and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
package org.n52.io.response.v2;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class OutputCollection<T> implements Iterable<T> {
    
    private final List<T> items;
    
    protected OutputCollection() {
        this.items = Collections.emptyList();
    }

    protected OutputCollection(T item) {
    	this();
    	this.items.add(item);
	}

	protected OutputCollection(List<T> itmes) {
		this.items = itmes;
    }
    
    public void addItem(T item) {
        items.add(item);
    }
    
    public void removeItem(T item) {
        items.remove(item);
    }
    
    public Collection<T> getItems() {
        return Collections.unmodifiableCollection(items);
    }
    
    public boolean containsItem(T item) {
        return items.contains(item);
    }
    
    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }
    
    
}
