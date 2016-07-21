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
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureIterator;
import org.n52.io.DatasetFactory;
import org.n52.series.dwd.store.AlertStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;

public class ShapeFileHarvester implements GeometryHarvester {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShapeFileHarvester.class);

    private static final String GEOMETRY_VERWALTUNGSGEBIETE_2500 = "geometries/DWD-PVW-Customer_VG2500.shp";

    private AlertStore store;

    private File file;

    public ShapeFileHarvester(AlertStore store) {
        this(getDefaultShapeFile(GEOMETRY_VERWALTUNGSGEBIETE_2500), store);
    }

    protected static File getDefaultShapeFile(String configFile) {
        try {
            Path path = Paths.get(ShapeFileHarvester.class.getResource("/").toURI());
            return path.resolve(configFile).toFile();
        } catch (URISyntaxException e) {
            LOGGER.info("Could not find shape file '{}'. Load from compiled default.", configFile, e);
            return null;
        }
    }

    public ShapeFileHarvester(File shpFile, AlertStore store) {
        this.store = store;
        this.file = shpFile;
    }

    protected Map<String, Geometry> loadGeometries() throws IOException {
        Map<String, Geometry> result = new HashMap<String, Geometry>();
        if (file != null && file.exists()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("url", file.toURI().toURL());

            DataStore dataStore = DataStoreFinder.getDataStore(map);
            String typeName = dataStore.getTypeNames()[0];

            FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
                    .getFeatureSource(typeName);

            try (FeatureIterator<SimpleFeature> features = source.getFeatures(Filter.INCLUDE).features()) {
                while (features.hasNext()) {
                    SimpleFeature feature = features.next();
                    result.put(feature.getAttribute("WARNCELLID").toString(), (Geometry)feature.getDefaultGeometryProperty().getValue());
                }
            }
        }
        return result;
    }

    @Override
    public void harvest() {
        try {

            // TODO set geometries in WarnCell!

            store.setWarnCellGeometries(loadGeometries());
        } catch (IOException e) {
            LOGGER.warn("Unable to harvest file.", e);
        }
    }


}
