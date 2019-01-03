/*
 * Copyright (C) 2013-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.io.response.sampling;

import java.util.List;

import org.n52.io.response.OptionalOutput;
import org.n52.io.response.ParameterOutput;

public class SamplingOutput extends ParameterOutput {

    public static final String COLLECTION_PATH = "samplings";
    public static final String COMMENT = "comment";
    public static final String MONITORING_PROGRAM = "measuringProgram";
    public static final String SAMPLER = "sampler";
    public static final String SAMPLING_METHOD = "samplingMehtod";
    public static final String ENVIRONMENTAL_CONDITIONS = "environmentalConditions";
    public static final String SAMPLING_TIME_START = "samplingTimeStart";
    public static final String SAMPLING_TIME_END = "samplingTimeEnd";
    public static final String LAST_SAMPLING_OBSERVATIONS = "lastSamplingObservations";

    private OptionalOutput<String> comment;
    private OptionalOutput<MeasuringProgramOutput> measuringProgram;
    private OptionalOutput<SamplerOutput> sampler;
    private OptionalOutput<String> samplingMehtod;
    private OptionalOutput<String> environmentalConditions;
    private OptionalOutput<Long> samplingTimeStart;
    private OptionalOutput<Long> samplingTimeEnd;

    private OptionalOutput<List<SamplingObservationOutput>> lastSamplingObservations;

    @Override
    protected String getCollectionName() {
        return COLLECTION_PATH;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return getIfSerialized(comment);
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(OptionalOutput<String> comment) {
        this.comment = comment;
    }

    /**
     * @return the measuringProgram
     */
    public MeasuringProgramOutput getMeasuringProgram() {
        return getIfSerialized(measuringProgram);
    }

    /**
     * @param measuringProgram the measuringProgram to set
     */
    public void setMeasuringProgram(OptionalOutput<MeasuringProgramOutput> measuringProgram) {
        this.measuringProgram = measuringProgram;
    }

    /**
     * @return the sampler
     */
    public SamplerOutput getSampler() {
        return getIfSerialized(sampler);
    }

    /**
     * @param sampler the sampler to set
     */
    public void setSampler(OptionalOutput<SamplerOutput> sampler) {
        this.sampler = sampler;
    }

    /**
     * @return the samplingMehtod
     */
    public String getSamplingMehtod() {
        return getIfSerialized(samplingMehtod);
    }

    /**
     * @param samplingMehtod the samplingMehtod to set
     */
    public void setSamplingMehtod(OptionalOutput<String> samplingMehtod) {
        this.samplingMehtod = samplingMehtod;
    }

    /**
     * @return the environmentalConditions
     */
    public String getEnvironmentalConditions() {
        return getIfSerialized(environmentalConditions);
    }

    /**
     * @param environmentalConditions the environmentalConditions to set
     */
    public void setEnvironmentalConditions(OptionalOutput<String> environmentalConditions) {
        this.environmentalConditions = environmentalConditions;
    }

    /**
     * @return the samplingTimeStart
     */
    public Long getSamplingTimeStart() {
        return getIfSerialized(samplingTimeStart);
    }

    /**
     * @param samplingTimeStart the samplingTimeStart to set
     */
    public void setSamplingTimeStart(OptionalOutput<Long> samplingTimeStart) {
        this.samplingTimeStart = samplingTimeStart;
    }

    /**
     * @return the samplingTimeEnd
     */
    public Long getSamplingTimeEnd() {
        return getIfSerialized(samplingTimeEnd);
    }

    /**
     * @param samplingTimeEnd the samplingTimeEnd to set
     */
    public void setSamplingTimeEnd(OptionalOutput<Long> samplingTimeEnd) {
        this.samplingTimeEnd = samplingTimeEnd;
    }

    /**
     * @return the lastSamplingObservations
     */
    public List<SamplingObservationOutput> getLastSamplingObservations() {
        return getIfSerialized(lastSamplingObservations);
    }

    /**
     * @param lastSamplingObservations the lastSamplingObservations to set
     */
    public void setLastSamplingObservations(OptionalOutput<List<SamplingObservationOutput>> lastSamplingObservations) {
        this.lastSamplingObservations = lastSamplingObservations;
    }
}
