package org.n52.series.db.da.beans.ext;

import java.util.List;
import org.n52.io.response.v1.ext.PlatformType;
import org.n52.series.db.da.beans.DescribableEntity;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class PlatformEntity extends DescribableEntity<I18nPlatformEntity> {

    private String platformType = PlatformType.STATIONARY_INSITU.getTypeName();

    private List<AbstractSeriesEntity> series;

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }

    public List<AbstractSeriesEntity> getSeries() {
        return series;
    }

    public void setSeries(List<AbstractSeriesEntity> series) {
        this.series = series;
    }

}
