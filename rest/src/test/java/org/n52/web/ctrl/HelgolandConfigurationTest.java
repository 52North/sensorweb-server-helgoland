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
package org.n52.web.ctrl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HelgolandConfigurationTest {

    private static final String EXPECTED_VALUE = "testdfjdsjg";
    private static final String URL = "http://localhost:8080/helgoland";
    private static final String EXPECTED_URL = "http://localhost:8080/helgoland/api/";
    private HelgolandConfiguration config = new HelgolandConfiguration();

    @Test
    public void when_UrlEndsBase() {
        String url = "http://localhost:8080/helgoland".concat(UrlSettings.BASE);
        String validated = config.checkForApi(url);
        assertEquals(EXPECTED_URL, validated);
    }

    @Test
    public void when_UrlEndsBaseMissingSlash() {
        String url = "http://localhost:8080/helgoland".concat("/api");
        String validated = config.checkForApi(url);
        assertEquals(EXPECTED_URL, validated);
    }

    @Test
    public void when_UrlSlash() {
        String url = "http://localhost:8080/helgoland".concat("/");
        String validated = config.checkForApi(url);
        assertEquals(EXPECTED_URL, validated);
    }

    @Test
    public void when_UrlOnly() {
        String url = "http://localhost:8080/helgoland";
        String validated = config.checkForApi(url);
        assertEquals(EXPECTED_URL, validated);
    }
}