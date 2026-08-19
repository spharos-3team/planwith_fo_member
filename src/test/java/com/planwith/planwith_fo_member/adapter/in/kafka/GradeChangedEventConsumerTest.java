package com.planwith.planwith_fo_member.adapter.in.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_member.application.port.in.ApplyGradeChangedUseCase;

class GradeChangedEventConsumerTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void appliesGradeAndBenefitsFromPayload() {
		CapturingApplyGradeChangedUseCase useCase = new CapturingApplyGradeChangedUseCase();
		GradeChangedEventConsumer consumer = new GradeChangedEventConsumer(useCase, objectMapper);
		String memberUuid = UUID.randomUUID().toString();
		String eventUuid = UUID.randomUUID().toString();

		consumer.consume("planwith.grade.changed", """
				{
				  "eventUuid":"%s",
				  "memberUuid":"%s",
				  "previousGradeCode":"TRAVELER",
				  "currentGradeCode":"EXPLORER",
				  "previousGradeLevel":3,
				  "currentGradeLevel":4,
				  "changedAt":"2026-08-19T06:00:00Z",
				  "currentBenefits":{
				    "monthlyTokenAmount":100,
				    "profileBadge":true,
				    "profileSpecialBorder":true,
				    "membershipPublicStory":false,
				    "membershipAccess":false,
				    "storyPriorityExposure":null
				  }
				}
				""".formatted(eventUuid, memberUuid));

		assertThat(useCase.commands).hasSize(1);
		ApplyGradeChangedUseCase.GradeChangedCommand command = useCase.commands.get(0);
		assertThat(command.eventUuid()).isEqualTo(eventUuid);
		assertThat(command.memberUuid()).isEqualTo(memberUuid);
		assertThat(command.currentGradeCode()).isEqualTo("EXPLORER");
		assertThat(command.profileBadge()).isTrue();
		assertThat(command.profileSpecialBorder()).isTrue();
	}

	@Test
	void treatsMissingBenefitsAsDisabled() {
		CapturingApplyGradeChangedUseCase useCase = new CapturingApplyGradeChangedUseCase();
		GradeChangedEventConsumer consumer = new GradeChangedEventConsumer(useCase, objectMapper);

		consumer.consume("planwith.grade.changed", """
				{"eventUuid":"%s","memberUuid":"%s","currentGradeCode":"ROOKIE"}
				""".formatted(UUID.randomUUID(), UUID.randomUUID()));

		assertThat(useCase.commands).hasSize(1);
		assertThat(useCase.commands.get(0).currentGradeCode()).isEqualTo("ROOKIE");
		assertThat(useCase.commands.get(0).profileBadge()).isFalse();
		assertThat(useCase.commands.get(0).profileSpecialBorder()).isFalse();
	}

	@Test
	void ignoresInvalidJson() {
		CapturingApplyGradeChangedUseCase useCase = new CapturingApplyGradeChangedUseCase();
		GradeChangedEventConsumer consumer = new GradeChangedEventConsumer(useCase, objectMapper);

		consumer.consume("planwith.grade.changed", "{not-json");

		assertThat(useCase.commands).isEmpty();
	}

	@Test
	void retriesTransientFailures() {
		GradeChangedEventConsumer consumer = new GradeChangedEventConsumer(command -> {
			throw new IllegalStateException("db down");
		}, objectMapper);

		assertThatThrownBy(() -> consumer.consume("planwith.grade.changed", """
				{"eventUuid":"%s","memberUuid":"%s","currentGradeCode":"LEAF"}
				""".formatted(UUID.randomUUID(), UUID.randomUUID())))
				.isInstanceOf(IllegalStateException.class);
	}

	private static final class CapturingApplyGradeChangedUseCase implements ApplyGradeChangedUseCase {

		private final List<GradeChangedCommand> commands = new ArrayList<>();

		@Override
		public void apply(GradeChangedCommand command) {
			commands.add(command);
		}
	}
}
