
package org.n52.io.response.dataset;

@FunctionalInterface
public interface ValueFormatter<T> {

    /**
     * Applies this formatter to the given value.
     *
     * @param value
     *        the value to format
     * @return the formatted value
     */
    String format(T value);
}
