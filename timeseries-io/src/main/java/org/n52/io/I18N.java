/**
 * ﻿Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.io;

import static java.util.ResourceBundle.getBundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

public final class I18N {

    private static final String MESSAGES = "locales/messages";

    private final ResourceBundle bundle;

    private Locale locale;

    private I18N(ResourceBundle bundle, Locale locale) {
        this.locale = locale;
        this.bundle = bundle;
    }

    public String get(String string) {
        return bundle.getString(string);
    }

    /**
     * @return the 2-char language code.
     * @see Locale#getLanguage()
     */
    public String getLocale() {
        return locale.getLanguage();
    }

    public static I18N getDefaultLocalizer() {
        return getMessageLocalizer("en");
    }

    public static I18N getMessageLocalizer(String languageCode) {
        Locale locale = createLocate(languageCode);
        return new I18N(getBundle(MESSAGES, locale, new UTF8Control()), locale);
    }

    private static Locale createLocate(String language) {
        if (language == null) {
            return new Locale("en");
        }
        String[] localeParts = language.split("_");
        if (localeParts.length == 0 || localeParts.length > 3) {
            throw new IllegalArgumentException("Unparsable language parameter: " + language);
        }
        if (localeParts.length == 1) {
            return new Locale(localeParts[0]);
        }
        else if (localeParts.length == 2) {
            return new Locale(localeParts[0], localeParts[1]);
        }
        else {
            return new Locale(localeParts[0], localeParts[1], localeParts[2]);
        }
    }

    /**
     * Overrides {@link Control#newBundle(String, Locale, String, ClassLoader, boolean)} as given in
     * {@link Control}'s JavaDoc example to handle UTF-8 localization bundles.
     */
    private static class UTF8Control extends Control {
        
        @Override
        public List<String> getFormats(String baseName) {
            return FORMAT_PROPERTIES;
        }

        /*
         * Implementation taken from Control JavaDoc example.
         * 
         * (non-Javadoc)
         * 
         * @see java.util.ResourceBundle.Control#newBundle(java.lang.String, java.util.Locale,
         * java.lang.String, java.lang.ClassLoader, boolean)
         */
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                        ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            if (baseName == null || locale == null || format == null || loader == null) {
                throw new NullPointerException();
            }
            ResourceBundle bundle = null;
            if (format.equals("java.properties")) {
                String bundleName = toBundleName(baseName, locale);
                final String resourceName = toResourceName(bundleName, "properties");
                final ClassLoader classLoader = loader;
                final boolean reloadFlag = reload;
                InputStream stream = null;
                if (reloadFlag) {
                    URL url = classLoader.getResource(resourceName);
                    if (url != null) {
                        URLConnection connection = url.openConnection();
                        if (connection != null) {
                            // Disable caches to get fresh data for
                            // reloading.
                            connection.setUseCaches(false);
                            stream = connection.getInputStream();
                        }
                    }
                }
                else {
                    stream = classLoader.getResourceAsStream(resourceName);
                }
                if (stream != null) {
                    try {
                        InputStreamReader utf8StreamReader = new InputStreamReader(stream, "UTF-8");
                        bundle = new PropertyResourceBundle(utf8StreamReader);
                    }
                    finally {
                        stream.close();
                    }
                }
            }
            else {
                throw new IllegalArgumentException("Only java.properties format allowed! Was: " + format);
            }
            return bundle;
        }
    }

}
