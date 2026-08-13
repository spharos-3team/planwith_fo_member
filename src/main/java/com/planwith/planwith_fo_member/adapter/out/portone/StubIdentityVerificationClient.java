package com.planwith.planwith_fo_member.adapter.out.portone;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.planwith.planwith_fo_member.application.port.out.IdentityVerificationClientPort;

/**
 * 테스트/로컬용 스텁.
 * identityVerificationId가 {@code ...-010XXXXXXXX} 형태면 해당 번호를 사용하고,
 * 아니면 기본값 01012345678을 반환한다.
 */
@Component
@ConditionalOnProperty(prefix = "app.portone", name = "stub-enabled", havingValue = "true")
public class StubIdentityVerificationClient implements IdentityVerificationClientPort {

	@Override
	public VerifiedIdentity fetchVerified(String identityVerificationId) {
		String phoneNumber = extractPhone(identityVerificationId);
		return new VerifiedIdentity(identityVerificationId, "VERIFIED", phoneNumber, "테스트사용자");
	}

	private String extractPhone(String identityVerificationId) {
		if (identityVerificationId == null) {
			return "01012345678";
		}
		int separator = identityVerificationId.lastIndexOf('-');
		if (separator >= 0) {
			String candidate = identityVerificationId.substring(separator + 1).replaceAll("[^0-9]", "");
			if (candidate.matches("01[016789]\\d{7,8}")) {
				return candidate;
			}
		}
		return "01012345678";
	}
}
