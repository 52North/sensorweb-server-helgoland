package org.n52.io.v1.data;

import org.n52.io.geojson.GeojsonPoint;

public class BBox {
    
    private GeojsonPoint ll;
    
    private GeojsonPoint ur;

    public BBox(GeojsonPoint ll, GeojsonPoint ur) {
        this.ll = ll;
        this.ur = ur;
    }

    public GeojsonPoint getLl() {
        return ll;
    }

    public void setLl(GeojsonPoint ll) {
        this.ll = ll;
    }

    public GeojsonPoint getUr() {
        return ur;
    }

    public void setUr(GeojsonPoint ur) {
        this.ur = ur;
    }
    
}
