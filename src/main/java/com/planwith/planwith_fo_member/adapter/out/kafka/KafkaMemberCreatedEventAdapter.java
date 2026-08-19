package com.planwith.planwith_fo_member.adapter.out.kafka;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_member.adapter.out.kafka.dto.MemberCreatedEventPayload;
import com.planwith.planwith_fo_member.application.event.MemberCreatedEvent;
import com.planwith.planwith_fo_member.application.port.out.MemberCreatedEventPort;
import com.planwith.planwith_fo_member.config.MemberKafkaProperties;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class KafkaMemberCreatedEventAdapter implements MemberCreatedEventPort {

	private static final Logger log = LoggerFactory.getLogger(KafkaMemberCreatedEventAdapter.class);

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final String topic;

	public KafkaMemberCreatedEventAdapter(
			KafkaTemplate<String, String> kafkaTemplate,
			ObjectMapper objectMapper,
			MemberKafkaProperties properties
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
		this.topic = requireTopic(properties.memberCreatedTopic());
	}

	@Override
	public void publish(MemberCreatedEvent event) {
		String payload;
		try {
			payload = objectMapper.writeValueAsString(new MemberCreatedEventPayload(
					event.eventUuid(),
					event.eventType(),
					event.memberUuid()
			));
		} catch (JsonProcessingException exception) {
			log.error("KafkaMemberCreatedEventAdapter : publish : 회원 생성 이벤트 직렬화 실패 - memberUuid={}",
					event.memberUuid());
			return;
		}

		log.info("KafkaMemberCreatedEventAdapter : publish : 회원 생성 이벤트 Kafka 발행 시작 - topic={}, eventUuid={}, memberUuid={}",
				topic, event.eventUuid(), event.memberUuid());
		try {
			CompletableFuture<?> sendResult = kafkaTemplate.send(topic, event.memberUuid(), payload);
			sendResult.whenComplete((result, exception) -> {
				if (exception != null) {
					log.error("KafkaMemberCreatedEventAdapter : publish : 회원 생성 이벤트 Kafka 발행 실패 - topic={}, memberUuid={}, eventUuid={}",
							topic, event.memberUuid(), event.eventUuid());
					return;
				}
				log.info("KafkaMemberCreatedEventAdapter : publish : 회원 생성 이벤트 Kafka 발행 완료 - topic={}, memberUuid={}, eventUuid={}",
						topic, event.memberUuid(), event.eventUuid());
			});
		} catch (RuntimeException exception) {
			log.error("KafkaMemberCreatedEventAdapter : publish : 회원 생성 이벤트 Kafka 발행 중 예외 - topic={}, memberUuid={}, eventUuid={}",
					topic, event.memberUuid(), event.eventUuid());
		}
	}

	private static String requireTopic(String topic) {
		if (topic == null || topic.isBlank()) {
			throw new IllegalArgumentException("Member created Kafka topic is required.");
		}
		return topic;
	}
}
