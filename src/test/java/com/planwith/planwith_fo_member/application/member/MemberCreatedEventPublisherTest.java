package com.planwith.planwith_fo_member.application.member;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_member.application.event.MemberCreatedEvent;
import com.planwith.planwith_fo_member.application.port.out.MemberCreatedEventPort;

class MemberCreatedEventPublisherTest {

	@Test
	void publishesContractPayloadWithEventUuidAndMemberUuid() {
		CapturingMemberCreatedEventPort port = new CapturingMemberCreatedEventPort();
		MemberCreatedEventPublisher publisher = new MemberCreatedEventPublisher(port);
		UUID memberUuid = UUID.randomUUID();

		publisher.publish(memberUuid);

		assertThat(port.events).hasSize(1);
		MemberCreatedEvent event = port.events.get(0);
		assertThat(event.eventType()).isEqualTo(MemberCreatedEvent.EVENT_TYPE);
		assertThat(event.memberUuid()).isEqualTo(memberUuid.toString());
		assertThat(event.eventUuid()).isNotBlank();
		assertThat(UUID.fromString(event.eventUuid())).isNotNull();
	}

	private static final class CapturingMemberCreatedEventPort implements MemberCreatedEventPort {

		private final List<MemberCreatedEvent> events = new ArrayList<>();

		@Override
		public void publish(MemberCreatedEvent event) {
			events.add(event);
		}
	}
}
