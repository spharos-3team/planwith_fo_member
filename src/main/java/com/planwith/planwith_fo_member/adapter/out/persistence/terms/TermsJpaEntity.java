package com.planwith.planwith_fo_member.adapter.out.persistence.terms;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "terms")
public class TermsJpaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "term_id")
	private Long termId;

	@Column(name = "term_uuid", nullable = false, unique = true, length = 36)
	private String termUuid;

	@Column(name = "title", nullable = false, length = 100)
	private String title;

	@Column(name = "term_type", nullable = false, length = 30)
	private String termType;

	@Column(name = "version", nullable = false, length = 20)
	private String version;

	@Lob
	@Column(name = "content")
	private String content;

	@Column(name = "is_required", nullable = false)
	private boolean required;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	public Long getTermId() {
		return termId;
	}

	public String getTermUuid() {
		return termUuid;
	}

	public void setTermUuid(String termUuid) {
		this.termUuid = termUuid;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTermType() {
		return termType;
	}

	public void setTermType(String termType) {
		this.termType = termType;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
