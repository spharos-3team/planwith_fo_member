package com.planwith.planwith_fo_member.application.follow;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_member.application.event.FollowChangedEvent;
import com.planwith.planwith_fo_member.application.port.out.FollowEventPort;
import com.planwith.planwith_fo_member.domain.follow.Follow;

@Component
public class FollowEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(FollowEventPublisher.class);

	private final FollowEventPort followEventPort;

	public FollowEventPublisher(FollowEventPort followEventPort) {
		this.followEventPort = followEventPort;
	}

	public void publishCreated(Follow follow, long sourceVersion) {
		Follow required = requireFollow(follow);
		FollowChangedEvent event = FollowChangedEvent.created(
				eventUuidOf(FollowChangedEvent.CREATED, required.getFollowUuid(), sourceVersion),
				required.getFollowerMemberUuid().toString(),
				required.getFolloweeMemberUuid().toString(),
				Instant.now().toString(),
				sourceVersion
		);
		log.info(
				"FollowEventPublisher : publishCreated : 팔로우 생성 이벤트 발행 요청 - followeeUuid={}, followerUuid={}, eventUuid={}, sourceVersion={}",
				event.followeeUuid(),
				event.followerUuid(),
				event.eventUuid(),
				event.sourceVersion()
		);
		followEventPort.publishCreated(event);
	}

	public void publishRemoved(Follow follow, long sourceVersion) {
		Follow required = requireFollow(follow);
		FollowChangedEvent event = FollowChangedEvent.removed(
				eventUuidOf(FollowChangedEvent.REMOVED, required.getFollowUuid(), sourceVersion),
				required.getFollowerMemberUuid().toString(),
				required.getFolloweeMemberUuid().toString(),
				Instant.now().toString(),
				sourceVersion
		);
		log.info(
				"FollowEventPublisher : publishRemoved : 팔로우 해제 이벤트 발행 요청 - followeeUuid={}, followerUuid={}, eventUuid={}, sourceVersion={}",
				event.followeeUuid(),
				event.followerUuid(),
				event.eventUuid(),
				event.sourceVersion()
		);
		followEventPort.publishRemoved(event);
	}

	static String eventUuidOf(String eventType, UUID followUuid, long sourceVersion) {
		return UUID.nameUUIDFromBytes(
				(eventType + ":" + followUuid + ":" + sourceVersion).getBytes(StandardCharsets.UTF_8)
		).toString();
	}

	private Follow requireFollow(Follow follow) {
		return Objects.requireNonNull(follow, "Follow is required.");
	}
}

