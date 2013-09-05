package org.n52.io.v1.data.search;

public class PhenomenonSearchResult extends SearchResult {

	public PhenomenonSearchResult(String id, String label) {
		super(id, label);
	}

	@Override
	public String getHref() {
		return "./phenomena/" + getId();
	}

	@Override
	public String getType() {
		return "phenomenon";
	}

}
