package com.mateusnavarro77.projeto_cybersec_taskmanager.web;

import com.mateusnavarro77.projeto_cybersec_taskmanager.security.SecurityFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieService {

    private static final Duration AUTH_COOKIE_AGE = Duration.ofHours(2);

    public void addAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(SecurityFilter.AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(AUTH_COOKIE_AGE)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearAuthCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(SecurityFilter.AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
