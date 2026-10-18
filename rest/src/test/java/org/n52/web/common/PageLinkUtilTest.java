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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

public class PageLinkUtilTest {

    private static final String HREF = "http://localhost:8080/api/v1/stations";

    @Test
    public void whenNotPaginated_thenNoLinkHeadersAdded() {
        Paginated paginated = new Paginated(null, 100);
        RecordingResponse recording = new RecordingResponse();

        PageLinkUtil.addPagingHeaders(HREF, recording.getProxy(), paginated);

        assertThat(recording.getLinkHeaders().size(), is(0));
    }

    @Test
    public void whenFirstPage_thenAddsSelfFirstNextLastHeaders() {
        Pagination page = new OffsetBasedPagination(0, 20);
        Paginated paginated = new Paginated(page, 100);
        RecordingResponse recording = new RecordingResponse();

        PageLinkUtil.addPagingHeaders(HREF, recording.getProxy(), paginated);

        List<String> headers = recording.getLinkHeaders();
        assertThat(headers.size(), is(4));
        assertThat(headers, hasItem("<" + HREF + "?offset=0&limit=20> rel=\"self\""));
        assertThat(headers, hasItem("<" + HREF + "?offset=1&limit=20> rel=\"next\""));
        assertThat(headers, hasItem("<" + HREF + "?offset=0&limit=20> rel=\"first\""));
        assertThat(headers, hasItem("<" + HREF + "?offset=4&limit=20> rel=\"last\""));
        assertThat(headers, not(hasItem("<" + HREF + "?offset=0&limit=20> rel=\"previous\"")));
    }

    @Test
    public void whenMiddlePage_thenAddsAllFiveHeaders() {
        Pagination page = new OffsetBasedPagination(2, 20);
        Paginated paginated = new Paginated(page, 100);
        RecordingResponse recording = new RecordingResponse();

        PageLinkUtil.addPagingHeaders(HREF, recording.getProxy(), paginated);

        List<String> headers = recording.getLinkHeaders();
        assertThat(headers.size(), is(5));
        assertThat(headers, hasItem("<" + HREF + "?offset=2&limit=20> rel=\"self\""));
        assertThat(headers, hasItem("<" + HREF + "?offset=1&limit=20> rel=\"previous\""));
        assertThat(headers, hasItem("<" + HREF + "?offset=3&limit=20> rel=\"next\""));
        assertThat(headers, hasItem("<" + HREF + "?offset=0&limit=20> rel=\"first\""));
        assertThat(headers, hasItem("<" + HREF + "?offset=4&limit=20> rel=\"last\""));
    }

    @Test
    public void whenLastPage_thenAddsSelfPreviousFirstLastWithoutNext() {
        Pagination page = new OffsetBasedPagination(4, 20);
        Paginated paginated = new Paginated(page, 100);
        RecordingResponse recording = new RecordingResponse();

        PageLinkUtil.addPagingHeaders(HREF, recording.getProxy(), paginated);

        List<String> headers = recording.getLinkHeaders();
        assertThat(headers.size(), is(4));
        assertThat(headers, hasItem("<" + HREF + "?offset=4&limit=20> rel=\"self\""));
        assertThat(headers, hasItem("<" + HREF + "?offset=3&limit=20> rel=\"previous\""));
        assertThat(headers, hasItem("<" + HREF + "?offset=0&limit=20> rel=\"first\""));
        assertThat(headers, hasItem("<" + HREF + "?offset=4&limit=20> rel=\"last\""));
    }

    private static class RecordingResponse implements InvocationHandler {
        private final List<String> linkHeaders = new ArrayList<>();

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if ("addHeader".equals(method.getName()) && args != null && args.length == 2 && "Link".equals(args[0])) {
                linkHeaders.add((String) args[1]);
            }
            return null;
        }

        public HttpServletResponse getProxy() {
            return (HttpServletResponse) Proxy.newProxyInstance(
                    HttpServletResponse.class.getClassLoader(),
                    new Class<?>[] { HttpServletResponse.class },
                    this);
        }

        public List<String> getLinkHeaders() {
            return linkHeaders;
        }
    }
}
