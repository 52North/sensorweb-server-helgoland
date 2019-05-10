/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.io.response;

import java.util.Objects;

/**
 * @author <a href="mailto:e.h.juerrens@52north.org">Eike Hinderk J&uuml;rrens</a>
 * @since 1.5.0
 */
public class AbstractOutput extends ParameterOutput {

    public static final String SERVICE = "service";

    protected OptionalOutput<ServiceOutput> service;

    public AbstractOutput() {
        super();
    }

    public ServiceOutput getService() {
        return getIfSerialized(service);
    }

    public AbstractOutput setService(OptionalOutput<ServiceOutput> service) {
        this.service = service;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(service, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AbstractOutput)) {
            return false;
        }
        AbstractOutput other = (AbstractOutput) obj;
        return Objects.equals(service, other.service)
                && super.equals(obj);
    }

}
