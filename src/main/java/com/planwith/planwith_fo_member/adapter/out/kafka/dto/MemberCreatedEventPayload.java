package com.planwith.planwith_fo_member.adapter.out.kafka.dto;

/**
 * grade-service {@code MemberCreatedEventConsumer} 수신 계약.
 * 필수 필드: {@code eventUuid}, {@code memberUuid}
 */
public record MemberCreatedEventPayload(
		String eventUuid,
		String eventType,
		String memberUuid
) {
}
