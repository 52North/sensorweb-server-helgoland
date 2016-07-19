package org.n52.io;

public class IoHandlerException extends Exception {

    private static final long serialVersionUID = 65118552505433367L;

    public IoHandlerException(String message) {
        super(message);
    }

    public IoHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

}
