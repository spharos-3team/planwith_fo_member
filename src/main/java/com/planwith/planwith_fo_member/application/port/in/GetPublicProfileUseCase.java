package com.planwith.planwith_fo_member.application.port.in;

import java.util.UUID;

public interface GetPublicProfileUseCase {

	PublicProfileResult getPublic(UUID memberUuid, UUID viewerMemberUuid);

	record PublicProfileResult(
			UUID memberUuid,
			String nickname,
			String profileImage,
			String profileIntro,
			String grade,
			boolean profileBadge,
			boolean profileSpecialBorder,
			long followerCount,
			long followingCount,
			Boolean isFollowing
	) {
	}
}
