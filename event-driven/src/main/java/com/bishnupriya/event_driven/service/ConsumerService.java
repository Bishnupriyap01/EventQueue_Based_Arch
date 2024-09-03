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
    @Scheduled(fixedDelay = 15, timeUnit = TimeUnit.SECONDS)
    public void processPendingEvents() {
        List<Event> events = fetchEventsByStatus(EventStatus.PENDING);
        for (Event event : events) {
            try {
                processEvent(event, false);
            } catch (Exception e) {
                handleFailedEvent(event, e);
            } finally {
                finalizeEventProcessing(event);
            }
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 15, timeUnit = TimeUnit.SECONDS)
    public void processRetriedEvents() {
        LocalDateTime now = LocalDateTime.now();
        int timeWindowSeconds = 60;

        List<Event> events = fetchReadyToRetryEvents(now, timeWindowSeconds);
        for (Event event : events) {
//            if : to avoid processing an event even a few seconds(1s) before its intended retry time.
//            ensuring events are processed only when they are truly ready, even accounting for small timing differences.
            if (now.isAfter(event.getRetryTimestamp().minusSeconds(BASE_RETRY_DELAY_SECONDS))) {
                try {
                    processEvent(event, true);
                } catch (Exception e) {
                    handleFailedEvent(event, e);
                } finally {
                    finalizeEventProcessing(event);
                }
            } else {
                System.out.println("Event ID " + event.getId() + " is not ready for retry yet.");
            }
        }
    }

    private void processEvent(Event event, boolean isRetry) throws Exception {
        String apiUrl = event.getApiUrl();
        if (isRetry && event.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
            apiUrl = apiProperties.getRetryApiUrl();
            event.setApiUrl(apiUrl); // Update the event with the retry URL
        }

        ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
        event.setStatus(EventStatus.PROCESSED);
        event.setStatusMessage("Successfully processed with response: " + response.getBody());
    }

    private void handleFailedEvent(Event event, Exception e) {
        System.err.println("Failed to process event with ID " + event.getId() + ": " + e.getMessage());

        int newRetryCount = event.getRetryCount() + 1;
        event.setRetryCount(newRetryCount);

        if (newRetryCount < MAX_RETRY_ATTEMPTS) {
            LocalDateTime nextRetryTimestamp = calculateNextRetryTimestamp(newRetryCount);
            event.setRetryTimestamp(nextRetryTimestamp);
            event.setStatus(EventStatus.RETRY);
            event.setStatusMessage("Marked for retry. Next retry at: " + nextRetryTimestamp);
        }
//        if (newRetryCount >= MAX_RETRY_ATTEMPTS) {
//            event.setStatusMessage("Failed after all retries: " + e.getMessage());
//        }
    }

    private void finalizeEventProcessing(Event event) {
        if (event.getStatusMessage().length() > 255) {
            event.setStatusMessage(event.getStatusMessage().substring(0, 255));
        }
        eventRepository.save(event);
        System.out.println("Saved event with ID " + event.getId() + " and retry count " + event.getRetryCount());
    }

    private List<Event> fetchEventsByStatus(EventStatus status) {
        Query query = entityManager.createQuery("SELECT e FROM Event e WHERE e.status = :status")
                .setParameter("status", status)
                .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return query.getResultList();
    }

    private List<Event> fetchReadyToRetryEvents(LocalDateTime now, int timeWindowSeconds) {
        LocalDateTime latestRetryTime = now.plusSeconds(timeWindowSeconds);

        Query query = entityManager.createQuery(
                        "SELECT e FROM Event e WHERE e.status = :status " +
                                "AND (e.retryTimestamp <= :now " +
                                "OR (e.retryTimestamp BETWEEN :now AND :latestRetryTime))")
                .setParameter("status", EventStatus.RETRY)
                .setParameter("now", now)
                .setParameter("latestRetryTime", latestRetryTime)
                .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);

        return query.getResultList();
    }

    private LocalDateTime calculateNextRetryTimestamp(int retryCount) {
        long delay = (long) Math.pow(4, retryCount) * BASE_RETRY_DELAY_SECONDS;
        return LocalDateTime.now().plusSeconds(delay);
    }
}
