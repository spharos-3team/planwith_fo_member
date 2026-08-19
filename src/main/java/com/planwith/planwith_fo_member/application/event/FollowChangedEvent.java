package com.planwith.planwith_fo_member.application.event;

public record FollowChangedEvent(
		String eventUuid,
		String eventType,
		String followerUuid,
		String followeeUuid,
		String occurredAt,
		Long sourceVersion
) {
	public static final String CREATED = "FollowCreated";
	public static final String REMOVED = "FollowRemoved";

	public static FollowChangedEvent created(
			String eventUuid,
			String followerUuid,
			String followeeUuid,
			String occurredAt,
			Long sourceVersion
	) {
		return new FollowChangedEvent(eventUuid, CREATED, followerUuid, followeeUuid, occurredAt, sourceVersion);
	}

	public static FollowChangedEvent removed(
			String eventUuid,
			String followerUuid,
			String followeeUuid,
			String occurredAt,
			Long sourceVersion
	) {
		return new FollowChangedEvent(eventUuid, REMOVED, followerUuid, followeeUuid, occurredAt, sourceVersion);
	}
}
