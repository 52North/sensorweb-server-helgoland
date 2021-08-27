/*
 * Copyright (C) 2013-2021 52°North Spatial Information Research GmbH
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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;

import javax.servlet.http.HttpServletRequest;

import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.janmayen.i18n.LocaleHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class RequestUtils {

    private static final String REQUEST_URL_FALLBACK = "http://localhost:8080";

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestUtils.class);

    private static final String ANY_LOCALE_STRING = "*";

    public static IoParameters overrideQueryLocaleWhenSet(String httpLocale, String defaultLocale,
            IoParameters query) {
        // TODO: Discuss which definition should be stronger HTTP-Header (locale) or the query parameter?
        IoParameters params = query == null ? IoParameters.createDefaults() : query;
        String checkForDefault = checkForDefault(httpLocale, defaultLocale, params);
        return checkForDefault != null ? params.replaceWith(Parameters.LOCALE, checkForDefault)
                : params.removeAllOf(Parameters.LOCALE).setDefaultLocale(defaultLocale);
    }

    private static String checkForDefault(String httpLocale, String defaultLocaleString, IoParameters query) {
        Locale defaulLocale = LocaleHelper.decode(defaultLocaleString);
        if (query.getLocale() != null && !query.getLocale().isEmpty()) {
            Locale queried = LocaleHelper.decode(query.getLocale());
            return checkLocales(queried, defaulLocale);
        } else if (httpLocale != null && !httpLocale.isEmpty()) {
            List<LanguageRange> localeRange = Locale.LanguageRange.parse(httpLocale);
            LanguageRange highest = getHighestPriority(localeRange);
            if (highest != null) {
                if (highest.getRange().equals(ANY_LOCALE_STRING)) {
                    return null;
                }
                Locale lookup = Locale.lookup(Arrays.asList(highest), Arrays.asList(defaulLocale));
                return lookup == null ? httpLocale : checkLocales(lookup, defaulLocale);
            }
            Locale lookup = Locale.lookup(localeRange, Arrays.asList(defaulLocale));
            return lookup == null ? httpLocale : checkLocales(lookup, defaulLocale);
        }
        return null;
    }

    private static LanguageRange getHighestPriority(List<LanguageRange> ranges) {
        LanguageRange lr = null;
        for (LanguageRange languageRange : ranges) {
            if (lr == null || lr.getWeight() < languageRange.getWeight()) {
                lr = languageRange;
            }
        }
        return lr;
    }

    private static String checkLocales(Locale queried, Locale defaulLocale) {
        return defaulLocale.equals(queried)
                || defaulLocale.getCountry() != null && !defaulLocale.getCountry().isEmpty()
                || queried.getCountry() != null && !queried.getCountry().isEmpty()
                        && defaulLocale.getCountry().equalsIgnoreCase(queried.getCountry())
                || defaulLocale.getLanguage().equalsIgnoreCase(queried.getLanguage()) ? null : queried.toString();
    }

    /**
     * Get the request {@link URL} without the query parameter
     *
     * @param externalUrl
     *            the external URL.
     * @return Request {@link URL} without query parameter
     */
    public static String resolveQueryLessRequestUrl(String externalUrl) {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        return externalUrl == null || externalUrl.isEmpty() ? createRequestUrl(request)
                : createRequestUrl(externalUrl);
    }

    private static String createRequestUrl(String externalUrl) {
        try {
            // e.g. in proxy envs
            String url = new URL(externalUrl).toString();
            return removeTrailingSlash(url);
        } catch (MalformedURLException e) {
            LOGGER.error("Invalid external url setting. Fallback to '{}'", REQUEST_URL_FALLBACK);
            return REQUEST_URL_FALLBACK;
        }
    }

    private static String createRequestUrl(HttpServletRequest request) {
        try {
            URL url = new URL(request.getRequestURL().toString());
            String scheme = url.getProtocol();
            String userInfo = url.getUserInfo();
            String host = url.getHost();

            int port = url.getPort();

            String path = request.getRequestURI();
            path = removeTrailingSlash(path);

            URI uri = new URI(scheme, userInfo, host, port, path, null, null);
            String requestUrl = uri.toString();

            LOGGER.debug("Resolved external url '{}'.", requestUrl);
            return requestUrl;
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.error("Could not resolve external url. Fallback to '{}'", REQUEST_URL_FALLBACK);
            return REQUEST_URL_FALLBACK;
        }

    }

    private static String removeTrailingSlash(String path) {
        return path != null && path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

}
