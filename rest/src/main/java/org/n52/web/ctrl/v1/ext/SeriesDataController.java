package org.n52.web.ctrl.v1.ext;

import javax.servlet.http.HttpServletResponse;
import org.n52.io.request.RequestSimpleParameterSet;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
public interface SeriesDataController {

    @RequestMapping(value = "/data", produces = {"application/json"}, method = POST)
    ModelAndView getSeriesCollectionData(
            HttpServletResponse response,
            @RequestBody RequestSimpleParameterSet parameters) throws Exception;

    @RequestMapping(value = "/{seriesId}/data", produces = {"application/json"}, method = GET)
    ModelAndView getSeriesData(
            HttpServletResponse response,
            @PathVariable String seriesId,
            @RequestParam(required = false) MultiValueMap<String, String> query);
}
