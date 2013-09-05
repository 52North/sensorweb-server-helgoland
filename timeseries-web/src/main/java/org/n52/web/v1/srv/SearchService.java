package org.n52.web.v1.srv;

import java.util.Collection;

import org.n52.io.v1.data.search.SearchResult;

public interface SearchService {
	
	Collection<SearchResult> searchResources(String search);

}
