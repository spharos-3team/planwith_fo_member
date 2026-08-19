package com.planwith.planwith_fo_member.domain.follow;

import java.util.UUID;

public class Follow {

	private final Long followId;
	private final UUID followUuid;
	private final UUID followerMemberUuid;
	private final UUID followeeMemberUuid;
	private final boolean active;

	public Follow(
			Long followId,
			UUID followUuid,
			UUID followerMemberUuid,
			UUID followeeMemberUuid,
			boolean active
	) {
		if (followerMemberUuid.equals(followeeMemberUuid)) {
			throw new IllegalArgumentException("cannot follow self");
		}
		this.followId = followId;
		this.followUuid = followUuid;
		this.followerMemberUuid = followerMemberUuid;
		this.followeeMemberUuid = followeeMemberUuid;
		this.active = active;
	}

	public static Follow create(UUID followerMemberUuid, UUID followeeMemberUuid) {
		return new Follow(null, UUID.randomUUID(), followerMemberUuid, followeeMemberUuid, true);
	}

	public Follow activated() {
		return new Follow(followId, followUuid, followerMemberUuid, followeeMemberUuid, true);
	}

	public Follow deactivated() {
		return new Follow(followId, followUuid, followerMemberUuid, followeeMemberUuid, false);
	}

	public Long getFollowId() {
		return followId;
	}

	public UUID getFollowUuid() {
		return followUuid;
	}

	public UUID getFollowerMemberUuid() {
		return followerMemberUuid;
	}

	public UUID getFolloweeMemberUuid() {
		return followeeMemberUuid;
	}

	public boolean isActive() {
		return active;
	}
}
