package com.grydtech.msstack.transport.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CaseFormat;
import com.grydtech.msstack.core.Event;
import com.grydtech.msstack.core.MessageBrokerAdapter;
import com.grydtech.msstack.core.EventHandler;

public class KafkaBroker implements MessageBrokerAdapter {

	private static KafkaSender kafkaSender;

	private static ObjectMapper objectMapper;

	static {
		kafkaSender = new KafkaSender();
		objectMapper = new ObjectMapper();
	}

	public void publish(Event event) {
		String message = null;
		String topic = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, event.getClass().getSimpleName());
		try {
			message = objectMapper.writeValueAsString(event);
		}
		catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		kafkaSender.send(topic, message);
	}

	public void registerHandler(Class<? extends EventHandler> aClass) {
		throw new UnsupportedOperationException();
	}

	public void unregisterHandler(Class<? extends EventHandler> aClass) {
		throw new UnsupportedOperationException();
	}
}
