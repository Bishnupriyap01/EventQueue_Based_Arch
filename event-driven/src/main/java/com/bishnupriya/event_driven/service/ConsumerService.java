package com.bishnupriya.event_driven.service;

import com.bishnupriya.event_driven.Repository.EventRepository;
import com.bishnupriya.event_driven.event.Event;
import com.bishnupriya.event_driven.event.EventStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ConsumerService {

    @Autowired
    private EventRepository eventRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.retryApiUrl}")
    private String retryApiUrl;

    @Transactional
    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.SECONDS)
    public void processPendingEvents() throws Exception {
        List<Event> events = fetchPendingEvents();

        for (Event event : events) {
            try {
                // Try processing the event
                processEvent(event);

            } catch (Exception e) {
                // Handle failure
                System.err.println("Failed to process event: " + e.getMessage());

                if (event.getStatus() == EventStatus.PENDING) {
                    // Mark the event for retry if it's the first failure
                    event.setStatus(EventStatus.RETRY);
                    event.setStatusMessage("Marked for retry after failure: " + e.getMessage());
                } else if (event.getStatus() == EventStatus.RETRY) {
                    // On retry, attempt with a different URL or strategy
                    processEventWithDifferentUrl(event);
                }
            } finally {
                // Ensure status message length is within limits
                if (event.getStatusMessage().length() > 255) {
                    event.setStatusMessage(event.getStatusMessage().substring(0, 255));
                }
                eventRepository.save(event);
            }
        }
    }

    private void processEvent(Event event) throws Exception {
        String apiUrl = event.getApiUrl();
        ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Processing event: " + event.getPayload());
            event.setStatus(EventStatus.PROCESSED);
            event.setStatusMessage("Successfully processed with response: " + response.getBody());
        } else {
            throw new Exception("API call failed with status: " + response.getStatusCode());
        }
    }

    private void processEventWithDifferentUrl(Event event) throws Exception {
        // Here, provided a different URL for retry logic which will succeed


        ResponseEntity<String> response = restTemplate.getForEntity(retryApiUrl, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Retrying event: " + event.getPayload());
            event.setStatus(EventStatus.PROCESSED);
            event.setStatusMessage("Successfully processed on retry with response: " + response.getBody());
        } else {
            throw new Exception("Retry API call failed with status: " + response.getStatusCode());
        }
    }

//    public void processPendingEvent(Long eventId) throws Exception {
//        List<Event> events;
//
//        if (eventId != null) {
//            events = fetchEventById(eventId);
//        } else {
//            events = fetchPendingEvents();
//        }
//
//        for (Event event : events) {
//            processPendingEvents();
//        }
//    }

    private List<Event> fetchPendingEvents() {
        Query query = entityManager.createQuery("SELECT e FROM Event e WHERE e.status IN (:statuses)")
                .setParameter("statuses", List.of(EventStatus.PENDING, EventStatus.RETRY))
                .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return query.getResultList();
    }

    private List<Event> fetchEventById(Long eventId) {
        Query query = entityManager.createQuery("SELECT e FROM Event e WHERE e.id = :id")
                .setParameter("id", eventId)
                .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return query.getResultList();
    }
}
