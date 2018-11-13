package com.grydtech.msstack.transport.kafka;

import com.grydtech.msstack.annotation.FrameworkComponent;
import com.grydtech.msstack.core.connectors.messagebus.MessageBusConnector;
import com.grydtech.msstack.core.services.EventsConsumer;
import com.grydtech.msstack.core.types.Entity;
import com.grydtech.msstack.core.types.messaging.Message;
import com.grydtech.msstack.transport.kafka.services.KafkaConsumerService;
import com.grydtech.msstack.transport.kafka.services.KafkaProducerService;
import com.grydtech.msstack.util.EntityUtils;
import com.grydtech.msstack.util.JsonConverter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

@SuppressWarnings("unused")
@FrameworkComponent
public final class KafkaMessageBusConnector extends MessageBusConnector {

    private static final Logger LOGGER = Logger.getLogger(KafkaMessageBusConnector.class.getName());

    private final KafkaProducerService kafkaProducerService;
    private final KafkaConsumerService kafkaConsumerService;

    private final Map<String, Consumer<String>> consumers = new HashMap<>();

    public KafkaMessageBusConnector() {
        this.kafkaProducerService = new KafkaProducerService();
        this.kafkaConsumerService = new KafkaConsumerService();
    }

    public final void attach(Class<? extends Entity> entityClass, EventsConsumer consumer) {
        consumers.put(EntityUtils.getTopic(entityClass), consumer);
        LOGGER.info(String.format("[TOPIC][%s] -> %s | attached", EntityUtils.getTopic(entityClass), consumer.getClass().getSimpleName()));
    }

    public final void detach(Class<? extends Entity> entityClass) {
        consumers.remove(EntityUtils.getTopic(entityClass));
        LOGGER.info(String.format("[TOPIC][%s] | detached", EntityUtils.getTopic(entityClass)));
    }

    @Override
    public void push(Message message) {
        String topic = message.getTopic();
        String eventName = message.getClass().getSimpleName();
        String eventData = JsonConverter.toJsonString(message.getPayload()).orElseThrow(RuntimeException::new);
        this.kafkaProducerService.publish(topic, 0, eventName, eventData);
    }

    @Override
    public void connect() throws IOException {
        LOGGER.info("Starting KafkaMessageBusConnector");
        this.kafkaConsumerService.setConsumers(this.consumers);
        this.kafkaProducerService.start();
        this.kafkaConsumerService.start();
        LOGGER.info("KafkaMessageBusConnector Started");
    }

    @Override
    public void disconnect() throws IOException {
        this.kafkaProducerService.flush();
    }
}
