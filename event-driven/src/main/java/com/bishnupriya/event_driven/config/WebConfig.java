package com.bishnupriya.event_driven.config;

import com.bishnupriya.event_driven.controller.interceptor.EventIdValidationInterceptor;
import com.bishnupriya.event_driven.controller.interceptor.EventStatusValidationInterceptor;
import com.bishnupriya.event_driven.controller.interceptor.PayloadValidationInterceptor;
import com.bishnupriya.event_driven.controller.interceptor.RequestLoggingInterceptor;
import com.bishnupriya.event_driven.controller.interceptor.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private PayloadValidationInterceptor payloadValidationInterceptor;

    @Autowired
    private EventIdValidationInterceptor eventIdValidationInterceptor;

    @Autowired
    private EventStatusValidationInterceptor eventStatusValidationInterceptor;

    @Autowired
    private RequestLoggingInterceptor requestLoggingInterceptor;

    @Autowired
    private ApiUrlInterceptor apiUrlInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(payloadValidationInterceptor)
                .addPathPatterns("/events/create");

        registry.addInterceptor(eventIdValidationInterceptor)
                .addPathPatterns("/events/*");

        registry.addInterceptor(eventStatusValidationInterceptor)
                .addPathPatterns("/events/*/status");

        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/events/**");  // Apply to all event-related endpoints

        registry.addInterceptor(apiUrlInterceptor)
                .addPathPatterns("/events/create"); // Apply to the specific endpoint

    }



}
