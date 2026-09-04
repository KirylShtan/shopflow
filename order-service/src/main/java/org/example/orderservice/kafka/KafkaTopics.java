package org.example.orderservice.kafka;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class KafkaTopics {
    public static final String ORDER_CREATED = "order.created";
    public static final String INVENTORY_RESERVED = "inventory.reserved";
    public static final String INVENTORY_FAILED = "inventory.failed";
    public static final String ORDER_CONFIRMED = "order.confirmed";
    public static final String ORDER_CANCELLED = "order.cancelled";


}
