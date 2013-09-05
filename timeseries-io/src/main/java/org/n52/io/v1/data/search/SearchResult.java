package org.n52.io.v1.data.search;

public abstract class SearchResult {
	
	private String id;
	
	private String label;
	
	public SearchResult(String id, String label) {
		this.id = id;
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public abstract String getHref();
	
	public abstract String getType();
	
}
