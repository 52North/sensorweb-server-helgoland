/*
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.series.db.da.v1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.io.request.IoParameters;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.series.SeriesParameters;
import org.n52.io.response.v1.CategoryOutput;
import org.n52.io.response.v1.FeatureOutput;
import org.n52.io.response.v1.OfferingOutput;
import org.n52.io.response.v1.PhenomenonOutput;
import org.n52.io.response.v1.ProcedureOutput;
import org.n52.io.response.v1.ServiceOutput;
import org.n52.io.response.v1.ext.PlatformOutput;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.SessionAwareRepository;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;
import org.n52.series.db.da.beans.ext.MeasurementSeriesEntity;
import org.n52.series.db.da.beans.ext.PlatformEntity;
import org.n52.web.ctrl.v1.ext.UrlHelper;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ExtendedSessionAwareRepository extends SessionAwareRepository<DbQuery> {

    @Autowired
    private ServiceRepository serviceRepository;

    protected UrlHelper urHelper = new UrlHelper();

    protected Map<String, SeriesParameters> createTimeseriesList(List<MeasurementSeriesEntity> series, DbQuery parameters) throws DataAccessException {
        Map<String, SeriesParameters> timeseriesOutputs = new HashMap<>();
        for (MeasurementSeriesEntity timeseries : series) {
            if (!timeseries.getProcedure().isReference()) {
                String timeseriesId = timeseries.getPkid().toString();
                timeseriesOutputs.put(timeseriesId, createTimeseriesOutput(timeseries, parameters));
            }
        }
        return timeseriesOutputs;
    }

    protected SeriesParameters createTimeseriesOutput(MeasurementSeriesEntity timeseries, DbQuery parameters) throws DataAccessException {
        SeriesParameters timeseriesOutput = new SeriesParameters();
        timeseriesOutput.setService(getCondensedService(parameters));
        timeseriesOutput.setOffering(getCondensedOffering(timeseries.getProcedure(), parameters));
        timeseriesOutput.setProcedure(getCondensedProcedure(timeseries.getProcedure(), parameters));
        timeseriesOutput.setPhenomenon(getCondensedPhenomenon(timeseries.getPhenomenon(), parameters));
        timeseriesOutput.setFeature(getCondensedFeature(timeseries.getFeature(), parameters));
        timeseriesOutput.setCategory(getCondensedCategory(timeseries.getCategory(), parameters));
        return timeseriesOutput;
    }

    protected SeriesParameters createSeriesParameters(AbstractSeriesEntity series, DbQuery parameters) throws DataAccessException {
        SeriesParameters seriesParameter = new SeriesParameters();
        seriesParameter.setService(getCondensedExtendedService(parameters));
        seriesParameter.setOffering(getCondensedExtendedOffering(series.getProcedure(), parameters));
        seriesParameter.setProcedure(getCondensedExtendedProcedure(series.getProcedure(), parameters));
        seriesParameter.setPhenomenon(getCondensedExtendedPhenomenon(series.getPhenomenon(), parameters));
        seriesParameter.setFeature(getCondensedExtendedFeature(series.getFeature(), parameters));
        seriesParameter.setCategory(getCondensedExtendedCategory(series.getCategory(), parameters));
        seriesParameter.setPlatform(getCondensedPlatform(series.getPlatform(), parameters));
        return seriesParameter;
    }

    @Override
    protected ServiceOutput getServiceOutput() throws DataAccessException {
        List<ServiceOutput> all = serviceRepository.getAllCondensed(null);
        return all.get(0); // only this service available
    }

    protected ServiceOutput getCondensedService(DbQuery parameters ) throws DataAccessException {
        String serviceId = serviceRepository.getServiceId();
        ServiceOutput instance = serviceRepository.getCondensedInstance(serviceId, parameters);
        ServiceOutput serviceOutput = new ServiceOutput();
        serviceOutput.setLabel(instance.getLabel());
        serviceOutput.setId(instance.getId());
        return serviceOutput;
    }

    private org.n52.io.response.ServiceOutput getCondensedExtendedService(DbQuery parameters) {
        String serviceId = serviceRepository.getServiceId();
        ServiceOutput instance = serviceRepository.getCondensedInstance(serviceId, parameters);
        ServiceOutput serviceOutput = new ServiceOutput();
        serviceOutput.setLabel(instance.getLabel());
        serviceOutput.setId(instance.getId());
        serviceOutput.setHref(urHelper.getServicesHrefBaseUrl(parameters.getHrefBase()) + "/" + instance.getId());
        return serviceOutput;
    }

    protected PhenomenonOutput getCondensedPhenomenon(DescribableEntity entity, DbQuery parameters) {
        PhenomenonOutput outputvalue = new PhenomenonOutput();
        final String id = entity.getPkid().toString();
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setId(id);
        return outputvalue;
    }

    protected PhenomenonOutput getCondensedExtendedPhenomenon(DescribableEntity entity, DbQuery parameters) {
        PhenomenonOutput outputvalue = getCondensedPhenomenon(entity, parameters);
        outputvalue.setHref(entity.getPkid().toString());
        return outputvalue;
    }

    protected ParameterOutput getCondensedOffering(DescribableEntity entity, DbQuery parameters) {
        return createCondensed(new OfferingOutput(), entity, parameters);
    }

    protected ParameterOutput getCondensedExtendedOffering(DescribableEntity entity, DbQuery parameters) {
        return createCondensedExtended(new OfferingOutput(), entity, parameters, urHelper.getOfferingsHrefBaseUrl(parameters.getHrefBase()));
    }

    private ParameterOutput createCondensed(ParameterOutput outputvalue, DescribableEntity entity, DbQuery parameters) {
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setId(Long.toString(entity.getPkid()));
        return outputvalue;
    }

    private ParameterOutput createCondensedExtended(ParameterOutput outputvalue, DescribableEntity entity, DbQuery parameters, String hrefBase) {
        createCondensed(outputvalue, entity, parameters);
        outputvalue.setHref(hrefBase + "/" + outputvalue.getId());
        return outputvalue;
    }

    protected ParameterOutput getCondensedProcedure(DescribableEntity entity, DbQuery parameters) {
        return createCondensed(new ProcedureOutput(), entity, parameters);
    }

    protected ParameterOutput getCondensedExtendedProcedure(DescribableEntity entity, DbQuery parameters) {
        return createCondensedExtended(new ProcedureOutput(), entity, parameters, urHelper.getProceduresHrefBaseUrl(parameters.getHrefBase()));
    }

    protected ParameterOutput getCondensedFeature(DescribableEntity entity, DbQuery parameters) {
        return createCondensed(new FeatureOutput(), entity, parameters);
    }

    protected ParameterOutput getCondensedExtendedFeature(DescribableEntity entity, DbQuery parameters) {
        return createCondensedExtended(new FeatureOutput(), entity, parameters, urHelper.getFeaturesHrefBaseUrl(parameters.getHrefBase()));
    }

    protected ParameterOutput getCondensedCategory(DescribableEntity entity, DbQuery parameters) {
        return createCondensed(new CategoryOutput(), entity, parameters);
    }

    protected ParameterOutput getCondensedExtendedCategory(DescribableEntity entity, DbQuery parameters) {
        return createCondensedExtended(new CategoryOutput(), entity, parameters, urHelper.getCategoriesHrefBaseUrl(parameters.getHrefBase()));
    }

    protected ParameterOutput getCondensedPlatform(PlatformEntity entity, DbQuery parameters) {
        return createCondensedExtended(new PlatformOutput(entity.getPlatformType()), entity, parameters, urHelper.getPlatformsHrefBaseUrl(parameters.getHrefBase()));
    }

    @Override
    protected DbQuery getDbQuery(IoParameters parameters) {
        return DbQuery.createFrom(parameters);
    }

}
