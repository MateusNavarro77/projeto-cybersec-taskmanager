package com.mateusnavarro77.projeto_cybersec_taskmanager.web;

import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.LoginRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.dto.RegisterRequestDTO;
import com.mateusnavarro77.projeto_cybersec_taskmanager.entity.User;
import com.mateusnavarro77.projeto_cybersec_taskmanager.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/app")
public class AppAuthController {

    private final AuthService authService;
    private final AuthCookieService authCookieService;

    public AppAuthController(AuthService authService, AuthCookieService authCookieService) {
        this.authService = authService;
        this.authCookieService = authCookieService;
    }

    @GetMapping
    public String index(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("username", user.getRealUsername());
        return "app/home";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequestDTO("", ""));
        }
        return "app/auth/login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginRequest") LoginRequestDTO loginRequest,
            BindingResult bindingResult,
            HttpServletResponse response,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "app/auth/login";
        }

        try {
            var authResponse = authService.login(loginRequest);
            authCookieService.addAuthCookie(response, authResponse.token());
            return "redirect:/app";
        } catch (AuthenticationException exception) {
            model.addAttribute("loginError", "Invalid email or password.");
            return "app/auth/login";
        }
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequestDTO("", "", ""));
        }
        return "app/auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequestDTO registerRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "app/auth/register";
        }

        try {
            authService.register(registerRequest);
            redirectAttributes.addFlashAttribute("registrationSuccess", "Account created. Sign in to continue.");
            return "redirect:/app/login";
        } catch (ResponseStatusException exception) {
            model.addAttribute("registerError", exception.getReason());
            return "app/auth/register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        authCookieService.clearAuthCookie(response);
        return "redirect:/app/login";
    }
}
