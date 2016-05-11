package org.n52.series.db.da.beans.ext;

import java.util.Set;
import org.n52.io.response.v1.ext.PlatformType;
import org.n52.series.db.da.beans.DescribableEntity;
import org.n52.series.db.da.beans.FeatureEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class PlatformEntity extends DescribableEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformEntity.class);

    private Set<AbstractSeriesEntity> series;

    private PlatformType platformType;

    public void setPlatformType(String platformType) {
        this.platformType = PlatformType.isKnownType(platformType)
                ? PlatformType.toInstance(platformType)
                : null;
    }

    public PlatformType getPlatformType() {
        PlatformType type = platformType;
        if (series == null) {
            return PlatformType.STATIONARY_INSITU;
        }
        for (AbstractSeriesEntity entity : series) {
            final FeatureEntity feature = entity.getFeature();
            final String concept = feature.getFeatureConcept();
            if (!PlatformType.isKnownType(concept)) {
                LOGGER.warn("unknown feature concept for feature '{}': {}", feature.getPkid(), concept);
                continue;
            }
            // TODO log warning when not consistent?!
//            PlatformType tmp = PlatformType.toInstance(concept);
//            if (type != null && tmp != type) {
//                LOGGER.warn("Different platform type referenced: {} vs. {}", tmp, type);
//            } else {
//                type = tmp;
//            }
            type = PlatformType.toInstance(concept);
            break;
        }
        return type == null
                ? PlatformType.STATIONARY_INSITU
                : type;
    }

    public Set<AbstractSeriesEntity> getSeries() {
        return series;
    }

    public void setSeries(Set<AbstractSeriesEntity> series) {
        this.series = series;
    }

}
