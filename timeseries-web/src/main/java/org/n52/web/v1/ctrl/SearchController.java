package org.n52.web.v1.ctrl;

import static org.n52.web.v1.ctrl.RestfulUrls.DEFAULT_PATH;
import static org.n52.web.v1.ctrl.RestfulUrls.SEARCH;

import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.n52.io.v1.data.search.SearchResult;
import org.n52.web.BadRequestException;
import org.n52.web.BaseController;
import org.n52.web.v1.srv.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = DEFAULT_PATH + "/" + SEARCH, produces = {"application/json"})
public class SearchController extends BaseController {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(SearchController.class);
	
	private SearchService searchService;
	
	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView searchResources(HttpServletResponse response, @RequestParam String q) {
		
		if (q == null) {
			throw new BadRequestException("Use parameter 'q' with search string to define your search term.");
		}
		
		Collection<SearchResult> resultedResources = searchService.searchResources(q);
		
		return new ModelAndView().addObject(resultedResources);
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

}
