package com.bishnupriya.event_driven.controller.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class EventIdValidationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        String path = request.getRequestURI();
        if (path.matches("/events/\\d+.*")) {  // Check if URL contains an event ID
            String idStr = path.split("/")[2];  // Extract the ID from the path
            try {
                Long id = Long.parseLong(idStr);
                if (id <= 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("Invalid event ID");
                    return false;
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid event ID format");
                return false;
            }
        }
        return true;
    }
}
