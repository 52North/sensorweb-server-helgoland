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
package org.n52.web.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PaginatedTest {

    @Test
    public void whenCurrentPaginationIsNull_thenNotPaginatedAndAllLinksAbsent() {
        Paginated paginated = new Paginated(null, 100);

        assertFalse(paginated.isPaginated());
        assertFalse(paginated.hasFirst());
        assertFalse(paginated.hasLast());
        assertFalse(paginated.hasPrevious());
        assertFalse(paginated.hasNext());

        assertFalse(paginated.getCurrent().isPresent());
        assertFalse(paginated.getFirst().isPresent());
        assertFalse(paginated.getLast().isPresent());
        assertFalse(paginated.getPrevious().isPresent());
        assertFalse(paginated.getNext().isPresent());

        assertThat(paginated.getTotalCount(), is(100L));
        assertThat(paginated.toString(), is("Paginated[first=null, previous=null, current=null, next=null, last=null]"));
    }

    @Test
    public void whenFirstPage_thenFirstNextLastPresentAndPreviousAbsent() {
        Pagination page = new OffsetBasedPagination(0, 20);
        Paginated paginated = new Paginated(page, 100);

        assertTrue(paginated.isPaginated());
        assertThat(paginated.getCurrent().get(), is(page));
        assertTrue(paginated.hasFirst());
        assertTrue(paginated.hasLast());
        assertTrue(paginated.hasNext());
        assertFalse(paginated.hasPrevious());

        assertThat(paginated.getFirst().get().getOffset(), is(0L));
        assertThat(paginated.getLast().get().getOffset(), is(80L));
        assertThat(paginated.getNext().get().getOffset(), is(20L));
        assertFalse(paginated.getPrevious().isPresent());
        assertThat(paginated.getTotalCount(), is(100L));
    }

    @Test
    public void whenMiddlePage_thenAllNavigationLinksPresent() {
        Pagination page = new OffsetBasedPagination(2, 20);
        Paginated paginated = new Paginated(page, 100);

        assertTrue(paginated.hasFirst());
        assertTrue(paginated.hasPrevious());
        assertTrue(paginated.hasNext());
        assertTrue(paginated.hasLast());

        assertThat(paginated.getPrevious().get().getOffset(), is(20L));
        assertThat(paginated.getNext().get().getOffset(), is(60L));
    }

    @Test
    public void whenLastPage_thenNextAbsentAndPreviousPresent() {
        Pagination page = new OffsetBasedPagination(4, 20);
        Paginated paginated = new Paginated(page, 100);

        assertTrue(paginated.hasFirst());
        assertTrue(paginated.hasPrevious());
        assertTrue(paginated.hasLast());
        assertFalse(paginated.hasNext());

        assertThat(paginated.getFirst().get().getOffset(), is(0L));
        assertThat(paginated.getPrevious().get().getOffset(), is(60L));
        assertThat(paginated.getLast().get().getOffset(), is(80L));
    }

    @Test
    public void whenOutOfBoundsPage_thenAllDerivedLinksAbsent() {
        Pagination page = new OffsetBasedPagination(10, 20);
        Paginated paginated = new Paginated(page, 100);

        assertTrue(paginated.isPaginated());
        assertFalse(paginated.hasFirst());
        assertFalse(paginated.hasLast());
        assertFalse(paginated.hasPrevious());
        assertFalse(paginated.hasNext());
    }
}
