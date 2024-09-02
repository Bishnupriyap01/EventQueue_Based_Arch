package com.bishnupriya.event_driven.controller.interceptor;

import com.bishnupriya.event_driven.event.EventStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class EventStatusValidationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if ("PUT".equalsIgnoreCase(request.getMethod()) && request.getRequestURI().contains("/status")) {
            String status = request.getReader().lines().reduce("", String::concat);
            if (!Arrays.stream(EventStatus.values()).anyMatch(e -> e.name().equals(status))) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid event status");
                return false;
            }
        }
        return true;
    }
}
