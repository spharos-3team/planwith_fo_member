package com.planwith.planwith_fo_member.adapter.out.persistence.terms;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "member_term_agreements")
public class MemberTermAgreementJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "agreement_id")
	private Long agreementId;

	@Column(name = "term_id", nullable = false)
	private Long termId;

	@Column(name = "member_uuid", nullable = false, length = 36)
	private String memberUuid;

	@Column(name = "agreed", nullable = false)
	private boolean agreed;

	@Column(name = "agreed_at")
	private Instant agreedAt;

	public Long getAgreementId() {
		return agreementId;
	}

	public Long getTermId() {
		return termId;
	}

	public void setTermId(Long termId) {
		this.termId = termId;
	}

	public String getMemberUuid() {
		return memberUuid;
	}

	public void setMemberUuid(String memberUuid) {
		this.memberUuid = memberUuid;
	}

	public boolean isAgreed() {
		return agreed;
	}

	public void setAgreed(boolean agreed) {
		this.agreed = agreed;
	}

	public Instant getAgreedAt() {
		return agreedAt;
	}

	public void setAgreedAt(Instant agreedAt) {
		this.agreedAt = agreedAt;
	}
}
