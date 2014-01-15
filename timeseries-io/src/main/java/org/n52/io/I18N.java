/**
 * ﻿Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
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
