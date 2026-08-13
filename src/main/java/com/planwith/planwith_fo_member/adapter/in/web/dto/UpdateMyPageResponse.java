package com.planwith.planwith_fo_member.adapter.in.web.dto;

import java.util.List;

public record UpdateMyPageResponse(
		MemberMeResponse member,
		MemberProfileResponse profile,
		List<MemberAgreementResponse> agreements
) {
}
