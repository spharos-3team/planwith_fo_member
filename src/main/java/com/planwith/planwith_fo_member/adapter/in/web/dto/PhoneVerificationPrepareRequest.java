package com.planwith.planwith_fo_member.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "본인인증 준비 요청. 스텁에서만 phoneNumber/name을 사용하고, 실연동(포트원 SDK)에서는 비워도 된다.")
public record PhoneVerificationPrepareRequest(
		@Schema(description = "스텁 전용 휴대폰 번호. confirm에 같은 identityVerificationId를 넣으면 이 번호로 인증된다.", example = "01012345678")
		@Size(max = 30, message = "휴대폰 번호는 30자 이하여야 합니다.")
		String phoneNumber,

		@Schema(description = "스텁 전용 실명. 가입/휴대폰 변경 시 name과 같아야 한다.", example = "홍길동")
		@Size(max = 100, message = "이름은 100자 이하여야 합니다.")
		String name
) {
}
