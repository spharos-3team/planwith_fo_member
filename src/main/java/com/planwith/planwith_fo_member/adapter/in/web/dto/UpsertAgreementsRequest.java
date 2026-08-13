package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record UpsertAgreementsRequest(
		@NotNull(message = "약관 동의 목록은 필수입니다.")
		@Valid
		List<Item> agreements
) {
	public record Item(
			@NotNull(message = "termUuid는 필수입니다.")
			UUID termUuid,

			@NotNull(message = "agreed는 필수입니다.")
			Boolean agreed
	) {
	}
}
