package org.fao.sola.clients.android.opentenure.exceptions;

public class OpenTenureException extends Exception {
    public OpenTenureException() {
        super();
    }

    public OpenTenureException(String message) {
        super(message);
    }

    public OpenTenureException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenTenureException(Throwable cause) {
        super(cause);
    }
}
