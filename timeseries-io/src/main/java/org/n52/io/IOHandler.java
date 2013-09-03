/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.io;

import java.io.IOException;
import java.io.OutputStream;

import org.n52.io.format.TvpDataCollection;

public interface IOHandler {

    /**
     * @param data
     *        the input data collection to create an output for.
     * @throws TimeseriesIOException
     *         if ouput generation fails.
     */
    public void generateOutput(TvpDataCollection data) throws TimeseriesIOException;

    /**
     * Encodes and writes previously generated output to the given stream. After handling the stream gets
     * flushed and closed.
     * 
     * @param stream
     *        the stream to write on the generated ouput.
     * @throws IOException
     *         if writing output to stream fails.
     */
    public void encodeAndWriteTo(OutputStream stream) throws TimeseriesIOException;
}
