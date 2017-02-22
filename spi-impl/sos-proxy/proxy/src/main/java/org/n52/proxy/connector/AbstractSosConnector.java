/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
package org.n52.proxy.connector;

import java.io.IOException;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.proxy.config.DataSourceConfiguration;
import org.n52.proxy.connector.utils.ServiceConstellation;
import org.n52.proxy.web.SimpleHttpClient;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.UnitEntity;
import org.n52.series.db.dao.DbQuery;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesResponse;
import org.n52.shetland.ogc.ows.service.OwsServiceRequest;
import org.n52.shetland.ogc.ows.service.OwsServiceResponse;
import org.n52.svalbard.decode.DecoderKey;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.EncoderKey;
import org.n52.svalbard.encode.EncoderRepository;
import org.n52.svalbard.encode.exception.EncodingException;
import org.n52.svalbard.util.CodingHelper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractSosConnector {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AbstractSosConnector.class);

    private final int CONNECTION_TIMEOUT = 30000;

    @Autowired
    protected DecoderRepository decoderRepository;

    @Autowired
    protected EncoderRepository encoderRepository;

    public String getConnectorName() {
        return getClass().getName();
    }

    public boolean matches(DataSourceConfiguration config, GetCapabilitiesResponse capabilities) {
        if (config.getConnector() != null) {
            return this.getClass().getSimpleName().equals(config.getConnector())
                    || this.getClass().getName().equals(config.getConnector());
        } else {
            return canHandle(config, capabilities);
        }
    }

    private HttpResponse sendRequest(XmlObject request, String uri) {
        return new SimpleHttpClient(CONNECTION_TIMEOUT, CONNECTION_TIMEOUT).executePost(uri, request);
    }

    protected OwsServiceResponse getSosResponseFor(OwsServiceRequest request, String namespace, String serviceUrl) {
        try {
            EncoderKey encoderKey = CodingHelper.getEncoderKey(namespace, request);
            XmlObject xmlRequest = (XmlObject) encoderRepository.getEncoder(encoderKey).encode(request);
            HttpResponse response = sendRequest(xmlRequest, serviceUrl);
            XmlObject xmlResponse = XmlObject.Factory.parse(response.getEntity().getContent());
            DecoderKey decoderKey = CodingHelper.getDecoderKey(xmlResponse);
            return (OwsServiceResponse) decoderRepository.getDecoder(decoderKey).decode(xmlResponse);
        } catch (EncodingException | IOException | UnsupportedOperationException | XmlException | DecodingException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
            return null;
        }
    }

    protected abstract boolean canHandle(DataSourceConfiguration config, GetCapabilitiesResponse capabilities);

    public abstract ServiceConstellation getConstellation(DataSourceConfiguration config, GetCapabilitiesResponse capabilities);

    public abstract List<DataEntity> getObservations(DatasetEntity seriesEntity, DbQuery query);

    public abstract UnitEntity getUom(DatasetEntity seriesEntity);

    public abstract DataEntity getFirstObservation(DatasetEntity entity);

    public abstract DataEntity getLastObservation(DatasetEntity entity);

}
