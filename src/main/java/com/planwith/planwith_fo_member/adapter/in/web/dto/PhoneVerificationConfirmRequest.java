package com.planwith.planwith_fo_member.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "본인인증 완료 확인. 실연동은 identityVerificationId만 사용한다. 스텁은 phoneNumber/name으로 휴대폰·실명을 지정할 수 있다.")
public record PhoneVerificationConfirmRequest(
		@Schema(
				description = "POST /auth/phone-verifications 응답값. 스텁만 쓸 때는 identity-verification-stub-{휴대폰번호} 형식도 가능.",
				example = "identity-verification-stub-01012345678"
		)
		@NotBlank(message = "identityVerificationId는 필수입니다.")
		String identityVerificationId,

		@Schema(description = "스텁 전용. 본인인증 휴대폰 번호. 없으면 id 끝자리 또는 prepare에서 지정한 번호.", example = "01012345678")
		@Size(max = 30, message = "휴대폰 번호는 30자 이하여야 합니다.")
		String phoneNumber,

		@Schema(description = "스텁 전용. 본인인증 실명. 없으면 prepare 값 또는 '테스트사용자'. 가입 시 name과 같아야 한다.", example = "홍길동")
		@Size(max = 100, message = "이름은 100자 이하여야 합니다.")
		String name
) {
}
