/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.n52.proxy.connector;

/**
 *
 * @author jansch
 */
public class DatasetConstellation {

    private final String procedure;
    private final String offering;
    private final String category;
    private final String phenomenon;
    private final String feature;

    public DatasetConstellation(String procedure, String offering, String category, String phenomenon, String feature) {
        this.procedure = procedure;
        this.offering = offering;
        this.category = category;
        this.phenomenon = phenomenon;
        this.feature = feature;
    }

    public String getProcedure() {
        return procedure;
    }

    public String getOffering() {
        return offering;
    }

    public String getCategory() {
        return category;
    }

    public String getPhenomenon() {
        return phenomenon;
    }

    public String getFeature() {
        return feature;
    }

    @Override
    public String toString() {
        return "DatasetConstellation{" + "procedure=" + procedure + ", offering=" + offering + ", category=" + category + ", phenomenon=" + phenomenon + ", feature=" + feature + '}';
    }

}
