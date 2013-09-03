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

package org.n52.web;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.n52.io.MimeType.APPLICATION_JSON;
import static org.n52.io.MimeType.APPLICATION_PDF;
import static org.n52.io.MimeType.IMAGE_PNG;
import static org.n52.web.ExceptionResponse.createExceptionResponse;
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

    @ExceptionHandler(value = { BadRequestException.class })
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
        } else {
            WebException wrappedException = new InternalServerException("Unexpected Exception occured.", e);
            writeExceptionResponse(wrappedException, response, INTERNAL_SERVER_ERROR);
        }
    }

    private void writeExceptionResponse(WebException e, HttpServletResponse response, HttpStatus status) {
        LOGGER.error("An exception occured.", e);

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
            LOGGER.error("Could not process error message.", e);
        }
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper().setSerializationInclusion(NON_NULL);
    }

}
