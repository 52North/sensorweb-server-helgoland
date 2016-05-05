package org.n52.series.db.da.beans.ext;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.n52.series.db.da.beans.UnitEntity;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public class MeasurementEntity extends AbstractObservationEntity {

    private int numberOfDecimals;

    private UnitEntity unit;

    private Set<MeasurementEntity> referenceValues = new HashSet<>();

    private MeasurementEntity firstValue;

    private MeasurementEntity lastValue;

    public Set<MeasurementEntity> getReferenceValues() {
        return referenceValues;
    }

    public void setReferenceValues(Set<MeasurementEntity> referenceValues) {
        this.referenceValues = referenceValues;
    }

    public int getNumberOfDecimals() {
        return numberOfDecimals;
    }

    public void setNumberOfDecimals(int numberOfDecimals) {
        this.numberOfDecimals = numberOfDecimals;
    }

    public UnitEntity getUnit() {
        return unit;
    }

    public void setUnit(UnitEntity unit) {
        this.unit = unit;
    }

    public MeasurementEntity getFirstValue() {
        if (firstValue != null) {
            Date when = firstValue.getTimestamp();
            Double value = firstValue.getValue();
            if (when == null || value == null) {
                return null; // empty component
            }
        }
        return firstValue;
    }

    public void setFirstValue(MeasurementEntity firstValue) {
        this.firstValue = firstValue;
    }

    public MeasurementEntity getLastValue() {
        if (lastValue != null) {
            Date when = lastValue.getTimestamp();
            Double value = lastValue.getValue();
            if (when == null || value == null) {
                return null; // empty component
            }
        }
        return lastValue;
    }

    public void setLastValue(MeasurementEntity lastValue) {
        this.lastValue = lastValue;
    }

}
