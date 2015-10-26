/**
 * Copyright (C) 2013-2015 52°North Initiative for Geospatial Open Source
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
package org.n52.io.response.v1;

public class TimeseriesOutput {
    
    private ServiceOutput service;
    
    private OfferingOutput offering;
    
    private FeatureOutput feature;
    
    private ProcedureOutput procedure;
    
    private PhenomenonOutput phenomenon;
    
    private CategoryOutput category;

    public ServiceOutput getService() {
        return service;
    }

    public void setService(ServiceOutput service) {
        this.service = service;
    }

    public OfferingOutput getOffering() {
        return offering;
    }

    public void setOffering(OfferingOutput offering) {
        this.offering = offering;
    }

    public FeatureOutput getFeature() {
        return feature;
    }

    public void setFeature(FeatureOutput feature) {
        this.feature = feature;
    }

    public ProcedureOutput getProcedure() {
        return procedure;
    }

    public void setProcedure(ProcedureOutput procedure) {
        this.procedure = procedure;
    }

    public PhenomenonOutput getPhenomenon() {
        return phenomenon;
    }

    public void setPhenomenon(PhenomenonOutput phenomenon) {
        this.phenomenon = phenomenon;
    }

    public CategoryOutput getCategory() {
        return category;
    }

    public void setCategory(CategoryOutput category) {
        this.category = category;
    }
    
}
