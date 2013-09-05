package org.n52.io.v1.data.search;

public class StationSearchResult extends SearchResult {

	public StationSearchResult(String id, String label) {
		super(id, label);
	}

	@Override
	public String getHref() {
		return "./stations/" + getId();
	}

	@Override
	public String getType() {
		return "station";
	}

}
