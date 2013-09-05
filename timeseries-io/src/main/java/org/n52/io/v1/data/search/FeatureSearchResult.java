package org.n52.io.v1.data.search;

public class FeatureSearchResult extends SearchResult {

	public FeatureSearchResult(String id, String label) {
		super(id, label);
	}

	@Override
	public String getHref() {
		return "./features/" + getId();
	}

	@Override
	public String getType() {
		return "feature";
	} 

}
