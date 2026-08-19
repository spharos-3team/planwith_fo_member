package com.planwith.planwith_fo_member.application.follow;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_member.application.event.FollowChangedEvent;
import com.planwith.planwith_fo_member.application.port.out.FollowEventPort;
import com.planwith.planwith_fo_member.domain.follow.Follow;

class FollowEventPublisherTest {

	@Test
	void publishesStableEventUuidForSameFollowAndSourceVersion() {
		CapturingFollowEventPort port = new CapturingFollowEventPort();
		FollowEventPublisher publisher = new FollowEventPublisher(port);
		Follow follow = Follow.create(UUID.randomUUID(), UUID.randomUUID());

		publisher.publishCreated(follow, 3L);
		publisher.publishCreated(follow, 3L);

		assertThat(port.created).hasSize(2);
		assertThat(port.created.get(0).eventUuid()).isEqualTo(port.created.get(1).eventUuid());
		assertThat(port.created.get(0).eventUuid()).isEqualTo(
				FollowEventPublisher.eventUuidOf(FollowChangedEvent.CREATED, follow.getFollowUuid(), 3L)
		);
		assertThat(port.created.get(0).followeeUuid()).isEqualTo(follow.getFolloweeMemberUuid().toString());
		assertThat(port.created.get(0).sourceVersion()).isEqualTo(3L);
		assertThat(UUID.fromString(port.created.get(0).eventUuid())).isNotNull();
	}

	@Test
	void publishesDistinctEventUuidForRemoved() {
		CapturingFollowEventPort port = new CapturingFollowEventPort();
		FollowEventPublisher publisher = new FollowEventPublisher(port);
		Follow follow = Follow.create(UUID.randomUUID(), UUID.randomUUID());

		publisher.publishCreated(follow, 1L);
		publisher.publishRemoved(follow, 2L);

		assertThat(port.created.get(0).eventUuid()).isNotEqualTo(port.removed.get(0).eventUuid());
		assertThat(port.removed.get(0).eventType()).isEqualTo(FollowChangedEvent.REMOVED);
		assertThat(port.removed.get(0).sourceVersion()).isEqualTo(2L);
	}

	private static final class CapturingFollowEventPort implements FollowEventPort {

		private final List<FollowChangedEvent> created = new ArrayList<>();
		private final List<FollowChangedEvent> removed = new ArrayList<>();

		@Override
		public void publishCreated(FollowChangedEvent event) {
			created.add(event);
		}

		@Override
		public void publishRemoved(FollowChangedEvent event) {
			removed.add(event);
		}
	}
}
