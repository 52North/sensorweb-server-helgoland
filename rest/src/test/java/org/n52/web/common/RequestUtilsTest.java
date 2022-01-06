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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;

public class RequestUtilsTest {

    private static final String DEFAULT_LOCALE = "de";

    @Test
    public void test_notLocale() {
        IoParameters params = RequestUtils.overrideQueryLocaleWhenSet(null, DEFAULT_LOCALE, createParameters());
        Assertions.assertNotNull(params);
        Assertions.assertNotNull(params.getLocale());
        Assertions.assertEquals(DEFAULT_LOCALE, params.getLocale());
        Assertions.assertTrue(params.isDefaultLocal());
    }

    @Test
    public void test_params_local_de() {
        IoParameters params = RequestUtils.overrideQueryLocaleWhenSet(null, DEFAULT_LOCALE, createParameters("de"));
        Assertions.assertNotNull(params);
        Assertions.assertNotNull(params.getLocale());
        Assertions.assertEquals(DEFAULT_LOCALE, params.getLocale());
        Assertions.assertTrue(params.isDefaultLocal());
    }

    @Test
    public void test_params_local_de_de() {
        IoParameters params = RequestUtils.overrideQueryLocaleWhenSet(null, DEFAULT_LOCALE, createParameters("de_de"));
        Assertions.assertNotNull(params);
        Assertions.assertNotNull(params.getLocale());
        Assertions.assertEquals(DEFAULT_LOCALE, params.getLocale());
        Assertions.assertTrue(params.isDefaultLocal());
    }

    @Test
    public void test_params_local_en() {
        IoParameters params = RequestUtils.overrideQueryLocaleWhenSet(null, DEFAULT_LOCALE, createParameters("en"));
        Assertions.assertNotNull(params);
        Assertions.assertNotNull(params.getLocale());
        Assertions.assertEquals("en", params.getLocale());
        Assertions.assertFalse(params.isDefaultLocal());
    }

    @Test
    public void test_http_local_de() {
        IoParameters params = RequestUtils.overrideQueryLocaleWhenSet("de", DEFAULT_LOCALE, createParameters());
        Assertions.assertNotNull(params);
        Assertions.assertNotNull(params.getLocale());
        Assertions.assertEquals(DEFAULT_LOCALE, params.getLocale());
        Assertions.assertTrue(params.isDefaultLocal());
    }

    @Test
    public void test_http_local_en() {
        IoParameters params = RequestUtils.overrideQueryLocaleWhenSet("en", DEFAULT_LOCALE, createParameters());
        Assertions.assertNotNull(params);
        Assertions.assertNotNull(params.getLocale());
        Assertions.assertEquals("en", params.getLocale());
        Assertions.assertFalse(params.isDefaultLocal());
    }

    @Test
    public void test_params_de_http_en() {
        IoParameters params = RequestUtils.overrideQueryLocaleWhenSet("en", DEFAULT_LOCALE, createParameters("de"));
        Assertions.assertNotNull(params);
        Assertions.assertNotNull(params.getLocale());
        Assertions.assertEquals(DEFAULT_LOCALE, params.getLocale());
        Assertions.assertTrue(params.isDefaultLocal());
    }

    @Test
    public void test_params_en_http_de() {
        IoParameters params = RequestUtils.overrideQueryLocaleWhenSet("de", DEFAULT_LOCALE, createParameters("en"));
        Assertions.assertNotNull(params);
        Assertions.assertNotNull(params.getLocale());
        Assertions.assertEquals("en", params.getLocale());
        Assertions.assertFalse(params.isDefaultLocal());
    }

    @Test
    public void test_http_locale() {
        IoParameters params = RequestUtils.overrideQueryLocaleWhenSet("de,en-US;q=0.7,en;q=0.3", DEFAULT_LOCALE, createParameters());
        Assertions.assertNotNull(params);
        Assertions.assertNotNull(params.getLocale());
        Assertions.assertEquals(DEFAULT_LOCALE, params.getLocale());
        Assertions.assertTrue(params.isDefaultLocal());
    }

    @Test
    public void test_http_locale_default_en() {
        IoParameters params = RequestUtils.overrideQueryLocaleWhenSet("de,en-US;q=0.7,en;q=0.3", "en", createParameters());
        Assertions.assertNotNull(params);
        Assertions.assertNotNull(params.getLocale());
        Assertions.assertEquals("de,en-US;q=0.7,en;q=0.3", params.getLocale());
        Assertions.assertFalse(params.isDefaultLocal());
    }

    @Test
    public void test_http_locale_any() {
        IoParameters params = RequestUtils.overrideQueryLocaleWhenSet("*", DEFAULT_LOCALE, createParameters());
        Assertions.assertNotNull(params);
        Assertions.assertNotNull(params.getLocale());
        Assertions.assertEquals(DEFAULT_LOCALE, params.getLocale());
        Assertions.assertTrue(params.isDefaultLocal());
    }

    private IoParameters createParameters(String locale) {
        return createParameters().replaceWith(Parameters.LOCALE, locale);
    }

    private IoParameters createParameters() {
        return IoParameters.createDefaults();
    }
}
