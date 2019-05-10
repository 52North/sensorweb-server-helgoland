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
package org.n52.io.response.extension;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.n52.io.request.IoParameters;
import org.n52.io.response.ParameterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LicenseExtension extends MetadataExtension<ParameterOutput> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseExtension.class);

    private static final String CONFIG_FILE = "config-license.txt";

    private static final String EXTENSION_NAME = "license";

    private final String licenseText;

    public LicenseExtension() {
        this(CONFIG_FILE);
    }

    public LicenseExtension(String configFile) {
        this.licenseText = readLicenseText(configFile);
    }

    private String readLicenseText(String configFile) {
        try {
            Path root = Paths.get(getClass().getResource("/").toURI());
            final Path target = Paths.get(configFile);
            File file = !Files.exists(target)
                    ? root.resolve(configFile).toFile()
                    : target.toFile();
            if (file.exists()) {
                return FileUtils.readFileToString(file);
            }
            ClassLoader classLoader = getClass().getClassLoader();
            InputStream is = classLoader.getResourceAsStream("/" + configFile);
            if (is == null) {
                LOGGER.error("Could not find license config file '{}'", file.getPath());
                return "";
            }
            return readFromInputStream(is);
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Could not load {}. Using empty license.", CONFIG_FILE, e);
        }
        return "";
    }

    private String readFromInputStream(InputStream stream) {
        try (Scanner scanner = new Scanner(stream, "UTF-8")) {
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
            return sb.toString();
        }
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Map<String, Object> getExtras(ParameterOutput output, IoParameters parameters) {
        return hasExtrasToReturn(output, parameters)
                ? wrapSingleIntoMap(licenseText)
                : Collections.emptyMap();
    }

}
