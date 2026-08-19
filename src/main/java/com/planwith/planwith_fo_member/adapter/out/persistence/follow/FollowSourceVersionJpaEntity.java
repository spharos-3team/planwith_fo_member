package com.planwith.planwith_fo_member.adapter.out.persistence.follow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "follow_source_version")
public class FollowSourceVersionJpaEntity {

	@Id
	@Column(name = "followee_member_uuid", length = 36)
	private String followeeMemberUuid;

	@Column(name = "source_version", nullable = false)
	private long sourceVersion;

	public String getFolloweeMemberUuid() {
		return followeeMemberUuid;
	}

	public void setFolloweeMemberUuid(String followeeMemberUuid) {
		this.followeeMemberUuid = followeeMemberUuid;
	}

	public long getSourceVersion() {
		return sourceVersion;
	}

	public void setSourceVersion(long sourceVersion) {
		this.sourceVersion = sourceVersion;
	}
}
