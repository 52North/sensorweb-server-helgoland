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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlObject;
import org.n52.series.db.da.ProcedureRepository;
import org.n52.shetland.ogc.ows.service.GetCapabilitiesRequest;
import org.n52.shetland.ogc.sos.Sos2Constants;
import org.n52.shetland.ogc.sos.SosConstants;
import org.n52.svalbard.decode.DecoderRepository;
import org.n52.svalbard.decode.exception.DecodingException;
import org.n52.svalbard.encode.EncoderRepository;
import org.n52.svalbard.encode.exception.EncodingException;
import org.n52.svalbard.util.CodingHelper;
import org.n52.svalbard.util.XmlHelper;

public class SOS2Connector extends AbstractSOSConnector {

  private EncoderRepository encoderRepository;
  private DecoderRepository decoderRepository;

  public SOS2Connector(String serviceURI) {
    super(serviceURI);
  }

  @Inject
  public void setDecoderRepository(DecoderRepository decoderRepository) {
      this.decoderRepository = decoderRepository;
  }

  @Inject
  public void setEncoderRepository(EncoderRepository encoderRepository) {
      this.encoderRepository = encoderRepository;
  }

  public void fetchCapabilities() {
    GetCapabilitiesRequest req = new GetCapabilitiesRequest();
    req.setService(SosConstants.SOS);
    try {
      String request = ((XmlObject)encoderRepository.getEncoder(CodingHelper.getEncoderKey(Sos2Constants.NS_SOS_20, req)).encode(req)).xmlText();
      try {
        String response = IOUtils.toString(this.sendRequest(request).getEntity().getContent());
        System.out.println(response);
        XmlObject parseXmlString = XmlHelper.parseXmlString(response);
        Object temp = decoderRepository.getDecoder(CodingHelper.getDecoderKey(parseXmlString)).decode(parseXmlString);
        System.out.println(temp);
      } catch (IOException | UnsupportedOperationException | DecodingException ex) {
        Logger.getLogger(SOS2Connector.class.getName()).log(Level.SEVERE, null, ex);
      }
    } catch (EncodingException ex) {
      Logger.getLogger(ProcedureRepository.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
