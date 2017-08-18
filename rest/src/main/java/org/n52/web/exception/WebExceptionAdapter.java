package org.n52.web.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class WebExceptionAdapter extends RuntimeException implements WebException {
    
    private static final long serialVersionUID = 8960179333452332350L;

    private List<String> details;

    public WebExceptionAdapter(String message, Throwable cause) {
        super(message, cause);
    }

    public WebExceptionAdapter(String message) {
        super(message);
    }

    @Override
    public WebExceptionAdapter addHint(String... details) {
        if (details != null) {
            Arrays.asList(details)
                  .stream()
                  .forEach(d -> addHint(d));
        }
        return this;
    }

    @Override
    public WebExceptionAdapter addHint(String hint) {
        if (hint == null) {
            return this;
        }
        if (getHints() == null) {
            this.details = new ArrayList<>();
        }
        this.details.add(hint);
        return this;
    }

    @Override
    public String[] getHints() {
        return details == null ? null : details.toArray(new String[0]);
    }

    @Override
    public Throwable getThrowable() {
        return this;
    }

}
