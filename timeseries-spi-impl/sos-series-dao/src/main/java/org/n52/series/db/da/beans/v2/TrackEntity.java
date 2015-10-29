package org.n52.series.db.da.beans.v2;

import java.util.List;

import org.n52.series.db.da.beans.DescribableEntity;

public class TrackEntity extends DescribableEntity<I18nTrackEntity> {

	private List<TrackLocationEntity> trackLocations;

	public List<TrackLocationEntity> getTrackLocations() {
		return trackLocations;
	}

	public void setTrackLocations(List<TrackLocationEntity> trackLocations) {
		this.trackLocations = trackLocations;
	}
	
	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName()).append(" [");
        sb.append(" Canonical id: ").append(getCanonicalId());
        return sb.append(" ]").toString();
    }
	
}
