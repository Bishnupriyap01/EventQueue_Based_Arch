package com.bishnupriya.event_driven.controller;

import com.bishnupriya.event_driven.event.Event;
import com.bishnupriya.event_driven.service.ConsumerService;
import com.bishnupriya.event_driven.service.ProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private ProducerService producerService;

    @Autowired
    private ConsumerService consumerService; // Injecting ConsumerService

    @PostMapping("/create")
    public ResponseEntity<?> createEvent( @RequestParam String apiUrl, @RequestBody(required = false) Map<String, Object> requestBody ) {
        if (requestBody == null || !requestBody.containsKey("payload") ) {
            return ResponseEntity.badRequest().body("Missing required parameter: payload");
        }
        String payload = (String) requestBody.get("payload");

        Event event = producerService.createEvent(payload, apiUrl);
        return ResponseEntity.ok(event);
    }
    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        // Process all pending events
        // consumerService.processPendingEvents(null);

        List<Event> events = producerService.getAllEvents();
        return new ResponseEntity<>(events, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        // Process the specific event before retrieving it
        Event event = producerService.getEventById(id);
        if (event != null) {
            return new ResponseEntity<>(event, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteEventById(@PathVariable Long id) {
        try {
            boolean isDeleted = producerService.deleteEventById(id);
            if (isDeleted) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}













//package com.bishnupriya.event_driven.controller;
//
//import com.bishnupriya.event_driven.event.Event;
//import com.bishnupriya.event_driven.event.EventStatus;
//import com.bishnupriya.event_driven.service.ProducerService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/events")
//public class EventController {
//
//    @Autowired
//    private ProducerService producerService;
//
//
//    @PostMapping("/create")
//    public ResponseEntity<?> createEvent(@RequestBody(required = false) Map<String, Object> requestBody) {
//        if (requestBody == null || !requestBody.containsKey("someParam")) {
//            return ResponseEntity.badRequest().body("Missing required parameter: someParam");
//        }
//
//        String payload = (String) requestBody.get("payload");
//        Event event = producerService.createEvent(payload);
//        return ResponseEntity.ok(event);
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
//        Event event = producerService.getEventById(id);
//        if (event != null) {
//            return new ResponseEntity<>(event, HttpStatus.OK);
//        } else {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Event>> getAllEvents() {
//        List<Event> events = producerService.getAllEvents();
//        return new ResponseEntity<>(events, HttpStatus.OK);
//    }
//
//
//
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<HttpStatus> deleteEventById(@PathVariable Long id) {
//        try {
//            boolean isDeleted = producerService.deleteEventById(id);
//            if (isDeleted) {
//                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//            } else {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//}
