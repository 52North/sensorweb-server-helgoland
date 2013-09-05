package org.n52.io.v1.data.search;

public class ServiceSearchResult extends SearchResult {

	public ServiceSearchResult(String id, String label) {
		super(id, label);
	}

	@Override
	public String getHref() {
		return "./services/" + getId();
	}

	@Override
	public String getType() {
		return "service";
	}

}
