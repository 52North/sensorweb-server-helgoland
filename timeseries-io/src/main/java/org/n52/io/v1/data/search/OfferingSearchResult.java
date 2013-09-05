package org.n52.io.v1.data.search;

public class OfferingSearchResult extends SearchResult {

	public OfferingSearchResult(String id, String label) {
		super(id, label);
	}

	@Override
	public String getHref() {
		return "./offerings/" + getId();
	}

	@Override
	public String getType() {
		return "offering";
	}

}
