package com.bishnupriya.event_driven.service;

import com.bishnupriya.event_driven.Repository.EventRepository;
import com.bishnupriya.event_driven.event.Event;
import com.bishnupriya.event_driven.event.EventStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProducerService {

    @Autowired
    private EventRepository eventRepository;

    @Transactional
    public Event createEvent(String payload, String apiUrl) {
        Event event = new Event();
        event.setPayload(payload);
        event.setApiUrl(apiUrl);
        event.setStatusMessage("");
        event.setStatus(EventStatus.PENDING);
        event.setTimestamp(LocalDateTime.now());
        return eventRepository.save(event);
    }

    public Event getEventById(Long id) {
        Optional<Event> event = eventRepository.findById(id);
        return event.orElse(null);
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }



    @Transactional
    public boolean deleteEventById(Long id) {
        if (eventRepository.existsById(id)) {
            eventRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
