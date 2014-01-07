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
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
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
     * Customized {@link Control} to enable correct bundle reading in UTF-8.
     */
    private static class UTF8Control extends Control {
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                        ClassLoader loader, boolean reload)
                throws IllegalAccessException, InstantiationException, IOException {
            String bundleName = toBundleName(baseName, locale);
            ResourceBundle bundle = null;
            if (format.equals("java.class")) {
                try {
                    @SuppressWarnings("unchecked")
                    Class< ? extends ResourceBundle> bundleClass = (Class< ? extends ResourceBundle>) loader.loadClass(bundleName);

                    // If the class isn't a ResourceBundle subclass, throw a
                    // ClassCastException.
                    if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
                        bundle = bundleClass.newInstance();
                    }
                    else {
                        throw new ClassCastException(bundleClass.getName()
                                + " cannot be cast to ResourceBundle");
                    }
                }
                catch (ClassNotFoundException e) {
                }
            }
            else if (format.equals("java.properties")) {
                final String resourceName = toResourceName(bundleName, "properties");
                final ClassLoader classLoader = loader;
                final boolean reloadFlag = reload;
                InputStream stream = null;
                try {
                    stream = AccessController.doPrivileged(
                            new PrivilegedExceptionAction<InputStream>() {
                                public InputStream run() throws IOException {
                                    InputStream is = null;
                                    if (reloadFlag) {
                                        URL url = classLoader.getResource(resourceName);
                                        if (url != null) {
                                            URLConnection connection = url.openConnection();
                                            if (connection != null) {
                                                // Disable caches to get fresh data for
                                                // reloading.
                                                connection.setUseCaches(false);
                                                is = connection.getInputStream();
                                            }
                                        }
                                    }
                                    else {
                                        is = classLoader.getResourceAsStream(resourceName);
                                    }
                                    return is;
                                }
                            });
                }
                catch (PrivilegedActionException e) {
                    throw (IOException) e.getException();
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
                throw new IllegalArgumentException("unknown format: " + format);
            }
            return bundle;
        }
    }

}
