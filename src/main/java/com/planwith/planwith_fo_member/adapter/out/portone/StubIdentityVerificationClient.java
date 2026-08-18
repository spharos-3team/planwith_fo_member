package com.planwith.planwith_fo_member.adapter.out.portone;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.application.port.out.IdentityVerificationClientPort;
import com.planwith.planwith_fo_member.application.port.out.StubIdentityDraftPort;

/**
 * 테스트/로컬용 스텁.
 * <ul>
 *   <li>confirm의 phoneNumber/name이 있으면 그 값을 사용</li>
 *   <li>prepare에서 기억한 값이 있으면 사용</li>
 *   <li>identityVerificationId가 {@code ...-010XXXXXXXX} 형태면 해당 번호, 실명은 기본 {@code 테스트사용자}</li>
 * </ul>
 */
@Component
@ConditionalOnProperty(prefix = "app.portone", name = "stub-enabled", havingValue = "true")
public class StubIdentityVerificationClient implements IdentityVerificationClientPort, StubIdentityDraftPort {

	private static final String DEFAULT_PHONE = "01012345678";
	private static final String DEFAULT_NAME = "테스트사용자";

	private final ConcurrentHashMap<String, Draft> drafts = new ConcurrentHashMap<>();

	@Override
	public void remember(String identityVerificationId, String phoneNumber, String name) {
		if (!StringUtils.hasText(identityVerificationId)) {
			return;
		}
		if (!StringUtils.hasText(phoneNumber) && !StringUtils.hasText(name)) {
			return;
		}
		drafts.put(identityVerificationId.trim(), new Draft(trimToNull(phoneNumber), trimToNull(name)));
	}

	@Override
	public VerifiedIdentity fetchVerified(String identityVerificationId) {
		return fetchVerified(identityVerificationId, null, null);
	}

	@Override
	public VerifiedIdentity fetchVerified(String identityVerificationId, String stubPhoneNumber, String stubName) {
		Draft draft = identityVerificationId == null ? null : drafts.get(identityVerificationId);
		String phoneNumber = firstNonBlank(
				stubPhoneNumber,
				draft == null ? null : draft.phoneNumber(),
				extractPhone(identityVerificationId)
		);
		String name = firstNonBlank(
				stubName,
				draft == null ? null : draft.name(),
				DEFAULT_NAME
		);
		return new VerifiedIdentity(identityVerificationId, "VERIFIED", phoneNumber, name);
	}

	private String extractPhone(String identityVerificationId) {
		if (identityVerificationId == null) {
			return DEFAULT_PHONE;
		}
		int separator = identityVerificationId.lastIndexOf('-');
		if (separator >= 0) {
			String candidate = identityVerificationId.substring(separator + 1).replaceAll("[^0-9]", "");
			if (candidate.matches("01[016789]\\d{7,8}")) {
				return candidate;
			}
		}
		return DEFAULT_PHONE;
	}

	private static String firstNonBlank(String... values) {
		if (values == null) {
			return null;
		}
		for (String value : values) {
			if (StringUtils.hasText(value)) {
				return value.trim();
			}
		}
		return null;
	}

	private static String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private record Draft(String phoneNumber, String name) {
	}
}
