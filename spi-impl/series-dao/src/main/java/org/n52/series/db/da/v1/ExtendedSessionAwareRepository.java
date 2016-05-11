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
import org.n52.io.response.CommonSeriesParameters;
import org.n52.io.response.v1.ServiceOutput;
import org.n52.io.response.v1.CategoryOutput;
import org.n52.io.response.v1.FeatureOutput;
import org.n52.io.response.v1.OfferingOutput;
import org.n52.io.response.v1.PhenomenonOutput;
import org.n52.io.response.v1.ProcedureOutput;
import org.n52.io.response.v1.TimeseriesOutput;
import org.n52.io.response.v1.ext.SeriesParameters;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.SessionAwareRepository;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.ext.MeasurementSeriesEntity;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ExtendedSessionAwareRepository extends SessionAwareRepository<DbQuery> {

    @Autowired
    private ServiceRepository serviceRepository;

    protected Map<String, CommonSeriesParameters> createTimeseriesList(List<MeasurementSeriesEntity> series, DbQuery parameters) throws DataAccessException {
        Map<String, CommonSeriesParameters> timeseriesOutputs = new HashMap<>();
        for (MeasurementSeriesEntity timeseries : series) {
            if (!timeseries.getProcedure().isReference()) {
                String timeseriesId = timeseries.getPkid().toString();
                timeseriesOutputs.put(timeseriesId, createTimeseriesOutput(timeseries, parameters));
            }
        }
        return timeseriesOutputs;
    }

    protected SeriesParameters createTimeseriesOutput(MeasurementSeriesEntity timeseries, DbQuery parameters) throws DataAccessException {
        TimeseriesOutput timeseriesOutput = new TimeseriesOutput();
        timeseriesOutput.setService(getCondensedService());
        timeseriesOutput.setOffering(getCondensedOffering(timeseries.getProcedure(), parameters));
        timeseriesOutput.setProcedure(getCondensedProcedure(timeseries.getProcedure(), parameters));
        timeseriesOutput.setPhenomenon(getCondensedPhenomenon(timeseries.getPhenomenon(), parameters));
        timeseriesOutput.setFeature(getCondensedFeature(timeseries.getFeature(), parameters));
        timeseriesOutput.setCategory(getCondensedCategory(timeseries.getCategory(), parameters));
        return timeseriesOutput;
    }

    @Override
    protected ServiceOutput getServiceOutput() throws DataAccessException {
        List<ServiceOutput> all = serviceRepository.getAllCondensed(null);
        return all.get(0); // only this service available
    }

    protected ServiceOutput getCondensedService() throws DataAccessException {
        String serviceId = serviceRepository.getServiceId();
        ServiceOutput instance = serviceRepository.getCondensedInstance(serviceId);
        ServiceOutput serviceOutput = new ServiceOutput();
        serviceOutput.setLabel(instance.getLabel());
        serviceOutput.setId(instance.getId());
        return serviceOutput;
    }

    protected PhenomenonOutput getCondensedPhenomenon(DescribableEntity entity, DbQuery parameters) {
        PhenomenonOutput outputvalue = new PhenomenonOutput();
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setId(entity.getPkid().toString());
        return outputvalue;
    }

    protected OfferingOutput getCondensedOffering(DescribableEntity entity, DbQuery parameters) {
        OfferingOutput outputvalue = new OfferingOutput();
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setId(entity.getPkid().toString());
        return outputvalue;
    }

    protected ProcedureOutput getCondensedProcedure(DescribableEntity entity, DbQuery parameters) {
        ProcedureOutput outputvalue = new ProcedureOutput();
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setId(entity.getPkid().toString());
        return outputvalue;
    }

    protected FeatureOutput getCondensedFeature(DescribableEntity entity, DbQuery parameters) {
        FeatureOutput outputvalue = new FeatureOutput();
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setId(entity.getPkid().toString());
        return outputvalue;
    }

    protected CategoryOutput getCondensedCategory(DescribableEntity entity, DbQuery parameters) {
        CategoryOutput outputvalue = new CategoryOutput();
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setId(entity.getPkid().toString());
        return outputvalue;
    }

    @Override
    protected DbQuery getDbQuery(IoParameters parameters) {
        return DbQuery.createFrom(parameters);
    }

}
