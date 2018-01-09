/*
 * Copyright (C) 2013-2018 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public License
 * version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 */
package org.n52.io.response;

import java.util.Optional;

/**
 * Takes a value which may be marked as serialized or not. A caller still can force to retrieve the actual
 * value even not marked for serialization (by setting the appropriate flag). By default a value will be
 * marked to be serialized.
 *
 * @param <T>
 *        the type of the output value to wrap.
 */
public final class OptionalOutput<T> {

    private Optional<T> value;

    private boolean serialize;

    private OptionalOutput(T value, boolean serialize) {
        this.value = Optional.ofNullable(value);
        this.serialize = serialize;
    }

    /**
     * Creates an instance which is serialized by default.
     *
     * @param value
     *        the output value (can be <tt>null</tt>)
     * @return a optional value which is serialized by default.
     */
    public static <T> OptionalOutput<T> of(T value) {
        return of(value, true);
    }

    /**
     * Creates an instance with a flag indicating if given value shall be serialized or not.
     *
     * @param value
     *        the output value (can be <tt>null</tt>)
     * @param serialize
     *        <tt>true</tt> if the value shall be serialized, <tt>false</tt> otherwise.
     * @return a optional value indicating if it shall be serialied or not.
     */
    public static <T> OptionalOutput<T> of(T value, boolean serialize) {
        return new OptionalOutput<T>(value, serialize);
    }

    public boolean isSerialize() {
        return serialize;
    }

    public boolean isPresent() {
        return value.isPresent();
    }

    public boolean isAbsent() {
        return !value.isPresent();
    }

    /**
     * @return the value if it shall be serialized, <tt>null</tt> otherwise.
     */
    public T getValue() {
        return getValue(false);
    }

    /**
     * @param forced
     *        if value shall be returned independent of serialization flag is set or not.
     * @return the acutal set value (can be <tt>null</tt>).
     */
    public T getValue(boolean forced) {
        return forced || isSerialize()
                ? value.get()
                : null;
    }

    // TODO implement hashCode and equals

}
