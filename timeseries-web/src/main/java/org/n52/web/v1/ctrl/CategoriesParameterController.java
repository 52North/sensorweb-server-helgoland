package org.n52.web.v1.ctrl;

import static org.n52.web.v1.ctrl.Stopwatch.startStopwatch;

import org.n52.io.v1.data.CategoryOutput;
import org.n52.web.ResourceNotFoundException;
import org.n52.web.v1.srv.ParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = RestfulUrls.DEFAULT_PATH + "/" + RestfulUrls.COLLECTION_CATEGORIES, produces = {"application/json"})
public class CategoriesParameterController extends ParameterController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CategoriesParameterController.class);
	
	private ParameterService<CategoryOutput> categoryParameterService;
	
	public ModelAndView getCollection(@RequestParam(required=false) MultiValueMap<String, String> query) {
		QueryMap queryMap = QueryMap.createFromQuery(query);
		
        if (queryMap.shallExpand()) {
            Stopwatch stopwatch = startStopwatch();
            Object[] result = categoryParameterService.getExpandedParameters(queryMap);
            LOGGER.debug("Processing request took {} seconds.", stopwatch.stopInSeconds());

            // TODO add paging
            
            return new ModelAndView().addObject(result);
        } else {
            Object[] result = categoryParameterService.getCondensedParameters(queryMap);

            // TODO add paging
            
            return new ModelAndView().addObject(result);
        }
	}

	public ModelAndView getItem(@PathVariable("item") String categoryId, @RequestParam(required=false) MultiValueMap<String, String> query) {
		QueryMap map = QueryMap.createFromQuery(query);

        // TODO check parameters and throw BAD_REQUEST if invalid

        CategoryOutput category = categoryParameterService.getParameter(categoryId);

        if (category == null) {
            throw new ResourceNotFoundException("Found no category with given id.");
        }

        return new ModelAndView().addObject(category);
	}

	public ParameterService<CategoryOutput> getCategoryParameterService() {
		return categoryParameterService;
	}

	public void setCategoryParameterService(
			ParameterService<CategoryOutput> categoryParameterService) {
		this.categoryParameterService = categoryParameterService;
	}


}
