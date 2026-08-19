package com.planwith.planwith_fo_member.adapter.out.persistence.follow;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "follow",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_follow_member_pair",
				columnNames = {"follower_member_uuid", "followee_member_uuid"}
		),
		indexes = {
				@Index(name = "idx_follow_follower", columnList = "follower_member_uuid,is_active"),
				@Index(name = "idx_follow_followee", columnList = "followee_member_uuid,is_active")
		},
		check = @CheckConstraint(
				name = "chk_follow_self",
				constraint = "follower_member_uuid <> followee_member_uuid"
		)
)
public class FollowJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "follow_id")
	private Long followId;

	@Column(name = "follow_uuid", nullable = false, unique = true, length = 36)
	private String followUuid;

	@Column(name = "follower_member_uuid", nullable = false, length = 36)
	private String followerMemberUuid;

	@Column(name = "followee_member_uuid", nullable = false, length = 36)
	private String followeeMemberUuid;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	public Long getFollowId() {
		return followId;
	}

	public String getFollowUuid() {
		return followUuid;
	}

	public void setFollowUuid(String followUuid) {
		this.followUuid = followUuid;
	}

	public String getFollowerMemberUuid() {
		return followerMemberUuid;
	}

	public void setFollowerMemberUuid(String followerMemberUuid) {
		this.followerMemberUuid = followerMemberUuid;
	}

	public String getFolloweeMemberUuid() {
		return followeeMemberUuid;
	}

	public void setFolloweeMemberUuid(String followeeMemberUuid) {
		this.followeeMemberUuid = followeeMemberUuid;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
