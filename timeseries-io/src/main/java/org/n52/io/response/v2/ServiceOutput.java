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
package org.n52.io.response.v2;

import java.util.Arrays;
import java.util.List;

public class ServiceOutput extends org.n52.io.response.ServiceOutput {

    private final List<String> filters = Arrays.asList(new String[]{ 
        "features, procedures, phenomena, categories, services, platforms"} );
    
	private ParameterCount quantities;
    
    public ParameterCount getQuantities() {
        return quantities;
    }

    public void setQuantities(ParameterCount countedParameters) {
        this.quantities = countedParameters;
    }
    
    public void removeFilter(String filter) {
        filters.remove(filter);
    }
    
    public void addFilter(String filter) {
        if ( !filters.contains(filter)) {
            filters.add(filter);
        }
    }
    
    public String[] getFilters() {
        return filters.toArray(new String[0]);
    }

    public static class ParameterCount {

        private int amountFeatures;
        
        private int amountProcedures;
        
        private int amountPhenomena;
        
        private int amountPlatforms;

        private int amountSeries;
        
        private int amountCategories;

        public int getFeatures() {
            return amountFeatures;
        }

        public void setFeaturesSize(int size) {
            this.amountFeatures = size;
        }

        public int getProcedures() {
            return amountProcedures;
        }

        public void setProceduresSize(int size) {
            this.amountProcedures = size;
        }

        public int getPhenomena() {
            return amountPhenomena;
        }

        public void setPhenomenaSize(int size) {
            this.amountPhenomena = size;
        }

        public int getPlatforms() {
            return amountPlatforms;
        }

        public void setPlatformsSize(int size) {
            this.amountPlatforms = size;
        }

        public void setSeriesSize(int countSeries) {
            this.amountSeries = countSeries;
        }
        
        public int getSeries() {
            return this.amountSeries;
        }

        public int getCategories() {
            return amountCategories;
        }

        public void setCategoriesSize(Integer amountCategories) {
            this.amountCategories = amountCategories;
        }
    }
	
}
