/**
 * ﻿Copyright (C) 2012
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

import java.util.Locale;
import java.util.ResourceBundle;

public class I18N {

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
        return new I18N(getBundle(MESSAGES, locale), locale);
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
        } else if (localeParts.length == 2){
            return new Locale(localeParts[0], localeParts[1]);
        } else {
            return new Locale(localeParts[0], localeParts[1], localeParts[2]);
        } 
    }

}
