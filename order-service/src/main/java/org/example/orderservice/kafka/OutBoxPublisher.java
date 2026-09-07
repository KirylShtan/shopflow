package org.example.orderservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.orderservice.event.OrderCreatedEvent;
import org.example.orderservice.model.OutBoxMessage;
import org.example.orderservice.repository.OutBoxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutBoxPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OutBoxPublisher.class);

    private final OutBoxRepository outBoxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutBoxPublisher(OutBoxRepository outBoxRepository,
                           KafkaTemplate<String, Object> kafkaTemplate
            , ObjectMapper objectMapper) {
        this.outBoxRepository = outBoxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPending(){
        List<OutBoxMessage> pending = outBoxRepository.findBySentFalseOrderByIdAsc();
        for (OutBoxMessage message : pending) {
            try{
                OrderCreatedEvent event = objectMapper.readValue(
                        message.getPayload(), OrderCreatedEvent.class);
                kafkaTemplate.send(message.getTopic(), message.getKey(), event);
                message.setSent(true);
                logger.info("Outbox sent: id={}, topic={}, key={}",
                        message.getId(), message.getTopic(), message.getKey());
            } catch (Exception e){
                logger.warn("Outbox send failed id={}: {}", message.getId(), e.getMessage());
            }
        }
    }
}
