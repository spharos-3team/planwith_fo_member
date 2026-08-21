package com.planwith.planwith_fo_member.adapter.out.social;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.out.SocialOAuthClientPort;
import com.planwith.planwith_fo_member.domain.member.LoginType;

/**
 * 로컬/테스트용 스텁.
 * authorizationCode 형식: {@code stub:{socialId}:{email}}
 * 예: stub:google-123:user@example.com
 */
@Component
@ConditionalOnProperty(prefix = "app.social", name = "stub-enabled", havingValue = "true", matchIfMissing = true)
public class StubSocialOAuthClient implements SocialOAuthClientPort {

	@Override
	public SocialUserProfile fetchUser(LoginType provider, String authorizationCode, String redirectUri) {
		if (!StringUtils.hasText(authorizationCode) || !authorizationCode.startsWith("stub:")) {
			throw new BusinessException(
					ErrorCode.SOCIAL_AUTH_FAILED,
					"스텁 모드에서는 authorizationCode를 stub:{socialId}:{email} 형식으로 전달하세요."
			);
		}
		String payload = authorizationCode.substring("stub:".length());
		String[] parts = payload.split(":", 2);
		if (parts.length < 1 || !StringUtils.hasText(parts[0])) {
			throw new BusinessException(
					ErrorCode.SOCIAL_AUTH_FAILED,
					"스텁 authorizationCode 형식이 올바르지 않습니다. stub:{socialId}:{email}"
			);
		}
		String socialId = parts[0].trim();
		String email = parts.length < 2 || !StringUtils.hasText(parts[1]) ? null : parts[1].trim();
		return new SocialUserProfile(
				socialId,
				email,
				null,
				provider.name().toLowerCase() + "-user"
		);
	}
}
