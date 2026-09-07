package org.example.orderservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "outbox")
@Getter
@Setter
@NoArgsConstructor
public class OutBoxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Column(name = "message_key", nullable = false)
    private String key;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private boolean sent = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public OutBoxMessage(String topic, String key, String payload) {
        this.topic = topic;
        this.key = key;
        this.payload = payload;
    }
}
