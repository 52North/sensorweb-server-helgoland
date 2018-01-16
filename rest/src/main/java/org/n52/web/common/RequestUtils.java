/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.web.common;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
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

    public static IoParameters overrideQueryLocaleWhenSet(String locale, IoParameters query) {
        return locale != null
                ? query.replaceWith(Parameters.LOCALE, locale)
                : query;
    }

    /**
     * Get the request {@link URL} without the query parameter
     *
     * @param externalUrl
     *        the external URL.
     * @return Request {@link URL} without query parameter
     */
    public static String resolveQueryLessRequestUrl(String externalUrl) {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        if (LOGGER.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder("\n----- Start of HTTP Header -----\n");
            Enumeration< ? > headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                sb.append(headerName + ": " + request.getHeader(headerName));
                sb.append("\n");
            }
            sb.append("----- END of HTTP Header -----");
            LOGGER.trace(sb.toString());
        }

        return externalUrl == null || externalUrl.isEmpty()
                ? createRequestUrl(request)
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
            URL url = new URL(request.getRequestURL()
                                     .toString());
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
        return path != null && path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;
    }

}
