/**
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.series.api.v1.db.da;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.Session;
import org.n52.io.IoParameters;
import static org.n52.io.IoParameters.createFromQuery;
import org.n52.io.v1.data.CategoryOutput;
import org.n52.io.v1.data.FeatureOutput;
import org.n52.io.v1.data.OfferingOutput;
import org.n52.io.v1.data.PhenomenonOutput;
import org.n52.io.v1.data.ProcedureOutput;
import org.n52.io.v1.data.ServiceOutput;
import org.n52.io.v1.data.TimeseriesOutput;
import org.n52.sensorweb.v1.spi.search.SearchResult;
import org.n52.series.api.v1.db.da.beans.DescribableEntity;
import org.n52.series.api.v1.db.da.beans.I18nCategoryEntity;
import org.n52.series.api.v1.db.da.beans.I18nEntity;
import org.n52.series.api.v1.db.da.beans.I18nFeatureEntity;
import org.n52.series.api.v1.db.da.beans.I18nOfferingEntity;
import org.n52.series.api.v1.db.da.beans.I18nPhenomenonEntity;
import org.n52.series.api.v1.db.da.beans.I18nProcedureEntity;
import org.n52.series.api.v1.db.da.beans.OfferingEntity;
import org.n52.series.api.v1.db.da.beans.SeriesEntity;
import org.n52.series.api.v1.db.da.beans.ServiceInfo;
import org.n52.sos.ds.hibernate.HibernateSessionHolder;
import org.n52.sos.ds.hibernate.SessionFactoryProvider;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.service.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SessionAwareRepository {

    // TODO tackle issue #71
    private static final String DATASOURCE_PROPERTIES = "/datasource.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAwareRepository.class);

    private static final HibernateSessionHolder sessionHolder = createSessionHolderIfNeccessary();

    private final ServiceInfo serviceInfo;

    protected SessionAwareRepository(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public abstract Collection<SearchResult> searchFor(String queryString, String locale);

    protected abstract List<SearchResult> convertToSearchResults(List<? extends DescribableEntity<? extends I18nEntity>> found, String locale);

    private static HibernateSessionHolder createSessionHolderIfNeccessary() {
        try {
            if (Configurator.getInstance() == null) {
                Properties connectionProviderConfig = new Properties();
                connectionProviderConfig.load(SessionAwareRepository.class.getResourceAsStream(DATASOURCE_PROPERTIES));
                SessionFactoryProvider provider = new SessionFactoryProvider();
                provider.initialize(connectionProviderConfig);

                // TODO configure hbm.xml mapping path

                return new HibernateSessionHolder(provider);
            } else {
                return new HibernateSessionHolder();
            }
        } catch (IOException e) {
            LOGGER.error("Could not establish database connection. Check {}", DATASOURCE_PROPERTIES, e);
            throw new RuntimeException("Could not establish database connection.");
        }
    }

    public ServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    protected DbQuery createDefaultsWithLocale(String locale) {
        if (locale == null) {
            return DbQuery.createFrom(IoParameters.createDefaults());
        }
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("locale", locale);
        return DbQuery.createFrom(createFromQuery(parameters));
    }

    protected ServiceOutput getServiceOutput() throws DataAccessException {
        ServiceRepository serviceRepository = createServiceRepository();
        List<ServiceOutput> all = serviceRepository.getAllCondensed(null);
        return all.get(0); // only this service available
    }

    private ServiceRepository createServiceRepository() {
        return new ServiceRepository(serviceInfo);
    }

    protected Long parseId(String id) throws DataAccessException {
        try {
            return Long.parseLong(id);
        }
        catch (NumberFormatException e) {
            throw new DataAccessException("Illegal id: " + id);
        }
    }

    protected void returnSession(Session session) {
        sessionHolder.returnSession(session);
    }

    protected Session getSession() {
        if (sessionHolder == null) {
            createSessionHolderIfNeccessary();
        }
        try {
            return sessionHolder.getSession();
        }
        catch (OwsExceptionReport e) {
            throw new IllegalStateException("Could not get hibernate session.", e);
        }
    }

    protected Map<String, TimeseriesOutput> createTimeseriesList(List<SeriesEntity> series, DbQuery parameters) throws DataAccessException {
        Map<String, TimeseriesOutput> timeseriesOutputs = new HashMap<String, TimeseriesOutput>();
        for (SeriesEntity timeseries : series) {
            if ( !timeseries.getProcedure().isReference()) {
                String timeseriesId = timeseries.getPkid().toString();
                timeseriesOutputs.put(timeseriesId, createTimeseriesOutput(timeseries, parameters));
            }
        }
        return timeseriesOutputs;
    }

    protected TimeseriesOutput createTimeseriesOutput(SeriesEntity timeseries, DbQuery parameters) throws DataAccessException {
        TimeseriesOutput timeseriesOutput = new TimeseriesOutput();
        timeseriesOutput.setService(getCondensedService());
        timeseriesOutput.setOffering(getCondensedOffering(timeseries.getOffering(), parameters));
        timeseriesOutput.setProcedure(getCondensedProcedure(timeseries.getProcedure(), parameters));
        timeseriesOutput.setPhenomenon(getCondensedPhenomenon(timeseries.getPhenomenon(), parameters));
        timeseriesOutput.setFeature(getCondensedFeature(timeseries.getFeature(), parameters));
        timeseriesOutput.setCategory(getCondensedCategory(timeseries.getCategory(), parameters));
        return timeseriesOutput;
    }

    private ServiceOutput getCondensedService() throws DataAccessException {
        ServiceRepository serviceRepository = createServiceRepository();
        String serviceId = serviceRepository.getServiceId();
        ServiceOutput instance = serviceRepository.getCondensedInstance(serviceId);
        ServiceOutput serviceOutput = new ServiceOutput();
        serviceOutput.setLabel(instance.getLabel());
        serviceOutput.setId(instance.getId());
        return serviceOutput;
    }
    
    protected String getLabelFrom(DescribableEntity<?> entity, String locale) {
        if (isi18nNameAvailable(entity, locale)) {
            return entity.getNameI18n(locale);
        } else if (isNameAvailable(entity)) {
            return entity.getName();
        } else {
            return entity.getDomainId();
        }
    }

    private boolean isNameAvailable(DescribableEntity<?> entity) {
        return entity.getName() != null && !entity.getName().isEmpty();
    }

    private boolean isi18nNameAvailable(DescribableEntity<?> entity, String locale) {
        return entity.getNameI18n(locale) != null && !entity.getNameI18n(locale).isEmpty();
    }

    private PhenomenonOutput getCondensedPhenomenon(DescribableEntity<I18nPhenomenonEntity> entity, DbQuery parameters) {
        PhenomenonOutput outputvalue = new PhenomenonOutput();
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setId(entity.getPkid().toString());
        return outputvalue;
    }

    private List<OfferingOutput> getCondensedOffering(Set<OfferingEntity> offerings, DbQuery parameters) {
        List<OfferingOutput> list = new ArrayList<>(offerings.size());
        for (OfferingEntity entity : offerings) {
            list.add(getCondensedOffering(entity, parameters));
        }
        return list;
    }

    private OfferingOutput getCondensedOffering(DescribableEntity<I18nOfferingEntity> entity, DbQuery parameters) {
        OfferingOutput outputvalue = new OfferingOutput();
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setId(entity.getPkid().toString());
        return outputvalue;
    }

    private ProcedureOutput getCondensedProcedure(DescribableEntity<I18nProcedureEntity> entity, DbQuery parameters) {
        ProcedureOutput outputvalue = new ProcedureOutput();
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setId(entity.getPkid().toString());
        return outputvalue;
    }

    private FeatureOutput getCondensedFeature(DescribableEntity<I18nFeatureEntity> entity, DbQuery parameters) {
        FeatureOutput outputvalue = new FeatureOutput();
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setId(entity.getPkid().toString());
        return outputvalue;
    }

    private CategoryOutput getCondensedCategory(DescribableEntity<I18nCategoryEntity> entity, DbQuery parameters) {
        CategoryOutput outputvalue = new CategoryOutput();
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setId(entity.getPkid().toString());
        return outputvalue;
    }


}
