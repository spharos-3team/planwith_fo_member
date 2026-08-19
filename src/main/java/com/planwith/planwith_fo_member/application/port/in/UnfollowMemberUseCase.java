package com.planwith.planwith_fo_member.application.port.in;

import java.util.UUID;

public interface UnfollowMemberUseCase {

	void unfollow(UUID followerMemberUuid, UUID followeeMemberUuid);
}
