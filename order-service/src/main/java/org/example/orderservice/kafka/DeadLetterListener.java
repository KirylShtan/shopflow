package org.example.orderservice.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterListener {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterListener.class);

    @KafkaListener(topics = {"inventory.reserved.DLT", "inventory.failed.DLT"},
    properties = {
            "value.deserializer=org.apache.kafka.common.serialization.StringDeserializer",
            "spring.deserializer.value.delegate.class=org.apache.kafka.common.serialization.StringDeserializer",
            "spring.json.use.type.headers=false"
          }
    )
    public void onInventoryDlt(ConsumerRecord<String, String> record) {
        logger.error("DLT {}: key={}, offset={}, value={}",
                record.topic(), record.key(), record.offset(), record.value());
    }
}
