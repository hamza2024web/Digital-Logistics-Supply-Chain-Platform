package com.spring.digital_logistics.config;

import com.spring.digital_logistics.security.service.UserDetailsImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LogMdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Récupérer l'authentification actuelle
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

                // Injecter les infos dans les logs
                MDC.put("user_email", userDetails.getEmail());
                MDC.put("user_id", String.valueOf(userDetails.getId()));

                // Récupérer le premier rôle
                String role = userDetails.getAuthorities().stream()
                        .findFirst().map(Object::toString).orElse("UNKNOWN");
                MDC.put("user_role", role);
            }

            // Injecter l'endpoint et la méthode
            MDC.put("http_method", request.getMethod());
            MDC.put("http_uri", request.getRequestURI());

            filterChain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }
}