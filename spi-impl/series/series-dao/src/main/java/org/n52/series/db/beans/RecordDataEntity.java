package org.n52.series.db.beans;

import java.util.Collection;
import java.util.Map;

public class RecordDataEntity extends DataEntity<Map<String, Object>> {

    @Override
    public boolean isNoDataValue(Collection<String> noDataValues) {
        return getValue() == null || getValue().isEmpty();
    }

    public int getDimension() {
        return getValue() != null
                ? getValue().size()
                : 0;
    }

}
