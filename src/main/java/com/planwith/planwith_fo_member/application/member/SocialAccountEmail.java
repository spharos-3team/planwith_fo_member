package com.planwith.planwith_fo_member.application.member;

import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.domain.member.LoginType;

/**
 * 소셜 계정의 고유 식별자는 provider + socialId 이다.
 * 네이버 이메일은 계정 고유값이 아니라 이용자가 등록한 연락처 이메일이라
 * 없거나 다른 회원과 겹쳐도 가입을 막지 않는다.
 */
final class SocialAccountEmail {

	private SocialAccountEmail() {
	}

	static String resolve(LoginType provider, String socialId, String providerEmail, boolean emailTaken) {
		String provided = providerEmail == null ? null : providerEmail.trim().toLowerCase();
		if (StringUtils.hasText(provided) && !emailTaken) {
			return provided;
		}
		return synthetic(provider, socialId);
	}

	static String synthetic(LoginType provider, String socialId) {
		String raw = socialId == null ? "" : socialId.trim();
		String safeId = raw.replaceAll("[^a-zA-Z0-9._-]", "");
		if (!StringUtils.hasText(safeId)) {
			safeId = Integer.toHexString(raw.hashCode());
		}
		return "social." + provider.name().toLowerCase() + "." + safeId + "@users.planwith";
	}
}
