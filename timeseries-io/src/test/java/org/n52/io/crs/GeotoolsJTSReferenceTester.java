package org.n52.io.crs;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class GeotoolsJTSReferenceTester {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GeotoolsJTSReferenceTester.class);

    @Before
    public void setUp() {
        
    }
    
    @Test 
    public void shouldBlah() throws Exception {
        CRSUtils forcedXYOrder = CRSUtils.createEpsgForcedXYAxisOrder();
        GeometryFactory xyFactory = forcedXYOrder.createGeometryFactory("EPSG:4326");
        Point forcedXYPoint = xyFactory.createPoint(new Coordinate(7.4, 52.3));
        LOGGER.info("EPSG:4326 as JTS point (forced XY): {}", forcedXYPoint);
        LOGGER.info("Transformed to EPSG:25832: {}", forcedXYOrder.transform(forcedXYPoint, "EPSG:4326", "EPSG:25832"));
        
        CRSUtils respectEpsgOrder = CRSUtils.createEpsgStrictAxisOrder();
        GeometryFactory strictFactory = respectEpsgOrder.createGeometryFactory("EPSG:4326");
        Point strictPoint = strictFactory.createPoint(new Coordinate(52.3, 7.4));
        LOGGER.info("EPSG:4326 as JTS point (strict EPSG order): {}", strictPoint);
        LOGGER.info("Transformed to EPSG:25832: {}", respectEpsgOrder.transform(strictPoint, "EPSG:4326", "EPSG:25832"));
    }
    
    @Test
    public void shouldCreateCRS84() throws Exception {
        CRSUtils respectEpsgOrder = CRSUtils.createEpsgStrictAxisOrder();
        GeometryFactory strictFactory = respectEpsgOrder.createGeometryFactory("EPSG:4326");
        Point strictPoint = strictFactory.createPoint(new Coordinate(52.3, 7.4));
        LOGGER.info("EPSG:4326 as JTS point (strict EPSG order): {}", strictPoint);
        LOGGER.info("Transformed to CRS:84: {}", respectEpsgOrder.transform(strictPoint, "EPSG:4326", "CRS:84"));
    }
    
    
}
