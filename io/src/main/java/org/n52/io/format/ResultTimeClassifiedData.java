
package org.n52.io.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.io.response.dataset.AbstractValue;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

public class ResultTimeClassifiedData<T extends AbstractValue< ? >> {

    private Map<String, List<AbstractValue< ? >>> valuesByResultTime;

    public ResultTimeClassifiedData() {
        valuesByResultTime = new HashMap<>();
    }

    @JsonAnyGetter
    public Map<String, List<AbstractValue< ? >>> getValuesByResultTime() {
        return valuesByResultTime;
    }

    public void setValuesByResultTime(Map<String, List<AbstractValue< ? >>> valuesByResultTime) {
        this.valuesByResultTime = valuesByResultTime;
    }

    /**
     * Classifies the given value into result time (if present).
     * 
     * @param value
     *        the value to classify.
     */
    public void classifyValue(T value) {
        Long resultTime = value.getResultTime();
        if (resultTime != null) {
            String rt = Long.toString(resultTime);
            if (!valuesByResultTime.containsKey(rt)) {
                valuesByResultTime.put(rt, new ArrayList<>());
            }
            valuesByResultTime.get(rt)
                              .add(value);
        }
    }

}
