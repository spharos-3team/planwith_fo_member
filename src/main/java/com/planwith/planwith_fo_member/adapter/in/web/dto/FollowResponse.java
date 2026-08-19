package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.util.UUID;

public record FollowResponse(
		UUID followUuid,
		boolean isActive
) {
}
