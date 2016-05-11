package org.n52.series.db.da.beans.ext;

import org.n52.series.db.da.beans.FeatureEntity;

/**
 * TODO: JavaDoc
 *
 * @since 2.0.0
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class SiteFeatureEntity extends FeatureEntity {

    /**
     * @since 2.0.0
     */
    private GeometryEntity geometry;

    public GeometryEntity getGeometry() {
        return geometry;
    }

    public void setGeometry(GeometryEntity geometry) {
        this.geometry = geometry;
    }

    public boolean isSetGeometry() {
        final GeometryEntity g = getGeometry();
        return g != null && g.isSetGeometry();
    }

    public boolean isSetLonLat() {
        final GeometryEntity g = getGeometry();
        return g != null && g.isSetLonLat();
    }

}
