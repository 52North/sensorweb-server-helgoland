package org.n52.io.v1.data.search;

public class ProcedureSearchResult extends SearchResult {

	public ProcedureSearchResult(String id, String label) {
		super(id, label);
	}

	@Override
	public String getHref() {
		return "./procedures/" + getId();
	}

	@Override
	public String getType() {
		return "procedure";
	}

}
