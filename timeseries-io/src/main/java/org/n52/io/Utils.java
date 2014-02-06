
package org.n52.io;

import java.lang.reflect.Array;

public final class Utils {

    private Utils() {
        // hide construction
    }

    /**
     * Copies an array via {@link System#arraycopy(Object, int, Object, int, int)}. This is useful for objects
     * encapsulating arrays from being externally modified.<br/>
     * <br/>
     * <b>Note:</b> No deep copy is made.
     * 
     * @param source the array to copy.
     * @return a copied instance of the array.
     */
    @SuppressWarnings("unchecked")
    public static final <T> T[] copy(T[] source) {
        Class< ? > type = source.getClass().getComponentType();
        T[] target = (T[]) Array.newInstance(type, source.length);
        System.arraycopy(source, 0, target, 0, source.length);
        return target;
    }
}
