package org.n52.web.ctrl.v1.ext;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.n52.io.response.OutputCollection;
import org.n52.io.v1.data.RawFormats;
import org.n52.web.ctrl.ParameterController;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@RequestMapping(produces = {"application/json"})
public abstract class ExtParameterRequestMappingAdapter extends ParameterController {

    @Override
    protected ModelAndView createModelAndView(OutputCollection<?> items) {
        return new ModelAndView().addObject(items.getItems());
    }

    @RequestMapping(method = GET)
    public ModelAndView getCollection(@RequestParam MultiValueMap<String, String> query) {
        return super.getCollection(query);
    }

    @RequestMapping(value = "/{item}", method = GET)
    public ModelAndView getItem(@PathVariable("item") String id, @RequestParam MultiValueMap<String, String> query) {
        return super.getItem(id, query);
    }

    @RequestMapping(value = "/{item}", method = GET, params = {RawFormats.RAW_FORMAT})
    public void getRawData(HttpServletResponse response,
            @PathVariable("item") String id, @RequestParam MultiValueMap<String, String> query) {
        super.getRawData(response, id, query);
    }

    @RequestMapping(value = "/{item}/extras", method = GET)
    public Map<String, Object> getExtras(@PathVariable("item") String resourceId,
            @RequestParam(required = false) MultiValueMap<String, String> query) {
        return super.getExtras(resourceId, query);
    }
}
