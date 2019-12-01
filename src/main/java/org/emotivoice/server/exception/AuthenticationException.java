package org.emotivoice.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Unauthorized_101")
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
