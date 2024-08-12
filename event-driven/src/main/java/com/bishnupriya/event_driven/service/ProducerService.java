package com.bishnupriya.event_driven.service;

import com.bishnupriya.event_driven.Repository.EventRepository;
import com.bishnupriya.event_driven.event.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class ProducerService {
    @Autowired
    private EventRepository eventRepository;

    @Transactional
    public Event createEvent(String payload) {
        Event event = new Event();
        event.setPayload(payload);
        event.setStatus(EventStatus.PENDING);
        event.setTimestamp(LocalDateTime.now());
        return eventRepository.save(event);
    }
}
