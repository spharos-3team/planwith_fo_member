package com.planwith.planwith_fo_member.adapter.out.kafka;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_member.adapter.out.kafka.dto.FollowEventPayload;
import com.planwith.planwith_fo_member.application.event.FollowChangedEvent;
import com.planwith.planwith_fo_member.application.port.out.FollowEventPort;
import com.planwith.planwith_fo_member.config.MemberKafkaProperties;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "enabled", havingValue = "true")
public class KafkaFollowEventAdapter implements FollowEventPort {

	private static final Logger log = LoggerFactory.getLogger(KafkaFollowEventAdapter.class);

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;
	private final String followCreatedTopic;
	private final String followRemovedTopic;

	public KafkaFollowEventAdapter(
			KafkaTemplate<String, String> kafkaTemplate,
			ObjectMapper objectMapper,
			MemberKafkaProperties properties
	) {
		this.kafkaTemplate = kafkaTemplate;
		this.objectMapper = objectMapper;
		this.followCreatedTopic = requireTopic(properties.followCreatedTopic(), "Follow created");
		this.followRemovedTopic = requireTopic(properties.followRemovedTopic(), "Follow removed");
	}

	@Override
	public void publishCreated(FollowChangedEvent event) {
		send(followCreatedTopic, event);
	}

	@Override
	public void publishRemoved(FollowChangedEvent event) {
		send(followRemovedTopic, event);
	}

	private void send(String topic, FollowChangedEvent event) {
		String payload;
		try {
			payload = objectMapper.writeValueAsString(new FollowEventPayload(
					event.eventUuid(),
					event.followerUuid(),
					event.followeeUuid(),
					event.occurredAt(),
					event.sourceVersion()
			));
		} catch (JsonProcessingException exception) {
			log.error(
					"KafkaFollowEventAdapter : send : 팔로우 이벤트 직렬화 실패 - eventType={}, followeeUuid={}",
					event.eventType(),
					event.followeeUuid()
			);
			return;
		}

		log.info(
				"KafkaFollowEventAdapter : send : 팔로우 이벤트 Kafka 발행 시작 - topic={}, eventType={}, followeeUuid={}, eventUuid={}, sourceVersion={}",
				topic,
				event.eventType(),
				event.followeeUuid(),
				event.eventUuid(),
				event.sourceVersion()
		);
		try {
			CompletableFuture<?> sendResult = kafkaTemplate.send(topic, event.followeeUuid(), payload);
			sendResult.whenComplete((result, exception) -> {
				if (exception != null) {
					log.error(
							"KafkaFollowEventAdapter : send : 팔로우 이벤트 Kafka 발행 실패 - topic={}, followeeUuid={}, eventUuid={}",
							topic,
							event.followeeUuid(),
							event.eventUuid()
					);
					return;
				}
				log.info(
						"KafkaFollowEventAdapter : send : 팔로우 이벤트 Kafka 발행 완료 - topic={}, followeeUuid={}, eventUuid={}",
						topic,
						event.followeeUuid(),
						event.eventUuid()
				);
			});
		} catch (RuntimeException exception) {
			log.error(
					"KafkaFollowEventAdapter : send : 팔로우 이벤트 Kafka 발행 중 예외 - topic={}, followeeUuid={}, eventUuid={}",
					topic,
					event.followeeUuid(),
					event.eventUuid()
			);
		}
	}

	private static String requireTopic(String topic, String label) {
		if (topic == null || topic.isBlank()) {
			throw new IllegalArgumentException(label + " Kafka topic is required.");
		}
		return topic;
	}
}
