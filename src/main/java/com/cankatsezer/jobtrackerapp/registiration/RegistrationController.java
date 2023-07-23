package com.cankatsezer.jobtrackerapp.registiration;

import com.cankatsezer.jobtrackerapp.event.RegistrationCompleteEvent;
import com.cankatsezer.jobtrackerapp.event.listener.RegistrationCompleteEventListener;
import com.cankatsezer.jobtrackerapp.registiration.password.IPasswordResetTokenService;
import com.cankatsezer.jobtrackerapp.registiration.token.VerificationToken;
import com.cankatsezer.jobtrackerapp.registiration.token.VerificationTokenService;
import com.cankatsezer.jobtrackerapp.user.IUserService;
import com.cankatsezer.jobtrackerapp.user.User;
import com.cankatsezer.jobtrackerapp.utility.UrlUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/registration")
public class RegistrationController {

    private final IUserService userService;
    private final ApplicationEventPublisher publisher;
    private final VerificationTokenService verificationTokenService;
    private final IPasswordResetTokenService iPasswordResetTokenService;
    private final RegistrationCompleteEventListener eventListener;

    @GetMapping("/registration-form")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new RegistrationRequest());
        return "registration";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") RegistrationRequest registration, HttpServletRequest request) {
        User user = userService.registerUser(registration);
        publisher.publishEvent(new RegistrationCompleteEvent(user, UrlUtil.getApplicationUrl(request)));
        return "redirect:/registration/registration-form?success";
    }

    @GetMapping("/verifyEmail")
    public String verifyEmail(@RequestParam("token") String token) {
        Optional<VerificationToken> theToken = verificationTokenService.findByVerificationToken(token);
        if (theToken.isPresent() && theToken.get().getUser().isEnabled()) {
            return "redirect:/login?verified";
        }
        String verificationResult = verificationTokenService.validateToken(token); //TODO HATALI GİBİ
        if (verificationResult.equalsIgnoreCase("invalid")) {
            return "redirect:/error?invalid";
        } else if (verificationResult.equalsIgnoreCase("expired")) {
            return "redirect:/error?expired";
        } else if (verificationResult.equalsIgnoreCase("valid")) {
            return "redirect:/login?valid";
        }
        return "redirect:/error?invalid";
    }

    @GetMapping("/forgot-password-request")
    public String forgotPassword() {
        return "forgot-password-form";
    }

    @PostMapping("/forgot-password")
    public String resetPasswordRequest(HttpServletRequest request, Model model) {
        String email = request.getParameter("email");
        User user = userService.findByEmail(email);
        if (user == null) {
            return "redirect:/registration/forgot-password-request?not_found";
        }
        String passwordResetToken = UUID.randomUUID().toString();
        iPasswordResetTokenService.createPasswordResetTokenForUser(user, passwordResetToken);
        String url = UrlUtil.getApplicationUrl(request) + "/registration/password-reset-form?token=" + passwordResetToken;
        try {
            eventListener.sendPasswordResetVerificationEmail(url);
        } catch (MessagingException | UnsupportedEncodingException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/registration/forgot-password-request?success";
    }

    @GetMapping("/password-reset-form")
    public String passwordResetForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "password-reset-form";
    }

    @PostMapping("/reset-password")
    public String resetPassword(HttpServletRequest request) {
        String theToken = request.getParameter("token");
        String password = request.getParameter("password");
        String tokenVerificationResult = iPasswordResetTokenService.validatePasswordResetToken(theToken);
        if (tokenVerificationResult.equalsIgnoreCase("valid")) {
            return "redirect:/error?invalid_token";
        }
        Optional<User> theUser = iPasswordResetTokenService.findUserByPasswordResetToken(theToken);
        if (theUser.isPresent()) {
            iPasswordResetTokenService.resetPassword(theUser.get(), password);
            return "redirect:/login?reset_success";
        }
        return "redirect:/error?not_found";
    }
}


