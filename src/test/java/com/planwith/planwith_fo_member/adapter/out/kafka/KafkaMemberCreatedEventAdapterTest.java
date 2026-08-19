package com.planwith.planwith_fo_member.adapter.out.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_member.application.event.MemberCreatedEvent;
import com.planwith.planwith_fo_member.config.MemberKafkaProperties;

@ExtendWith(MockitoExtension.class)
class KafkaMemberCreatedEventAdapterTest {

	@Mock
	private KafkaTemplate<String, String> kafkaTemplate;

	@Test
	void publishesJsonMatchingGradeServiceContractWithMemberUuidKey() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		MemberKafkaProperties properties = kafkaProperties();
		KafkaMemberCreatedEventAdapter adapter = new KafkaMemberCreatedEventAdapter(
				kafkaTemplate,
				objectMapper,
				properties
		);
		String eventUuid = UUID.randomUUID().toString();
		String memberUuid = UUID.randomUUID().toString();
		when(kafkaTemplate.send(eq("planwith.member.created"), eq(memberUuid), org.mockito.ArgumentMatchers.anyString()))
				.thenReturn(CompletableFuture.completedFuture(null));

		adapter.publish(MemberCreatedEvent.of(eventUuid, memberUuid));

		ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
		verify(kafkaTemplate).send(eq("planwith.member.created"), eq(memberUuid), payloadCaptor.capture());

		JsonNode payload = objectMapper.readTree(payloadCaptor.getValue());
		assertThat(payload.get("eventUuid").asText()).isEqualTo(eventUuid);
		assertThat(payload.get("memberUuid").asText()).isEqualTo(memberUuid);
		assertThat(payload.get("eventType").asText()).isEqualTo("MemberCreated");
	}

	private static MemberKafkaProperties kafkaProperties() {
		return new MemberKafkaProperties(
				true,
				true,
				"planwith.member.created",
				"planwith.follow.created",
				"planwith.follow.removed",
				"planwith.grade.changed"
		);
	}
}
