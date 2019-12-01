package org.emotivoice.server.service;

import org.emotivoice.server.User;
import org.emotivoice.server.exception.AuthenticationException;

public interface AuthenticationService {

    User authenticate(String token) throws AuthenticationException;
}
