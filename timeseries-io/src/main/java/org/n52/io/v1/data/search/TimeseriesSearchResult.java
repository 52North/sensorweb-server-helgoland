package org.n52.io.v1.data.search;

public class TimeseriesSearchResult extends SearchResult {

	public TimeseriesSearchResult(String id, String label) {
		super(id, label);
	}

	@Override
	public String getHref() {
		return "./timeseries/" + getId();
	}

	@Override
	public String getType() {
		return "timeseries";
	}

}
