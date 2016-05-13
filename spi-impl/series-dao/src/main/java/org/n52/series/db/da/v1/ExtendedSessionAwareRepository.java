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
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.v1.ServiceOutput;
import org.n52.io.response.v1.CategoryOutput;
import org.n52.io.response.v1.FeatureOutput;
import org.n52.io.response.v1.OfferingOutput;
import org.n52.io.response.v1.PhenomenonOutput;
import org.n52.io.response.v1.ProcedureOutput;
import org.n52.io.response.v1.TimeseriesOutput;
import org.n52.io.response.v1.ext.PlatformOutput;
import org.n52.io.response.v1.ext.SeriesParameters;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.SessionAwareRepository;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.ext.AbstractSeriesEntity;
import org.n52.series.db.da.beans.ext.MeasurementSeriesEntity;
import org.n52.series.db.da.beans.ext.PlatformEntity;
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

    protected Map<String, CommonSeriesParameters> createSeriesList(List<AbstractSeriesEntity> series, DbQuery parameters) throws DataAccessException {
        Map<String, CommonSeriesParameters> outputs = new HashMap<>();
        for (AbstractSeriesEntity entity : series) {
            String seriesId = entity.getPkid().toString();
            SeriesParameters output = createTimeseriesOutput(entity, parameters);
            output.setPlatform(getCondensedPlatform(entity.getPlatform(), parameters));
            outputs.put(seriesId, output);
        }
        return outputs;
    }

    protected SeriesParameters createTimeseriesOutput(AbstractSeriesEntity timeseries, DbQuery parameters) throws DataAccessException {
        SeriesParameters timeseriesOutput = new SeriesParameters();
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

    protected ParameterOutput getCondensedOffering(DescribableEntity entity, DbQuery parameters) {
        return createCondensed(new OfferingOutput(), entity, parameters);
    }

    private ParameterOutput createCondensed(ParameterOutput outputvalue, DescribableEntity entity, DbQuery parameters) {
        final String id = entity.getPkid().toString();
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setHref(parameters.getHrefBase() + "/" + id);
        outputvalue.setId(id);
        return outputvalue;
    }

    protected ParameterOutput getCondensedProcedure(DescribableEntity entity, DbQuery parameters) {
        return createCondensed(new ProcedureOutput(), entity, parameters);
    }

    protected ParameterOutput getCondensedFeature(DescribableEntity entity, DbQuery parameters) {
        return createCondensed(new FeatureOutput(), entity, parameters);
    }

    protected ParameterOutput getCondensedCategory(DescribableEntity entity, DbQuery parameters) {
        return createCondensed(new CategoryOutput(), entity, parameters);
    }

    protected ParameterOutput getCondensedPlatform(PlatformEntity entity, DbQuery parameters) {
        return createCondensed(new PlatformOutput(entity.getPlatformType()), entity, parameters);
    }

    @Override
    protected DbQuery getDbQuery(IoParameters parameters) {
        return DbQuery.createFrom(parameters);
    }

}
