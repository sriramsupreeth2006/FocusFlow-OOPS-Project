package com.focusflow.admin.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminTokenFilter implements Filter {

    @Value("${app.admin.token}")
    private String adminToken;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // Allow CORS preflight and public endpoints
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())
                || req.getRequestURI().startsWith("/h2-console")
                || req.getRequestURI().startsWith("/ws")) {
            chain.doFilter(request, response);
            return;
        }

        String token = req.getHeader("x-admin-token");
        if (token == null || !token.equals(adminToken)) {
            res.setStatus(401);
            res.getWriter().write("Unauthorized");
            return;
        }
        chain.doFilter(request, response);
    }
}
