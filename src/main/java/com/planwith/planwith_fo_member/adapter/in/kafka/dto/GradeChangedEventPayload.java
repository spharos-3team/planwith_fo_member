package com.planwith.planwith_fo_member.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GradeChangedEventPayload(
		String eventUuid,
		String memberUuid,
		String previousGradeCode,
		String currentGradeCode,
		int previousGradeLevel,
		int currentGradeLevel,
		String changedAt,
		CurrentBenefits currentBenefits
) {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record CurrentBenefits(
			int monthlyTokenAmount,
			boolean profileBadge,
			boolean profileSpecialBorder,
			boolean membershipPublicStory,
			boolean membershipAccess,
			String storyPriorityExposure
	) {
	}
}
