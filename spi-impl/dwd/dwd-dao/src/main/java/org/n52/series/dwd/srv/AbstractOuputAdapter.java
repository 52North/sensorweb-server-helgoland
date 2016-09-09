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
package org.n52.series.dwd.srv;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.geotools.geometry.jts.JTS;
import org.n52.io.crs.BoundingBox;
import org.n52.io.crs.CRSUtils;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.CategoryOutput;
import org.n52.io.response.FeatureOutput;
import org.n52.io.response.OfferingOutput;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.PhenomenonOutput;
import org.n52.io.response.PlatformOutput;
import org.n52.io.response.PlatformType;
import org.n52.io.response.ProcedureOutput;
import org.n52.io.response.ServiceOutput;
import org.n52.series.dwd.beans.ServiceInfo;
import org.n52.series.dwd.beans.WarnCell;
import org.n52.series.dwd.rest.Alert;
import org.n52.series.dwd.rest.Alert.AlertTypes;
import org.n52.series.dwd.rest.AlertCollection;
import org.n52.series.dwd.rest.VorabInformationAlert;
import org.n52.series.dwd.rest.WarnungAlert;
import org.n52.series.dwd.store.AlertStore;
import org.n52.series.spi.srv.ParameterService;
import org.n52.web.ctrl.UrlHelper;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public abstract class AbstractOuputAdapter<T extends ParameterOutput> extends ParameterService<T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractOuputAdapter.class);
    protected final UrlHelper urlHelper = new UrlHelper();
    private ServiceInfo serviceInfo;
    private String sridAuthorityCode = "EPSG:4326"; // default

    public AbstractOuputAdapter(ServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public void setDatabaseAuthorityCode(String code) {
        this.sridAuthorityCode = code;
    }

    protected ServiceOutput getServiceOutput() {
        ServiceOutput result = new ServiceOutput();
        result.setLabel(serviceInfo.getServiceDescription());
        result.setId(serviceInfo.getServiceId());
        return result;
    }

    protected WarnCell getWarnCell(String id, AlertStore store) {
        for (WarnCell warnCell : store.getAllWarnCells()) {
            if (warnCell.getId().equals(id)) {
                return warnCell;
            }
        }
        return null;
    }

    protected Set<WarnCell> getFilteredWarnCells(IoParameters query, AlertStore store) {
        Set<String> requestedWarnCells = getRequestedWarnCells(query);
        Set<WarnCell> warnCells = new HashSet<WarnCell>();
        Polygon envelop = createEnvelopeFromSpatialFilter(query);
        if (requestedWarnCells != null) {
            for (WarnCell warnCell : store.getAllWarnCells()) {
                if ((requestedWarnCells.isEmpty() || requestedWarnCells.contains(warnCell.getId()))
                        && checkForBboxFilter(envelop, warnCell)) {
                    if (!getFilteredAlerts(warnCell, query, store).isEmpty()) {
                        warnCells.add(warnCell);
                    }
                }
            }
        }
        return warnCells;
    }

    private Polygon createEnvelopeFromSpatialFilter(IoParameters query) {
        BoundingBox spatialFilter = query.getSpatialFilter();
        if (spatialFilter != null) {
            try {
                CRSUtils crsUtils = CRSUtils.createEpsgForcedXYAxisOrder();
                Point ll = (Point) crsUtils.transformInnerToOuter(spatialFilter.getLowerLeft(), sridAuthorityCode);
                Point ur = (Point) crsUtils.transformInnerToOuter(spatialFilter.getUpperRight(), sridAuthorityCode);
                return JTS.toGeometry(new Envelope(ll.getCoordinate(), ur.getCoordinate()));
            }
            catch (FactoryException e) {
                LOGGER.error("Could not create transformation facilities.", e);
            }
            catch (TransformException e) {
                LOGGER.error("Could not perform transformation.", e);
            }
        }
        return null;
    }

    private boolean checkForBboxFilter(Polygon envelop, WarnCell warnCell) {
        if (envelop != null) {
            return warnCell.getGeometry().within(envelop);
        }
        return true;
    }

    private Set<String> getRequestedWarnCells(IoParameters query) {
        Set<String> platforms = new HashSet<>();
        Set<String> features = new HashSet<>();
        if (query.containsParameter(Parameters.FEATURES)) {
            for (String id : query.getFeatures()) {
                features.add(id);
            }
        } else if (query.containsParameter(Parameters.PLATFORMS)) {
            for (String id : query.getPlatforms()) {
                platforms.add(PlatformType.extractId(id));
            }
        }
        Set<String> requestedWarnCells = new HashSet<>();
        if (!platforms.isEmpty() && !features.isEmpty()) {
            for (String platform : platforms) {
                if (features.contains(platform)) {
                    requestedWarnCells.add(platform);
                }
            }
            if (requestedWarnCells.isEmpty()) {
                return null;
            }
        } else {
            requestedWarnCells.addAll(features);
            requestedWarnCells.addAll(platforms);
        }
        return requestedWarnCells;
    }

    protected Set<Alert> getFilteredAlerts(WarnCell warnCell, IoParameters query, AlertStore store) {
        Set<Alert> alerts = new HashSet<Alert>();
        AlertCollection currentAlerts = store.getCurrentAlerts();
        if (currentAlerts.hasWarning() && checkProcedures(query, AlertTypes.warning)) {
            List<WarnungAlert> list = currentAlerts.getWarnings().get(warnCell.getId());
            if (list != null) {
                for (WarnungAlert warnungAlert : list) {
                    if (checkAlert(warnungAlert, query)) {
                        alerts.add(warnungAlert);
                    }
                }
            }
        }
        if (currentAlerts.hasVorabInformation() && checkProcedures(query, AlertTypes.vorabInformation)) {
            List<VorabInformationAlert> list = currentAlerts.getVorabInformation().get(warnCell.getId());
            if (list != null) {
                for (VorabInformationAlert vorabInformationAlert : list) {
                    if (checkAlert(vorabInformationAlert, query)) {
                        alerts.add(vorabInformationAlert);
                    }
                }
            }
        }
        return alerts;
    }

    protected Set<Alert> getFilteredAlerts(IoParameters query, AlertStore store) {
        Set<Alert> filteredAlerts = new HashSet<>();
        for (WarnCell warnCell : getFilteredWarnCells(query, store)) {
            filteredAlerts.addAll(getFilteredAlerts(warnCell, query, store));
        }
        return filteredAlerts;
    }

    private boolean checkAlert(Alert alert, IoParameters query) {
        boolean offering = true;
        boolean category = true;
        boolean phenomenon = true;
        if (query.containsParameter(Parameters.PHENOMENA) && query.containsParameter(Parameters.CATEGORIES)) {
            phenomenon = query.getPhenomena().contains(toLowerCase(createPhenomenonId(alert.getEvent()))) && query.getCategories().contains(toLowerCase(createPhenomenonId(alert.getEvent())));
            category = phenomenon;
        } else {
            if (query.containsParameter(Parameters.PHENOMENA)) {
                phenomenon = query.getPhenomena().contains(toLowerCase(createPhenomenonId(alert.getEvent())));
            }
            if (query.containsParameter(Parameters.CATEGORIES)) {
                category = query.getCategories().contains(toLowerCase(createPhenomenonId(alert.getEvent())));
            }
        }

        if (query.containsParameter(Parameters.OFFERINGS)) {
            offering = query.getOfferings().contains(Integer.toString(alert.getType()));
        }
        return offering && category && phenomenon;
    }

    private String toLowerCase(String id) {
        return id.toLowerCase(Locale.ROOT);
    }

    private boolean checkProcedures(IoParameters query, AlertTypes alertType) {
        if (query.containsParameter(Parameters.PROCEDURES)) {
            return query.getProcedures().contains(toLowerCase(alertType.name()));
        }
        return true;
    }

    protected ParameterOutput createCondensedCategory(String event, IoParameters query) {
        CategoryOutput result = new CategoryOutput();
        result.setLabel(event);
        result.setId(createPhenomenonId(event));
        result.setDomainId(event);
        result.setHrefBase(urlHelper.getCategoriesHrefBaseUrl(query.getHrefBase()));
        return result;
    }

    protected ParameterOutput createCondensedFeature(WarnCell warnCell, Alert alert, IoParameters query) {
        FeatureOutput result = new FeatureOutput();
        result.setLabel(alert.getRegionName());
        result.setId(warnCell.getId());
        result.setDomainId(warnCell.getId());
        result.setHrefBase(urlHelper.getFeaturesHrefBaseUrl(query.getHrefBase()));
        return result;
    }

    protected ParameterOutput createCondensedOffering(int type, IoParameters query) {
        OfferingOutput result = new OfferingOutput();
        result.setLabel(Integer.toString(type));
        result.setId(Integer.toString(type));
        result.setDomainId(Integer.toString(type));
        result.setHrefBase(urlHelper.getOfferingsHrefBaseUrl(query.getHrefBase()));
        return result;
    }

    protected ParameterOutput createCondensedPhenomenon(String event, IoParameters query) {
        PhenomenonOutput result = new PhenomenonOutput();
        result.setLabel(event);
        result.setId(createPhenomenonId(event));
        result.setDomainId(event);
        result.setHrefBase(urlHelper.getPhenomenaHrefBaseUrl(query.getHrefBase()));
        return result;
    }

    protected ParameterOutput createCondensedPlatform(WarnCell warnCell, Alert alert, IoParameters query) {
        PlatformOutput result = new PlatformOutput(PlatformType.STATIONARY_INSITU);
        result.setLabel(alert.getRegionName());
        result.setId(warnCell.getId());
        result.setDomainId(warnCell.getId());
        result.setHrefBase(urlHelper.getPlatformsHrefBaseUrl(query.getHrefBase()));
        return result;
    }

    protected ParameterOutput createCondensedProcedure(String alertType, IoParameters query) {
        ProcedureOutput result = new ProcedureOutput();
        result.setLabel(alertType);
        result.setId(alertType);
        result.setDomainId(alertType);
        result.setHrefBase(urlHelper.getProceduresHrefBaseUrl(query.getHrefBase()));
        return result;
    }

    protected String createPhenomenonId(String id) {
        return id.replace("/", "-");
    }

    protected String parsePhenomenonId(String id) {
        return id.replace("-", "/");
    }
}
