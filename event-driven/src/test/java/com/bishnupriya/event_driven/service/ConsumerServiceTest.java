package com.bishnupriya.event_driven.service;

import com.bishnupriya.event_driven.Repository.EventRepository;
import com.bishnupriya.event_driven.event.Event;
import com.bishnupriya.event_driven.event.EventStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ConsumerServiceTest {

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private EventRepository eventRepository;

    @Test
    @Transactional
    public void testEventProcessing() {
        // Arrange
        Event event1 = new Event();
        event1.setPayload("Test Event 1");
        event1.setStatus(EventStatus.PENDING);
        event1.setTimestamp(LocalDateTime.now());
        event1.setApiUrl("https://reqres.in/api/users?page=2");
        eventRepository.save(event1);

        Event event2 = new Event();
        event2.setPayload("Test Event 2");
        event2.setStatus(EventStatus.PENDING);
        event2.setTimestamp(LocalDateTime.now());
        eventRepository.save(event2);

        // Act
       //consumerService.processPendingEvents();

        // Assert
        List<Event> events = eventRepository.findAll();
        assertEquals(2, events.size());
        assertEquals(EventStatus.PROCESSED, events.get(0).getStatus());
        assertEquals(EventStatus.PROCESSED, events.get(1).getStatus());
    }
}