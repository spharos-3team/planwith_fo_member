package com.planwith.planwith_fo_member.adapter.out.persistence.member;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "processed_member_event",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_processed_member_event_uuid",
				columnNames = {"event_uuid"}
		)
)
public class ProcessedMemberEventJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "processed_id")
	private Long processedId;

	@Column(name = "event_uuid", nullable = false, length = 36)
	private String eventUuid;

	@Column(name = "member_uuid", nullable = false, length = 36)
	private String memberUuid;

	@Column(name = "processed_at", nullable = false)
	private Instant processedAt;

	public Long getProcessedId() {
		return processedId;
	}

	public String getEventUuid() {
		return eventUuid;
	}

	public void setEventUuid(String eventUuid) {
		this.eventUuid = eventUuid;
	}

	public String getMemberUuid() {
		return memberUuid;
	}

	public void setMemberUuid(String memberUuid) {
		this.memberUuid = memberUuid;
	}

	public Instant getProcessedAt() {
		return processedAt;
	}

	public void setProcessedAt(Instant processedAt) {
		this.processedAt = processedAt;
	}
}
