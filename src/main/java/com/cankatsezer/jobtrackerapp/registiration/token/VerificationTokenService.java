package com.cankatsezer.jobtrackerapp.registiration.token;

import com.cankatsezer.jobtrackerapp.user.User;
import com.cankatsezer.jobtrackerapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationTokenService implements IVerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;

    @Override
    public String validateToken(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        if (verificationToken.isEmpty()) {
            return "invalid verification token.";
        }
        User user = verificationToken.get().getUser();
        Calendar calendar = Calendar.getInstance();
        if (verificationToken.get().getExpirationTime().getTime() - calendar.getTime().getTime() <= 0) {
            return "expired";
        }
        user.setEnabled(true);
        userRepository.save(user);
        return "valid";
    }

    @Override
    public void saveVerificationTokenForUser(User user, String token) {
        var verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public Optional<VerificationToken> findByVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }
}
