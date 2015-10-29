package org.n52.series.db.da.beans.v2;

import java.util.Date;

import com.vividsolutions.jts.geom.Geometry;

public class TrackLocationEntity {

	private Long pkid;

    private Date timestamp;

    private Geometry geom;

    public Long getPkid() {
        return pkid;
    }

    public void setPkid(Long pkid) {
        this.pkid = pkid;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Geometry getGeom() {
        return geom;
    }

    public void setGeom(Geometry geom) {
        this.geom = geom;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [");
        sb.append(" id: ").append(pkid);
        return sb.append(" ]").toString();
    }
    
    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof TrackLocationEntity) {
    		return this.timestamp.equals(((TrackLocationEntity) obj).getTimestamp());
    	}
    	return super.equals(obj);
    }
}
