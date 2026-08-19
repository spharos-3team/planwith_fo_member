package com.planwith.planwith_fo_member.adapter.in.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_member.adapter.in.kafka.dto.GradeChangedEventPayload;
import com.planwith.planwith_fo_member.application.port.in.ApplyGradeChangedUseCase;

@Component
@ConditionalOnProperty(prefix = "app.kafka", name = "consumer-enabled", havingValue = "true")
public class GradeChangedEventConsumer {

	private static final Logger log = LoggerFactory.getLogger(GradeChangedEventConsumer.class);

	private final ApplyGradeChangedUseCase applyGradeChangedUseCase;
	private final ObjectMapper objectMapper;

	public GradeChangedEventConsumer(
			ApplyGradeChangedUseCase applyGradeChangedUseCase,
			ObjectMapper objectMapper
	) {
		this.applyGradeChangedUseCase = applyGradeChangedUseCase;
		this.objectMapper = objectMapper;
	}

	@KafkaListener(topics = "${app.kafka.grade-changed-topic}")
	public void consume(
			@Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
			@Payload String payload
	) {
		log.info("GradeChangedEventConsumer : consume : 등급 변경 이벤트 수신 - topic={}", topic);
		GradeChangedEventPayload event = parse(payload);
		if (event == null) {
			log.error("GradeChangedEventConsumer : consume : 등급 변경 이벤트 파싱 실패로 반영을 생략");
			return;
		}
		try {
			GradeChangedEventPayload.CurrentBenefits benefits = event.currentBenefits();
			applyGradeChangedUseCase.apply(new ApplyGradeChangedUseCase.GradeChangedCommand(
					event.eventUuid(),
					event.memberUuid(),
					event.currentGradeCode(),
					benefits != null && benefits.profileBadge(),
					benefits != null && benefits.profileSpecialBorder()
			));
		} catch (IllegalArgumentException exception) {
			log.error("GradeChangedEventConsumer : consume : 잘못된 등급 변경 이벤트로 반영을 생략 - memberUuid={}",
					event.memberUuid());
		} catch (RuntimeException exception) {
			log.error("GradeChangedEventConsumer : consume : 등급 변경 반영 실패로 재처리 대기 - memberUuid={}",
					event.memberUuid());
			throw exception;
		}
	}

	private GradeChangedEventPayload parse(String payload) {
		if (payload == null || payload.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(payload, GradeChangedEventPayload.class);
		} catch (JsonProcessingException exception) {
			log.error("GradeChangedEventConsumer : consume : 등급 변경 이벤트 JSON 파싱 실패");
			return null;
		}
	}
}
