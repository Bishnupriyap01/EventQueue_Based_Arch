package com.bishnupriya.event_driven.service;

import com.bishnupriya.event_driven.Repository.EventRepository;
import com.bishnupriya.event_driven.event.Event;
import com.bishnupriya.event_driven.event.EventStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.util.List;

@Service
public class ConsumerService {

    @Autowired
    private EventRepository eventRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Scheduled(fixedRate = 5000) // Poll every 5 seconds
    public void processPendingEvents() {
        List<Event> events = fetchPendingEvents();
        for (Event event : events) {
            try {
                // Process the event (e.g., print payload)
                System.out.println("Processing event: " + event.getPayload());
                // Update event status
                event.setStatus(EventStatus.PROCESSED);
                eventRepository.save(event);
            } catch (Exception e) {
                // Handle failure
                event.setStatus(EventStatus.FAILED);
                eventRepository.save(event);
            }
        }
    }

    private List<Event> fetchPendingEvents() {
        // Lock rows for update to prevent concurrent processing
        Query query = entityManager.createQuery("SELECT e FROM Event e WHERE e.status = :status")
                .setParameter("status", EventStatus.PENDING)
                .setLockMode(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        return query.getResultList();
    }
}

//@Service
//public class ConsumerService {
//
//    @Autowired
//    private EventRepository eventRepository;
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
//    @Scheduled(fixedRate = 5000) // Poll every 5 seconds
//    @Transactional
//    public void processEvents() {
//        // Lock rows to process one at a time
//        Query query = entityManager.createNativeQuery("SELECT * FROM event WHERE status = 'PENDING' FOR UPDATE");
//        List<Event> events = query.getResultList();
//
//        for (Event event : events) {
//            try {
//                // Process event
//                System.out.println("Processing event: " + event.getPayload());
//                event.setStatus(EventStatus.PROCESSED);
//                eventRepository.save(event);
//            } catch (Exception e) {
//                event.setStatus(EventStatus.FAILED);
//                eventRepository.save(event);
//                e.printStackTrace();
//            }
//        }
//    }
//}
