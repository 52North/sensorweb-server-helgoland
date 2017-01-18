/*
 * Copyright (C) 2013-2017 52°North Initiative for Geospatial Open Source
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
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class SandboxTest {

    @Test
    public void when_concreteTypePassedToClassWithOverloadedMethods_then_mostConcreteMethodIsUsed() {
        final AbstractOverloader overloader = new ConcreteOverloader();
        Assert.assertTrue(overloader.pass(new ConcreteType()));
    }

    @Test
    public void when_abstractTypePassedToClassWithOverloadedMethods_then_abstractMethodIsUsed() {
        final AbstractOverloader overloader = new ConcreteOverloader();
        Assert.assertFalse(overloader.pass(new AbstractType() {}));
    }

    private static abstract class AbstractType {}

    private static class ConcreteType extends AbstractType {}

    private abstract static class AbstractOverloader {
        abstract boolean pass(Object type);
    }

    private static class ConcreteOverloader extends AbstractOverloader {
        @Override
        boolean pass(Object type) {
            return false;
        }
        boolean pass(ConcreteType type) {
            return true;
        }
    }
}
