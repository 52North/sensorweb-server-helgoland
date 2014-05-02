/**
 * Copyright (C) 2013-2014 52°North Initiative for Geospatial Open Source
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
package org.n52.io.web;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.n52.io.MimeType.APPLICATION_JSON;
import static org.n52.io.MimeType.APPLICATION_PDF;
import static org.n52.io.MimeType.IMAGE_PNG;
import static org.n52.io.web.ExceptionResponse.createExceptionResponse;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.n52.web.v1.ctrl.ResourcesController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.n52.sensorweb.v1.spi.BadQueryParameterException;

/**
 * Serves as central {@link ExceptionHandler} for all Web bindings inheriting from this class.
 * {@link WebException}s indicate an expected workflows while unexpected exceptions are automatically wrapped
 * to {@link InternalServerException}s as fallback.<br/>
 * <br/>
 * Developers should consider to add hints via {@link WebException#addHint(String)} so that as much
 * information is communicated to the caller as possible.
 */
@Controller
public abstract class BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesController.class);

    protected boolean isRequestingJsonData(HttpServletRequest request) {
        return APPLICATION_JSON.getMimeType().equals(request.getHeader("Accept"));
    }

    protected boolean isRequestingPdfData(HttpServletRequest request) {
        return APPLICATION_PDF.getMimeType().equals(request.getHeader("Accept"));
    }

    protected boolean isRequestingPngData(HttpServletRequest request) {
        return IMAGE_PNG.getMimeType().equals(request.getHeader("Accept"));
    }

    @ExceptionHandler(value = {BadRequestException.class, BadQueryParameterException.class})
    public void handle400(Exception e, HttpServletRequest request, HttpServletResponse response) {
        writeExceptionResponse((WebException) e, response, BAD_REQUEST);
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    public void handle404(Exception e, HttpServletRequest request, HttpServletResponse response) {
        writeExceptionResponse((WebException) e, response, NOT_FOUND);
    }

    @ExceptionHandler(value = InternalServerException.class)
    public void handle500(Exception e, HttpServletRequest request, HttpServletResponse response) {
        writeExceptionResponse((WebException) e, response, INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {RuntimeException.class, Exception.class, Throwable.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) {
        if (e instanceof HttpMessageNotReadableException) {
            WebException wrappedException = new BadRequestException("The request could not been read.", e);
            wrappedException.addHint("Check the message which has been sent to the server. Probably it is not valid.");
            writeExceptionResponse(wrappedException, response, BAD_REQUEST);
        }
        else {
            WebException wrappedException = new InternalServerException("Unexpected Exception occured.", e);
            writeExceptionResponse(wrappedException, response, INTERNAL_SERVER_ERROR);
        }
    }

    private void writeExceptionResponse(WebException e, HttpServletResponse response, HttpStatus status) {

        if (status == INTERNAL_SERVER_ERROR) {
            LOGGER.error("An exception occured.", e);
        } else {
            LOGGER.debug("An exception occured.", e);
        }

        // TODO consider using a 'suppress_response_codes=true' parameter and always return 200 OK

        response.setStatus(status.value());
        response.setContentType(APPLICATION_JSON.getMimeType());
        ObjectMapper objectMapper = createObjectMapper();
        ObjectWriter writer = objectMapper.writerWithType(ExceptionResponse.class);
        ExceptionResponse exceptionResponse = createExceptionResponse(e, status);
        try {
            writer.writeValue(response.getOutputStream(), exceptionResponse);
        }
        catch (IOException ioe) {
            LOGGER.error("Could not process error message.", ioe);
        }
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper().setSerializationInclusion(NON_NULL);
    }

}
