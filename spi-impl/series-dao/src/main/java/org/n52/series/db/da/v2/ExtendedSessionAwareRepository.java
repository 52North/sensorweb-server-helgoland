/**
 * Copyright (C) 2013-2016 52°North Initiative for Geospatial Open Source
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
package org.n52.series.db.da.v2;

import java.util.List;

import org.n52.io.request.IoParameters;
import org.n52.io.response.ServiceOutput;
import org.n52.io.response.v2.CategoryOutput;
import org.n52.io.response.v2.PhenomenonOutput;
import org.n52.io.response.v2.PlatformOutput;
import org.n52.io.response.v2.ProcedureOutput;
import org.n52.io.response.v2.SeriesOutput;
import org.n52.series.db.da.DataAccessException;
import org.n52.series.db.da.SessionAwareRepository;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.FeatureEntity;
import org.n52.series.db.da.beans.I18nCategoryEntity;
import org.n52.series.db.da.beans.I18nPhenomenonEntity;
import org.n52.series.db.da.beans.I18nProcedureEntity;
import org.n52.series.db.da.beans.v2.SeriesEntityV2;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ExtendedSessionAwareRepository extends SessionAwareRepository<DbQuery> {

    @Autowired
    private ServiceRepository serviceRepository;
    
    protected SeriesOutput createSeriesOutput(SeriesEntityV2 series, DbQuery parameters) throws DataAccessException {
    	SeriesOutput seriesOutput = new SeriesOutput();
    	seriesOutput.setService(getCondensedService());
    	seriesOutput.setProcedure(getCondensedProcedure(series.getProcedure(), parameters));
    	seriesOutput.setPhenomenon(getCondensedPhenomenon(series.getPhenomenon(), parameters));
    	seriesOutput.setPlatform(getCondensedPlatform(series.getFeature(), parameters));
    	seriesOutput.setCategory(getCondensedCategory(series.getCategory(), parameters));
        return seriesOutput;
	}

	protected ServiceOutput getCondensedService() throws DataAccessException {
	    String serviceId = serviceRepository.getServiceId();
	    org.n52.io.response.v2.ServiceOutput instance = serviceRepository.getCondensedInstance(serviceId);
	    org.n52.io.response.v2.ServiceOutput serviceOutput = new org.n52.io.response.v2.ServiceOutput();
	    serviceOutput.setLabel(instance.getLabel());
	    serviceOutput.setId(instance.getId());
	    return serviceOutput;
	}

	@Override
	protected ServiceOutput getServiceOutput() throws DataAccessException {
		List<org.n52.io.response.v2.ServiceOutput> all = serviceRepository.getAllCondensed(null);
		return all.get(0); // only this service available
	}

	private PhenomenonOutput getCondensedPhenomenon(DescribableEntity<I18nPhenomenonEntity> entity, DbQuery parameters) {
        PhenomenonOutput outputvalue = new PhenomenonOutput();
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

    private PlatformOutput getCondensedPlatform(FeatureEntity entity, DbQuery parameters) {
        PlatformRepository repository = new PlatformRepository();
        repository.setServiceInfo(getServiceInfo());
    	return repository.createCondensed(entity, parameters);
    }
    
    private CategoryOutput getCondensedCategory(DescribableEntity<I18nCategoryEntity> entity, DbQuery parameters) {
        CategoryOutput outputvalue = new CategoryOutput();
        outputvalue.setLabel(getLabelFrom(entity, parameters.getLocale()));
        outputvalue.setId(entity.getPkid().toString());
        return outputvalue;
    }
    
	@Override
	protected DbQuery getDbQuery(IoParameters parameters) {
		return DbQuery.createFrom(parameters);
	}

	@Override
	protected DbQuery getDbQuery(IoParameters parameters, String locale) {
		return DbQuery.createFrom(parameters, locale);
	}

}
