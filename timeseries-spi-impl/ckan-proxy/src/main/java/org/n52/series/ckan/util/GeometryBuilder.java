package org.n52.series.ckan.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import org.n52.io.crs.CRSUtils;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeometryBuilder {
    
    // TODO add line string builder  
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GeometryBuilder.class);
    
    private final CRSUtils utils = CRSUtils.createEpsgStrictAxisOrder();
    
    private String crs = "EPSG:4326";
    
    private double longitude;
    
    private double latitude;
    
    private double altitude;
    
    public static GeometryBuilder create() {
        return new GeometryBuilder();
    }
    
    public GeometryBuilder withCrs(String crs) {
        this.crs = crs;
        return this;
    }
    
    public GeometryBuilder setLongitude(String lon) {
        this.longitude = parseToDouble(lon);
        return this;
    }

    public GeometryBuilder setLatitude(String lat) {
        this.latitude = parseToDouble(lat);
        return this;
    }
    
    public GeometryBuilder setAltitude(String alt) {
        this.altitude = parseToDouble(alt);
        return this;
    }
    
    private double parseToDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            LOGGER.error("invalid coordinate value: {}", value, e);
            return 0d;
        }
    }
    
    public Geometry getPoint() {
        final Point lonLatPoint = utils.createPoint(longitude, latitude, altitude, crs);
        try {
            return utils.transformInnerToOuter(lonLatPoint, crs);
        } catch (TransformException | FactoryException e) {
            LOGGER.error("could not switch axes to conform strictly to {}", crs, e);
            return lonLatPoint;
        }
    }
    
}
