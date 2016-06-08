package org.n52.web.ctrl;

import org.n52.io.response.OutputCollection;
import org.n52.io.response.ParameterOutput;
import org.springframework.web.servlet.ModelAndView;

public class ParameterSimpleArrayCollectionAdapter<T extends ParameterOutput> extends ParameterController<T> {

    @Override
    protected ModelAndView createModelAndView(OutputCollection<T> items) {
        return new ModelAndView().addObject(items.getItems());
    }
}
