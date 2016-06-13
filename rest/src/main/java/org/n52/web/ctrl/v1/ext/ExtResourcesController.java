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
package org.n52.web.ctrl.v1.ext;

import static org.n52.web.ctrl.v1.ResourcesController.ResourceCollection.createResource;

import java.util.List;

import org.n52.io.I18N;
import org.n52.io.request.IoParameters;
import org.n52.sensorweb.spi.v1.ExtCountingMetadataService;
import org.n52.web.ctrl.v1.ResourcesController;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class ExtResourcesController extends ResourcesController {

    private ExtCountingMetadataService metadataService;

    @Override
    protected List<ResourceCollection> createResources(IoParameters params) {
        List<ResourceCollection> resources = super.createResources(params);

        I18N i18n = I18N.getMessageLocalizer(params.getLocale());

        ResourceCollection platforms = createResource("platforms").withLabel("Station").withDescription(i18n.get("msg.web.resources.platforms"));
        ResourceCollection series = createResource("series").withLabel("Timeseries").withDescription(i18n.get("msg.web.resources.series"));
        ResourceCollection extCategories = createResource("ext/categories").withLabel("Category").withDescription(i18n.get("msg.web.resources.ext.categories"));
        ResourceCollection extOfferings = createResource("ext/offerings").withLabel("Offering").withDescription(i18n.get("msg.web.resources.ext.offerings"));
        ResourceCollection extFeatures = createResource("ext/features").withLabel("Feature").withDescription(i18n.get("msg.web.resources.ext.features"));
        ResourceCollection extProcedures = createResource("ext/procedures").withLabel("Procedure").withDescription(i18n.get("msg.web.resources.ext.procedures"));
        ResourceCollection extPhenomena = createResource("ext/phenomena").withLabel("Phenomenon").withDescription(i18n.get("msg.web.resources.ext.phenomena"));
//        if (params.isExpanded()) {
//            platforms.setSize(getMetadataService().getPlatformCount());
//            series.setSize(getMetadataService().getSeriesCount());
//            extCategories.setSize(getMetadataService().getExtCategoriesCount());
//            extOfferings.setSize(getMetadataService().getExtOfferingsCount());
//            extFeatures.setSize(getMetadataService().getExtFeaturesCount());
//            extProcedures.setSize(getMetadataService().getExtProceduresCount());
//            extPhenomena.setSize(getMetadataService().getExtPhenomenaCount());
//        }
        resources.add(platforms);
        resources.add(series);
        resources.add(extCategories);
        resources.add(extOfferings);
        resources.add(extFeatures);
        resources.add(extProcedures);
        resources.add(extPhenomena);
        return resources;
    }

    public ExtCountingMetadataService getMetadataService() {
        return metadataService;
    }

    public void setMetadataService(ExtCountingMetadataService metadataService) {
        this.metadataService = metadataService;
    }


}
