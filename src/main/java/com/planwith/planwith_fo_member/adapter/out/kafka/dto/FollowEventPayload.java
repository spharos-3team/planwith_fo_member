package com.planwith.planwith_fo_member.adapter.out.kafka.dto;

/**
 * grade-service FollowCreated / FollowRemoved 수신 계약.
 * Kafka key는 등급 집계 대상인 {@code followeeUuid}를 사용한다.
 */
public record FollowEventPayload(
		String eventUuid,
		String followerUuid,
		String followeeUuid,
		String occurredAt,
		Long sourceVersion
) {
}
