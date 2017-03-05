/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.series.db.beans;

import java.util.Set;

import org.n52.series.db.beans.parameter.Parameter;

public class DescribableEntity {

    public final static String PKID = "pkid";

    public final static String DOMAIN_ID = "domainId";

    public final static String NAME = "name";

    public final static String SERVICE_ID = "serviceid";

    /**
     * A serial primary key.
     */
    private Long pkid;

    /**
     * Identification of the entity without special chars.
     */
    private String domainId;

    /**
     * Default name of the entity.
     */
    private String name;

    /**
     * Default description of the entity.
     */
    private String description;

    private ServiceEntity service;

    private Set<I18nEntity> translations;

    private Set<Parameter<?>> parameters;

    public Long getPkid() {
        return pkid;
    }

    @SuppressWarnings("unchecked")
    public <T> T setPkid(Long pkid) {
        this.pkid = pkid;
        return (T) this;
    }

    public String getDomainId() {
        return domainId;
    }

    @SuppressWarnings("unchecked")
    public <T> T setDomainId(String domainId) {
        this.domainId = domainId;
        return (T) this;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public <T> T setName(String name) {
        this.name = name;
        return (T) this;
    }

    public boolean isSetName() {
        return getName() != null && !getName().isEmpty();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSetDescription() {
        return getDescription() != null && !getDescription().isEmpty();
    }

    public Set<I18nEntity> getTranslations() {
        return translations;
    }

    public void setTranslations(Set<I18nEntity> translations) {
        this.translations = translations;
    }

    public Set<Parameter<?>> getParameters() {
        return parameters;
    }

    public void setParameters(Set<Parameter<?>> parameters) {
        this.parameters = parameters;
    }

    public boolean hasParameters() {
        return getParameters() != null && !getParameters().isEmpty();
    }

    public ServiceEntity getService() {
        return service;
    }

    public void setService(ServiceEntity service) {
        this.service = service;
    }

    public String getNameI18n(String locale) {
        if (noTranslationAvailable(locale)) {
            return name;
        }
        String candidate = name;
        String countryCode = getCountryCode(locale);
        for (I18nEntity translation : translations) {
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

    public String getLabelFrom(String locale) {
        if (isi18nNameAvailable(locale)) {
            return getNameI18n(locale);
        } else if (isNameAvailable()) {
            return getName();
        } else if (isDomainIdAvailable()){
            return getDomainId();
        } else {
            // absolute fallback
            return Long.toString(getPkid());
        }
    }

    private boolean isNameAvailable() {
        return getName() != null && !getName().isEmpty();
    }

    private boolean isDomainIdAvailable() {
        return getDomainId() != null && !getDomainId().isEmpty();
    }

    private boolean isi18nNameAvailable(String locale) {
        return getNameI18n(locale) != null && !getNameI18n(locale).isEmpty();
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
