/**
 * Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.series.api.v1.db.da.beans;

import java.util.Set;

public class DescribableEntity<T extends I18nEntity> {

    /**
     * A serial primary key.
     */
    private Long pkid;

    /**
     * Identification of the entity without special chars.
     */
    private String canonicalId;
    
    /**
     * Default name of the entity.
     */
    private String name;
    
    /**
     * Default description of the entity.
     */
    private String description;

    private Set<T> translations;
    

    public Long getPkid() {
        return pkid;
    }

    public void setPkid(Long pkid) {
        this.pkid = pkid;
    }

    public String getCanonicalId() {
        return canonicalId;
    }

    public void setCanonicalId(String canonicalId) {
        this.canonicalId = canonicalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<T> getTranslations() {
        return translations;
    }

    public void setTranslations(Set<T> translations) {
        this.translations = translations;
    }

    public String getNameI18n(String locale) {
        if (noTranslationAvailable(locale)) {
            return name;
        }
        String candidate = name;
        String countryCode = getCountryCode(locale);
        for (T translation : translations) {
            String translatedLocale = translation.getLocale();
            if (translatedLocale.equals(locale)) {
                // locale matches exactly
                return translation.getName();
            }
            if (translatedLocale.equals(countryCode)) {
                // hold a country candidate
                candidate = translation.getName();
            }
        }
        return candidate;
    }

    private boolean noTranslationAvailable(String locale) {
        return translations == null 
                || locale == null 
                || translations.isEmpty()
                || locale.isEmpty();
    }
    
    private String getCountryCode(String locale) {
        return locale.split("_")[0];
    }
    
}
