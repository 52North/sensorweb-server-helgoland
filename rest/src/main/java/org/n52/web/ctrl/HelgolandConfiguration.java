/*
 * Copyright (C) 2013-2021 52°North Initiative for Geospatial Open Source
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
package org.n52.web.ctrl;

import java.net.URI;

import org.joda.time.Period;
import org.n52.faroe.annotation.Configurable;
import org.n52.faroe.annotation.Setting;
import org.n52.web.common.RequestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Configurable
public class HelgolandConfiguration {

    public static final String DEFAULT_URL = "http://localhost:8080";
    public static final String DEFAULT_REQUEST_INTERVAL_RESTRICTION = "P370D";
    public static final String DEFAULT_DEFAULT_LOCALE = "en";
    private static final String EXTERNAL_URL_KEY = "helgoland.externa.url";
    private static final String REQUEST_INTERVAL_RESTRICTION_KEY = "helgoland.request.interval.restriction";
    private static final String REQUEST_DEFAULT_LOCALE_KEY = "helgoland.request.default.locale";

    @Value("${external.url:http://localhost:8080}")
    private String externalUrl;

    private URI externalHelgolandUri;

    @Value("${requestIntervalRestriction:P370D}")
    private String requestIntervalRestriction;

    @Value("${requestDefaultLocale:en}")
    private String defaultLocale = DEFAULT_DEFAULT_LOCALE;

    public String getExternalUrl() {
        return checkForApi(externalHelgolandUri != null ? externalHelgolandUri.toString() : externalUrl);
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = RequestUtils.resolveQueryLessRequestUrl(externalUrl);
    }

    @Setting(EXTERNAL_URL_KEY)
    public void setExternalHelgolandUri(URI externalHelgolandUri) {
        this.externalHelgolandUri = externalHelgolandUri;
    }

    public String getRequestIntervalRestriction() {
        return requestIntervalRestriction != null ? requestIntervalRestriction : DEFAULT_REQUEST_INTERVAL_RESTRICTION;
    }

    @Setting(REQUEST_INTERVAL_RESTRICTION_KEY)
    public void setRequestIntervalRestriction(String requestIntervalRestriction) {
        Period.parse(requestIntervalRestriction);
        this.requestIntervalRestriction = requestIntervalRestriction;
    }

    @Setting(REQUEST_DEFAULT_LOCALE_KEY)
    public void setDefaultLocale(String defaultLocale) {
        if (defaultLocale != null && !defaultLocale.isEmpty()) {
            this.defaultLocale = defaultLocale;
        }
    }

    public String getDefaultLocale() {
        return defaultLocale;
    }

    protected String checkForApi(String url) {
        return url.endsWith(UrlSettings.BASE) ? url
                : url.endsWith("/api") ? url.concat("/")
                        : url.endsWith("/") ? url.substring(0, url.lastIndexOf("/")).concat(UrlSettings.BASE)
                                : url.concat(UrlSettings.BASE);
    }
}
