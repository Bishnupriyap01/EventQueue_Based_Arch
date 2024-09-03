package com.bishnupriya.event_driven.service;

import com.bishnupriya.event_driven.Repository.EventRepository;
import com.bishnupriya.event_driven.config.ApiProperties;
import com.bishnupriya.event_driven.event.Event;
import com.bishnupriya.event_driven.event.EventStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.time.LocalDateTime;
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

    private final ApiProperties apiProperties;

    public ConsumerService(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
    }

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int BASE_RETRY_DELAY_SECONDS = 1;


    @Transactional
    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void processPendingEvents() {
        List<Event> events = fetchEventsByStatus(EventStatus.PENDING);

        for (Event event : events) {
            try {
                processEvent(event);

            } catch (Exception e) {
                System.err.println("Failed to process event: (1st time)" + e.getMessage());

                // Mark the event for retry if it fails
                event.setRetryCount(event.getRetryCount() + 1);
                event.setStatus(EventStatus.RETRY);
                //Mark the "retry at" time
                LocalDateTime nextRetryTimestamp = calculateNextRetryTimestamp(event.getRetryCount(), BASE_RETRY_DELAY_SECONDS);
                event.setRetryTimestamp(nextRetryTimestamp);
                event.setStatusMessage("Marked for retry. Next retry at: " + nextRetryTimestamp);

            } finally {
                if (event.getStatusMessage().length() > 255) {
                    event.setStatusMessage(event.getStatusMessage().substring(0, 255));
                }
                eventRepository.save(event);
            }
        }
    }


    @Transactional
    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void processRetriedEvents() throws Exception {
        EventStatus status = EventStatus.RETRY;
        List<Event> events = fetchEventsByStatus(status);

        LocalDateTime now = LocalDateTime.now();

        for (Event event : events) {
            LocalDateTime retryTimeToCompare = event.getRetryTimestamp().minusSeconds(BASE_RETRY_DELAY_SECONDS);

            if (now.isAfter(retryTimeToCompare)) {
                handleRetriedEvent(event);
            } else {
                System.out.println("Event ID " + event.getId() + " is not ready for retry yet.");
            }

        }
    }

    private void handleRetriedEvent(Event event) throws Exception {
        try {
            processEvent(event);

        } catch (Exception e) {
            System.err.println("Failed to process retried event with ID " + event.getId() + ": " + e.getMessage());

            int newRetryCount = event.getRetryCount() + 1;
            event.setRetryCount(newRetryCount);

            if (newRetryCount < MAX_RETRY_ATTEMPTS) {
                LocalDateTime nextRetryTimestamp = calculateNextRetryTimestamp(newRetryCount, BASE_RETRY_DELAY_SECONDS);
                event.setRetryTimestamp(nextRetryTimestamp);
                event.setStatusMessage("Marked for retry. Next retry at: " + nextRetryTimestamp);

            } else if (newRetryCount >= MAX_RETRY_ATTEMPTS) {
                processEventWithDifferentUrl(event);

            }
        } finally {
            if (event.getStatusMessage().length() > 255) {
                event.setStatusMessage(event.getStatusMessage().substring(0, 255));
            }
            eventRepository.save(event);
            System.out.println("Saved event with ID " + event.getId() + " and retry count " + event.getRetryCount());
        }
    }


    private void processEvent(Event event) throws Exception {
        String apiUrl = event.getApiUrl();
        ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            event.setStatus(EventStatus.PROCESSED);
            event.setStatusMessage("Successfully processed with response: " + response.getBody());
        } else {
            throw new Exception("API call failed with status: " + response.getStatusCode());
        }
    }

    private void processEventWithDifferentUrl(Event event) throws Exception {
        System.out.println("Processing event with different URL for event ID " + event.getId());

        // Set the retry URL for the event
        String retryApiUrl = apiProperties.getRetryApiUrl();
        event.setApiUrl(retryApiUrl); // Ensure the API URL is updated

        // Log the new API URL for debugging
        System.out.println("New API URL set to: " + retryApiUrl);

        // Perform the API call with the new URL
        ResponseEntity<String> response = restTemplate.getForEntity(retryApiUrl, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            event.setStatus(EventStatus.PROCESSED);
            event.setStatusMessage("Successfully processed on retry with response: " + response.getBody());
        } else {
            throw new Exception("Retry API call failed with status: " + response.getStatusCode());
        }
    }


    private List<Event> fetchEventsByStatus(EventStatus status) {
        Query query = entityManager.createQuery("SELECT e FROM Event e WHERE e.status = :status")
                .setParameter("status", status)
                .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return query.getResultList();
    }

    private LocalDateTime calculateNextRetryTimestamp(int retryCount, int baseRetryDelaySeconds) {
        long delay = (long) Math.pow(2, retryCount) * baseRetryDelaySeconds;
        return LocalDateTime.now().plusSeconds(delay);
    }

}
