package org.n52.io.generalize;

import org.n52.io.format.TvpDataCollection;

public interface Generalizer {

    public TvpDataCollection generalize() throws GeneralizerException;
}
