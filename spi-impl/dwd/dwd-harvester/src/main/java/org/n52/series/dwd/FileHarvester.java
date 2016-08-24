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
package org.n52.series.dwd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.joda.time.DateTime;
import org.n52.series.dwd.rest.JacksonBasedAlertParser;
import org.n52.series.dwd.store.AlertStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FileHarvester implements DwdHarvester {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileHarvester.class);

    private AlertStore store;

    private AlertParser parser;

    private DateTime lastHarvestedAt;

    private File file;

    public FileHarvester(AlertStore store) {
        this(store, new JacksonBasedAlertParser());
    }

    public FileHarvester(AlertStore store, AlertParser parser) {
        this.parser = parser;
        this.store = store;
    }

    public AlertStore getStore() {
        return store;
    }

    @Override
    public void harvest() {
        parseFile();
        this.lastHarvestedAt = DateTime.now();
    }

    private void parseFile() {
        try(InputStream is = new FileInputStream(file)) {
            parser.parse(is, store);
        } catch (IOException | ParseException e) {
            LOGGER.warn("Unable to harvest file.", e);
        }
    }

    @Override
    public DateTime getLastHarvestedAt() {
        return this.lastHarvestedAt;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setFilePath(String filePath) {
        this.file = new File(filePath);
    }

    public static FileHarvester.Builder aHarvester(AlertStore store) {
        return new Builder(store);
    }

    static class Builder {

        private final FileHarvester harvester;

        private Builder(AlertStore store) {
            this.harvester = new FileHarvester(store);
        }

        FileHarvester.Builder withParser(AlertParser parser) {
            this.harvester.parser = parser;
            return this;
        }

        FileHarvester.Builder withFile(File file) {
            this.harvester.file = file;
            return this;
        }

        DwdHarvester build() {
            return harvester;
        }

    }


}
