package com.bishnupriya.event_driven.event;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String payload;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private String apiUrl;

    @Column(name = "status_message", columnDefinition = "TEXT")
    private String statusMessage;

    @Setter
    @Getter
    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column
    private LocalDateTime retryTimestamp;

    private LocalDateTime timestamp;

}
