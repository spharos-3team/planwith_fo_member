package com.planwith.planwith_fo_member.adapter.out.persistence.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "member_profile_images")
public class MemberProfileImageJpaEntity {

	@Id
	@Column(name = "member_uuid", nullable = false, length = 36)
	private String memberUuid;

	@Column(name = "content_type", nullable = false, length = 64)
	private String contentType;

	@Lob
	@Column(name = "image_bytes", nullable = false, columnDefinition = "longblob")
	private byte[] imageBytes;

	public String getMemberUuid() {
		return memberUuid;
	}

	public void setMemberUuid(String memberUuid) {
		this.memberUuid = memberUuid;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}
}
