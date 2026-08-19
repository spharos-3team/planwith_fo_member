package com.planwith.planwith_fo_member.application.port.in;

import java.util.UUID;

public interface FollowMemberUseCase {

	FollowResult follow(UUID followerMemberUuid, UUID followeeMemberUuid);

	record FollowResult(UUID followUuid, boolean isActive) {
	}
}
