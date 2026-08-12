package com.planwith.planwith_fo_member.domain.terms;

import java.util.UUID;

public class Term {

	private final Long termId;
	private final UUID termUuid;
	private final String title;
	private final String termType;
	private final String version;
	private final String content;
	private final boolean required;
	private final boolean active;

	public Term(
			Long termId,
			UUID termUuid,
			String title,
			String termType,
			String version,
			String content,
			boolean required,
			boolean active
	) {
		this.termId = termId;
		this.termUuid = termUuid;
		this.title = title;
		this.termType = termType;
		this.version = version;
		this.content = content;
		this.required = required;
		this.active = active;
	}

	public Long getTermId() {
		return termId;
	}

	public UUID getTermUuid() {
		return termUuid;
	}

	public String getTitle() {
		return title;
	}

	public String getTermType() {
		return termType;
	}

	public String getVersion() {
		return version;
	}

	public String getContent() {
		return content;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isActive() {
		return active;
	}
}
