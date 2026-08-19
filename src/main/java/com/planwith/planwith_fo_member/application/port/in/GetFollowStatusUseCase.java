package com.planwith.planwith_fo_member.application.port.in;

import java.util.UUID;

public interface GetFollowStatusUseCase {

	boolean isFollowing(UUID followerMemberUuid, UUID followeeMemberUuid);
}
