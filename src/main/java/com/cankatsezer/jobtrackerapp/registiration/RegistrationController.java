package com.cankatsezer.jobtrackerapp.registiration;

import com.cankatsezer.jobtrackerapp.event.RegistrationCompleteEvent;
import com.cankatsezer.jobtrackerapp.registiration.token.VerificationToken;
import com.cankatsezer.jobtrackerapp.registiration.token.VerificationTokenService;
import com.cankatsezer.jobtrackerapp.user.IUserService;
import com.cankatsezer.jobtrackerapp.user.User;
import com.cankatsezer.jobtrackerapp.utility.UrlUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/registration")
public class RegistrationController {

    private final IUserService userService;
    private final ApplicationEventPublisher publisher;
    private final VerificationTokenService verificationTokenService;

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
}
