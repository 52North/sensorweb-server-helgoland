package org.n52.web.ctrl;

import org.n52.io.response.OutputCollection;
import org.springframework.web.servlet.ModelAndView;

public class ParameterSimpleArrayCollectionAdapter extends ParameterController {

    @Override
    protected ModelAndView createModelAndView(OutputCollection<?> items) {
        return new ModelAndView().addObject(items.getItems());
    }
}
