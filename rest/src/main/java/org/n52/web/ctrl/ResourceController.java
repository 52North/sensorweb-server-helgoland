package org.n52.web.ctrl;

import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.ModelAndView;

public interface ResourceController {

    public ModelAndView getCollection(MultiValueMap<String, String> query);

    public ModelAndView getItem(String id, MultiValueMap<String, String> query);

    public void getRawData(HttpServletResponse response, String id, MultiValueMap<String, String> query);

    public Map<String, Object> getExtras(String resourceId, MultiValueMap<String, String> query);

}
