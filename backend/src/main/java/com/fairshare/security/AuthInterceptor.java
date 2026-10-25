package com.fairshare.security;

import com.fairshare.entity.UserSessionEntity;
import com.fairshare.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserSessionRepository userSessionRepository;

    public AuthInterceptor(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7).trim();
        } else {
            token = request.getHeader("X-Auth-Token");
        }

        if (token != null && !token.isBlank()) {
            Optional<UserSessionEntity> sessionOpt = userSessionRepository.findByToken(token);
            if (sessionOpt.isPresent()) {
                UserSessionEntity session = sessionOpt.get();
                if (session.getExpiresAt().isAfter(Instant.now())) {
                    UserContext.setCurrentUser(session.getUser());
                    request.setAttribute("currentUser", session.getUser());
                } else {
                    userSessionRepository.delete(session);
                }
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
