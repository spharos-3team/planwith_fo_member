package com.planwith.planwith_fo_member.application.follow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.planwith.planwith_fo_member.application.port.out.FollowEventPort;
import com.planwith.planwith_fo_member.application.port.out.FollowRepositoryPort;
import com.planwith.planwith_fo_member.application.port.out.MemberRepositoryPort;
import com.planwith.planwith_fo_member.domain.follow.Follow;
import com.planwith.planwith_fo_member.domain.member.LoginType;
import com.planwith.planwith_fo_member.domain.member.Member;
import com.planwith.planwith_fo_member.domain.member.MemberStatus;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

	@Mock
	private FollowRepositoryPort followRepository;

	@Mock
	private MemberRepositoryPort memberRepository;

	private CapturingFollowEventPort followEventPort;
	private FollowService followService;

	@BeforeEach
	void setUp() {
		followEventPort = new CapturingFollowEventPort();
		followService = new FollowService(
				followRepository,
				memberRepository,
				new FollowEventPublisher(followEventPort)
		);
	}

	@Test
	void followPublishesCreatedEventWithMonotonicSourceVersion() {
		UUID followerUuid = UUID.randomUUID();
		UUID followeeUuid = UUID.randomUUID();
		Follow saved = new Follow(1L, UUID.randomUUID(), followerUuid, followeeUuid, true);
		stubActiveMembers(followerUuid, followeeUuid);
		when(followRepository.findByPair(followerUuid, followeeUuid)).thenReturn(Optional.empty());
		when(followRepository.save(any(Follow.class))).thenReturn(saved);
		when(followRepository.nextSourceVersion(followeeUuid)).thenReturn(1L);

		followService.follow(followerUuid, followeeUuid);

		assertThat(followEventPort.createdCount).isEqualTo(1);
		assertThat(followEventPort.lastFolloweeUuid).isEqualTo(followeeUuid.toString());
		assertThat(followEventPort.lastSourceVersion).isEqualTo(1L);
	}

	@Test
	void duplicateActiveFollowDoesNotPublish() {
		UUID followerUuid = UUID.randomUUID();
		UUID followeeUuid = UUID.randomUUID();
		Follow existing = new Follow(1L, UUID.randomUUID(), followerUuid, followeeUuid, true);
		stubActiveMembers(followerUuid, followeeUuid);
		when(followRepository.findByPair(followerUuid, followeeUuid)).thenReturn(Optional.of(existing));

		followService.follow(followerUuid, followeeUuid);

		assertThat(followEventPort.createdCount).isZero();
		assertThat(followEventPort.removedCount).isZero();
		verify(followRepository, never()).nextSourceVersion(any());
	}

	@Test
	void unfollowPublishesRemovedEvent() {
		UUID followerUuid = UUID.randomUUID();
		UUID followeeUuid = UUID.randomUUID();
		Follow existing = new Follow(1L, UUID.randomUUID(), followerUuid, followeeUuid, true);
		Follow deactivated = existing.deactivated();
		stubActiveMembers(followerUuid, followeeUuid);
		when(followRepository.findByPair(followerUuid, followeeUuid)).thenReturn(Optional.of(existing));
		when(followRepository.updateActive(1L, false)).thenReturn(deactivated);
		when(followRepository.nextSourceVersion(followeeUuid)).thenReturn(2L);

		followService.unfollow(followerUuid, followeeUuid);

		assertThat(followEventPort.removedCount).isEqualTo(1);
		assertThat(followEventPort.lastSourceVersion).isEqualTo(2L);
	}

	@Test
	void inactiveUnfollowDoesNotPublish() {
		UUID followerUuid = UUID.randomUUID();
		UUID followeeUuid = UUID.randomUUID();
		stubActiveMembers(followerUuid, followeeUuid);
		when(followRepository.findByPair(followerUuid, followeeUuid)).thenReturn(Optional.empty());

		followService.unfollow(followerUuid, followeeUuid);

		assertThat(followEventPort.removedCount).isZero();
		verify(followRepository, never()).updateActive(any(), eq(false));
	}

	private void stubActiveMembers(UUID followerUuid, UUID followeeUuid) {
		when(memberRepository.findByUuid(followerUuid)).thenReturn(Optional.of(activeMember(followerUuid)));
		when(memberRepository.findByUuid(followeeUuid)).thenReturn(Optional.of(activeMember(followeeUuid)));
	}

	private Member activeMember(UUID memberUuid) {
		return new Member(
				1L,
				memberUuid,
				LoginType.LOCAL,
				memberUuid + "@example.com",
				"hash",
				"01000000000",
				"이름",
				null,
				MemberStatus.ACTIVE,
				Instant.parse("2026-08-19T00:00:00Z")
		);
	}

	private static final class CapturingFollowEventPort implements FollowEventPort {

		private int createdCount;
		private int removedCount;
		private String lastFolloweeUuid;
		private Long lastSourceVersion;

		@Override
		public void publishCreated(com.planwith.planwith_fo_member.application.event.FollowChangedEvent event) {
			createdCount++;
			lastFolloweeUuid = event.followeeUuid();
			lastSourceVersion = event.sourceVersion();
		}

		@Override
		public void publishRemoved(com.planwith.planwith_fo_member.application.event.FollowChangedEvent event) {
			removedCount++;
			lastFolloweeUuid = event.followeeUuid();
			lastSourceVersion = event.sourceVersion();
		}
	}
}
