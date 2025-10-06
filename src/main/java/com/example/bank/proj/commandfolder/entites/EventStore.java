package com.example.bank.proj.commandfolder.entites;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "event_store")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String aggregateType;   // e.g., "Account", "User"

    @Column(nullable = false)
    private String aggregateId;     // e.g., accountId or userId

    @Column(nullable = false)
    private String eventType;       // e.g., "AccountCreatedEvent"

    @Lob
    @Column(nullable = false)
    private String eventData;       // JSON payload of the event

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
