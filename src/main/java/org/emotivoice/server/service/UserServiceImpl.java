package org.emotivoice.server.service;

import java.nio.file.Paths;
import java.util.concurrent.ThreadLocalRandom;

import org.emotivoice.server.User;
import org.emotivoice.server.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private static final int TOKEN_SIZE = 6;
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private UserRepository userRepository;

    @Value("${wav_dir}")
    private String wavDir;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User generateUser() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TOKEN_SIZE; ++i) {
            int idx = ThreadLocalRandom.current().nextInt(0, LETTERS.length());
            sb.append(LETTERS.charAt(idx));
        }

        String token = sb.toString();
        Paths.get(wavDir, token).toFile().mkdir();

        return userRepository.save(new User(token));
    }

}
