package org.n52.io.v1.data.search;

public class CategorySearchResult extends SearchResult {

	public CategorySearchResult(String id, String label) {
		super(id, label);
	}

	@Override
	public String getHref() {
		return "./categories/" + getId();
	}

	@Override
	public String getType() {
		return "category";
	}

}
