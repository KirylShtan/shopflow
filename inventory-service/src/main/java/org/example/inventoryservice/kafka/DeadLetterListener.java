package org.example.inventoryservice.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterListener {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterListener.class);

    @KafkaListener(
            topics = "order.created-dlt",
            properties = {
                    "value.deserializer.apache.kafka.common.serialization.StringDeserializer",
                    "spring.deserializer.value.delegate.class=org.apache.kafka.common.serialization.StringDeserializer",
                    "spring.json.use.type.headers=false"
            }
    )
    public void onOrderCreatedDlt(ConsumerRecord<String, String> record) {
        logger.error("DLT order.created: key={}, offset={},value={}",
                record.key(), record.offset(), record.value());
    }
}
