/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
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
package org.n52.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.codec.binary.Base64;
import org.n52.io.request.IoParameters;
import org.n52.io.request.Parameters;
import org.n52.io.response.dataset.AbstractValue;
import org.n52.io.response.dataset.Data;
import org.n52.io.response.dataset.DataCollection;

// TODO actually this interface describes an prepares and writes an output only
// TODO consider a plain JSON Writer to get rid of ModelAndView in controllers
public abstract class IoHandler<T extends Data<? extends AbstractValue<?>>> {

    protected final I18N i18n;

    private final IoProcessChain<T> processChain;

    private final IoParameters parameters;

    public IoHandler(IoParameters parameters, IoProcessChain<T> processChain) {
        this.processChain = processChain;
        this.parameters = parameters;
        i18n = parameters.containsParameter(Parameters.LOCALE)
                ? I18N.getMessageLocalizer(parameters.getLocale())
                : I18N.getDefaultLocalizer();
    }

    /**
     * Encodes and writes previously generated output to the given stream.
     *
     * @param data the input data collection to create an output for.
     * @param stream the stream to write on the generated ouput.
     * @throws IoHandlerException if writing output to stream fails.
     */
    protected abstract void encodeAndWriteTo(DataCollection<T> data, OutputStream stream) throws IoHandlerException;

    public void writeBinary(OutputStream outputStream) throws IoHandlerException {
        try {
            if (parameters.isBase64()) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                encodeAndWriteTo(processChain.getData(), baos);
                byte[] data = baos.toByteArray();
                byte[] encode = Base64.encodeBase64(data);
                outputStream.write(encode);
            } else {
                encodeAndWriteTo(processChain.getData(), outputStream);
            }
        } catch (IOException e) {
            throw new IoHandlerException("Error handling output stream.", e);
//        } catch (IoParseException e) {
//            throw new IoHandlerException("Could not write binary to stream.", e);
        }
    }

    protected IoParameters getParameters() {
        return parameters;
    }

}
