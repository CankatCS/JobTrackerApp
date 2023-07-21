package com.cankatsezer.jobtrackerapp.registiration.token;

import com.cankatsezer.jobtrackerapp.user.User;

import java.util.Optional;

public interface IVerificationTokenService {
    String validateToken(String token);

    void saveVerificationTokenForUser(User user, String token);

    Optional<VerificationToken> findByVerificationToken(String token);

}
