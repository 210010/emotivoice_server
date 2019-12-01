package org.emotivoice.server.service;

import org.emotivoice.server.User;
import org.emotivoice.server.UserRepository;
import org.emotivoice.server.exception.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    @Autowired
    public AuthenticationServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User authenticate(String token) throws AuthenticationException {
        User user = userRepository.findByToken(token);
        if (user == null) {
            throw new AuthenticationException("Invalid credentials");
        }
        return user;
    }
}
