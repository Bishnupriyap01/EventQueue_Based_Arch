package com.bishnupriya.event_driven.controller.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiUrlInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiUrl = request.getParameter("apiUrl");

        if (apiUrl == null || apiUrl.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameter: apiUrl");
            return false; // Prevents the request from reaching the controller
        }

        return true; // Allows the request to proceed
    }
}


