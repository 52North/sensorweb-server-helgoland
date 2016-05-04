package org.n52.web.ctrl;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.servlet.ServletConfig;
import org.n52.web.common.RequestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.ServletConfigAware;

/**
 * TODO: JavaDoc
 *
 * @author <a href="mailto:h.bredel@52north.org">Henning Bredel</a>
 */
@RestController
public class RootController implements ServletConfigAware {

    private ServletConfig servletConfig;

    @RequestMapping(value = "/")
    public String getFallbackPage() throws IOException, URISyntaxException {
        final String requestUrl = RequestUtils.resolveFullRequestUrl();
        return "index.jsp";
    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

}
