package org.n52.io.crs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class BoundingBoxTest {

    @Test
    public void testContainingGeometryIsContained() throws ParseException {
        Point ll = (Point) createGeometry("POINT (0 0)");
        Point ur = (Point) createGeometry("POINT (10 10)");
        BoundingBox bbox = new BoundingBox(ll, ur, "EPSG:4326");

        Geometry overlappingPolygon = createGeometry("POLYGON ((0 1, 9 0, 3 3, 0 1))");
        assertTrue("geometry is not contained by bbox", bbox.contains(overlappingPolygon));
    }

    @Test
    public void testOverlappingGeometryIsNotContained() throws ParseException {
        Point ll = (Point) createGeometry("POINT (0 0)");
        Point ur = (Point) createGeometry("POINT (10 10)");
        BoundingBox bbox = new BoundingBox(ll, ur, "EPSG:4326");

        Geometry overlappingPolygon = createGeometry("POLYGON ((0 1, 11 0, 3 3, 0 1))");
        assertFalse("geometry is contained by bbox", bbox.contains(overlappingPolygon));
    }

    private Geometry createGeometry(String wkt) throws ParseException {
        WKTReader wktReader = new WKTReader(new GeometryFactory());
         return wktReader.read(wkt);
    }

}
