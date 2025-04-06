package com.example.vdkfamilyserver.Config;

import com.example.vdkfamilyserver.Services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.Logger;

import java.io.IOException;

@Component
@RequiredArgsConstructor // Генерирует конструктор автоматически
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService; // Lombok создаст конструктор с этим полем

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = jwtService.resolveToken(request);
            logger.debug("Resolved token: {}", token);

            if (token != null && jwtService.validateToken(token)) {
                var auth = jwtService.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            logger.error("Authentication failed", e);
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authentication failed");
            return;
        }
        filterChain.doFilter(request, response);
    }
}