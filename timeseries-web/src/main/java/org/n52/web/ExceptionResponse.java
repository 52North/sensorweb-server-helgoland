/**
 * ﻿Copyright (C) 2013
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

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Wraps all {@link WebException}s thrown by controlled workflow. If unexpected Exceptions occur a general
 * {@link InternalServerException} should be wrapped so that all exceptions interrupting the expected workflow
 * can be serialized and returned to the requesting user/service. <br/>
 * <br/>
 * To ensure all exceptions are handled and communicated to the user all Web bindings shall inherit from
 * {@link BaseController} which is configured by default to serve as a central {@link ExceptionHandler}.
 */
public final class ExceptionResponse {

    // TODO add documentation url for details

    // TODO make stack tracing configurable

    private Throwable exception;

    private HttpStatus statusCode;

    private String[] hints;

    public static ExceptionResponse createExceptionResponse(WebException e, HttpStatus statusCode) {
        return new ExceptionResponse(e.getThrowable(), statusCode, e.getHints());
    }

    private ExceptionResponse(Throwable e, HttpStatus statusCode) {
        this(e, statusCode, null);
    }

    private ExceptionResponse(Throwable e, HttpStatus statusCode, String[] hints) {
        this.statusCode = statusCode;
        this.hints = hints;
        this.exception = e;
    }

    public int getStatusCode() {
        return statusCode.value();
    }

    public String getReason() {
        return statusCode.getReasonPhrase();
    }

    public String getUserMessage() {
        return exception.getMessage();
    }

    public String getDeveloperMessage() {
        Throwable causedBy = exception.getCause();
        return causedBy != null ? formatMessageOutput(causedBy) : null;
    }

    private String formatMessageOutput(Throwable causedBy) {
        return causedBy.getMessage().replace("\"", "'");
    }

    public String[] getHints() {
        return hints;
    }

}
