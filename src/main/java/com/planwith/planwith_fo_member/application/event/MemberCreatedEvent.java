package com.planwith.planwith_fo_member.application.event;

public record MemberCreatedEvent(
		String eventUuid,
		String eventType,
		String memberUuid
) {
	public static final String EVENT_TYPE = "MemberCreated";

	public static MemberCreatedEvent of(String eventUuid, String memberUuid) {
		return new MemberCreatedEvent(eventUuid, EVENT_TYPE, memberUuid);
	}
}
