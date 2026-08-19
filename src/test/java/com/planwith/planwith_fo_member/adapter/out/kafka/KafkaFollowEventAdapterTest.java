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
import com.planwith.planwith_fo_member.application.event.FollowChangedEvent;
import com.planwith.planwith_fo_member.config.MemberKafkaProperties;

@ExtendWith(MockitoExtension.class)
class KafkaFollowEventAdapterTest {

	@Mock
	private KafkaTemplate<String, String> kafkaTemplate;

	@Test
	void publishesFollowCreatedWithFolloweeUuidKey() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		KafkaFollowEventAdapter adapter = new KafkaFollowEventAdapter(kafkaTemplate, objectMapper, kafkaProperties());
		FollowChangedEvent event = FollowChangedEvent.created(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString(),
				"2026-08-19T13:00:00Z",
				15L
		);
		when(kafkaTemplate.send(eq("planwith.follow.created"), eq(event.followeeUuid()), org.mockito.ArgumentMatchers.anyString()))
				.thenReturn(CompletableFuture.completedFuture(null));

		adapter.publishCreated(event);

		ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
		verify(kafkaTemplate).send(eq("planwith.follow.created"), eq(event.followeeUuid()), payloadCaptor.capture());
		JsonNode payload = objectMapper.readTree(payloadCaptor.getValue());
		assertThat(payload.get("eventUuid").asText()).isEqualTo(event.eventUuid());
		assertThat(payload.get("followerUuid").asText()).isEqualTo(event.followerUuid());
		assertThat(payload.get("followeeUuid").asText()).isEqualTo(event.followeeUuid());
		assertThat(payload.get("occurredAt").asText()).isEqualTo("2026-08-19T13:00:00Z");
		assertThat(payload.get("sourceVersion").asLong()).isEqualTo(15L);
	}

	@Test
	void publishesFollowRemovedWithFolloweeUuidKey() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		KafkaFollowEventAdapter adapter = new KafkaFollowEventAdapter(kafkaTemplate, objectMapper, kafkaProperties());
		FollowChangedEvent event = FollowChangedEvent.removed(
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString(),
				UUID.randomUUID().toString(),
				"2026-08-19T13:00:01Z",
				16L
		);
		when(kafkaTemplate.send(eq("planwith.follow.removed"), eq(event.followeeUuid()), org.mockito.ArgumentMatchers.anyString()))
				.thenReturn(CompletableFuture.completedFuture(null));

		adapter.publishRemoved(event);

		ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
		verify(kafkaTemplate).send(eq("planwith.follow.removed"), eq(event.followeeUuid()), payloadCaptor.capture());
		JsonNode payload = objectMapper.readTree(payloadCaptor.getValue());
		assertThat(payload.get("eventUuid").asText()).isEqualTo(event.eventUuid());
		assertThat(payload.get("followeeUuid").asText()).isEqualTo(event.followeeUuid());
		assertThat(payload.get("sourceVersion").asLong()).isEqualTo(16L);
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
