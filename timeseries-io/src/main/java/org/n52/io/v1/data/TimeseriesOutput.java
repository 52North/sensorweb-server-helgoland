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
package org.n52.io.v1.data;

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
