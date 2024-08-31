package com.bishnupriya.event_driven.controller.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PayloadValidationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
            throws Exception {

        String contentType = request.getHeader("Content-Type");

        if (request.getMethod().equalsIgnoreCase("POST") && request.getRequestURI().contains("/create")) {
            if (contentType == null || !contentType.equalsIgnoreCase("application/json")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Content-Type must be application/json");
                return false;
            }
        }

        if (request.getMethod().equalsIgnoreCase("PUT") && request.getRequestURI().matches("/events/\\d+/status")) {
            if (contentType == null || !contentType.equalsIgnoreCase("application/json")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Content-Type must be application/json");
                return false;
            }
        }

        // Proceed with other interceptors or the handler method
        return true;
    }
}
