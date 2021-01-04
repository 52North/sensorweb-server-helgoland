/*
 * Copyright (C) 2013-2021 52°North Initiative for Geospatial Open Source
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
package org.n52.io.response.dataset;

import java.util.Collection;

import org.n52.io.response.OptionalOutput;
import org.n52.io.response.ParameterOutput;
import org.n52.io.response.SelfSerializedOutput;

public class DatasetParameters extends SelfSerializedOutput {

    private ParameterOutput phenomenon;

    private ParameterOutput procedure;

    private ParameterOutput category;

    private ParameterOutput offering;

    private ParameterOutput service;

    private ParameterOutput platform;

    private OptionalOutput<Collection<ParameterOutput>> tags;

    public ParameterOutput getPhenomenon() {
        return phenomenon;
    }

    public DatasetParameters setPhenomenon(ParameterOutput phenomenon) {
        this.phenomenon = phenomenon;
        return this;
    }

    public ParameterOutput getProcedure() {
        return procedure;
    }

    public DatasetParameters setProcedure(ParameterOutput procedure) {
        this.procedure = procedure;
        return this;
    }

    public ParameterOutput getCategory() {
        return category;
    }

    public DatasetParameters setCategory(ParameterOutput category) {
        this.category = category;
        return this;
    }

    public ParameterOutput getOffering() {
        return offering;
    }

    public DatasetParameters setOffering(ParameterOutput offering) {
        this.offering = offering;
        return this;
    }

    public ParameterOutput getService() {
        return service;
    }

    public DatasetParameters setService(ParameterOutput service) {
        this.service = service;
        return this;
    }

    public ParameterOutput getPlatform() {
        return platform;
    }

    public DatasetParameters setPlatform(ParameterOutput platform) {
        this.platform = platform;
        return this;
    }

    public Collection<ParameterOutput> getTags() {
        return getIfSerialized(tags);
    }

    public DatasetParameters setTags(Collection<ParameterOutput> tags) {
        return setTags(OptionalOutput.of(tags));
    }

    public DatasetParameters setTags(OptionalOutput<Collection<ParameterOutput>> tags) {
        this.tags = tags;
        return this;
    }

}
