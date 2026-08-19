package com.planwith.planwith_fo_member.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.planwith.planwith_fo_member.domain.follow.Follow;
import com.planwith.planwith_fo_member.domain.member.MemberProfile;

public interface FollowRepositoryPort {

	Follow save(Follow follow);

	Optional<Follow> findByPair(UUID followerMemberUuid, UUID followeeMemberUuid);

	Follow updateActive(Long followId, boolean active);

	boolean existsActive(UUID followerMemberUuid, UUID followeeMemberUuid);

	long countActiveFollowers(UUID followeeMemberUuid);

	long countActiveFollowings(UUID followerMemberUuid);

	PagedProfiles findActiveFollowerProfiles(UUID followeeMemberUuid, int page, int size);

	PagedProfiles findActiveFollowingProfiles(UUID followerMemberUuid, int page, int size);

	record PagedProfiles(
			List<MemberProfile> profiles,
			int page,
			int size,
			long totalElements,
			int totalPages
	) {
	}
}
