package com.planwith.planwith_fo_member.adapter.out.social;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.out.SocialOAuthClientPort;
import com.planwith.planwith_fo_member.config.SocialOAuthProperties;
import com.planwith.planwith_fo_member.domain.member.LoginType;

@Component
@ConditionalOnProperty(prefix = "app.social", name = "stub-enabled", havingValue = "false")
public class RestSocialOAuthClient implements SocialOAuthClientPort {

	private static final Logger log = LoggerFactory.getLogger(RestSocialOAuthClient.class);

	private final SocialOAuthProperties properties;
	private final RestClient restClient;

	public RestSocialOAuthClient(SocialOAuthProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.create();
	}

	@Override
	public SocialUserProfile fetchUser(LoginType provider, String authorizationCode, String redirectUri) {
		if (!StringUtils.hasText(authorizationCode)) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "authorizationCode는 필수입니다.");
		}
		try {
			return switch (provider) {
				case GOOGLE -> fetchGoogle(authorizationCode, redirectUri);
				case KAKAO -> fetchKakao(authorizationCode, redirectUri);
				case NAVER -> fetchNaver(authorizationCode, redirectUri);
				default -> throw new BusinessException(ErrorCode.UNSUPPORTED_PROVIDER);
			};
		}
		catch (BusinessException exception) {
			throw exception;
		}
		catch (RestClientResponseException exception) {
			log.warn("Social OAuth provider call failed. provider={}, status={}", provider, exception.getStatusCode().value());
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
		catch (Exception exception) {
			log.warn("Social OAuth unexpected failure. provider={}", provider, exception);
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
	}

	private SocialUserProfile fetchGoogle(String code, String redirectUri) {
		SocialOAuthProperties.ProviderProperties google = requireProvider(properties.google(), "google");
		TokenResponse token = exchangeToken(
				google.tokenUri(),
				Map.of(
						"code", code,
						"client_id", google.clientId(),
						"client_secret", google.clientSecret(),
						"redirect_uri", nullToEmpty(redirectUri),
						"grant_type", "authorization_code"
				)
		);
		JsonNode user = restClient.get()
				.uri(google.userInfoUri())
				.header("Authorization", "Bearer " + token.accessToken())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class);
		if (user == null) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
		return new SocialUserProfile(
				text(user, "sub"),
				text(user, "email"),
				text(user, "picture"),
				text(user, "name")
		);
	}

	private SocialUserProfile fetchKakao(String code, String redirectUri) {
		SocialOAuthProperties.ProviderProperties kakao = requireProvider(properties.kakao(), "kakao");
		TokenResponse token = exchangeToken(
				kakao.tokenUri(),
				Map.of(
						"code", code,
						"client_id", kakao.clientId(),
						"client_secret", nullToEmpty(kakao.clientSecret()),
						"redirect_uri", nullToEmpty(redirectUri),
						"grant_type", "authorization_code"
				)
		);
		JsonNode user = restClient.get()
				.uri(kakao.userInfoUri())
				.header("Authorization", "Bearer " + token.accessToken())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class);
		if (user == null || user.get("id") == null) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
		JsonNode account = user.path("kakao_account");
		JsonNode profile = account.path("profile");
		return new SocialUserProfile(
				user.get("id").asText(),
				text(account, "email"),
				text(profile, "profile_image_url"),
				text(profile, "nickname")
		);
	}

	private SocialUserProfile fetchNaver(String code, String redirectUri) {
		SocialOAuthProperties.ProviderProperties naver = requireProvider(properties.naver(), "naver");
		TokenResponse token = exchangeToken(
				naver.tokenUri(),
				Map.of(
						"code", code,
						"client_id", naver.clientId(),
						"client_secret", naver.clientSecret(),
						"redirect_uri", nullToEmpty(redirectUri),
						"grant_type", "authorization_code",
						"state", "planwith"
				)
		);
		JsonNode body = restClient.get()
				.uri(naver.userInfoUri())
				.header("Authorization", "Bearer " + token.accessToken())
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class);
		if (body == null) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
		JsonNode response = body.path("response");
		return new SocialUserProfile(
				text(response, "id"),
				text(response, "email"),
				text(response, "profile_image"),
				text(response, "nickname")
		);
	}

	private TokenResponse exchangeToken(String tokenUri, Map<String, String> form) {
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		form.forEach((key, value) -> {
			if (StringUtils.hasText(value)) {
				body.add(key, value);
			}
		});
		TokenResponse token = restClient.post()
				.uri(tokenUri)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(body)
				.retrieve()
				.body(TokenResponse.class);
		if (token == null || !StringUtils.hasText(token.accessToken())) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
		return token;
	}

	private SocialOAuthProperties.ProviderProperties requireProvider(
			SocialOAuthProperties.ProviderProperties provider,
			String name
	) {
		if (provider == null || !StringUtils.hasText(provider.clientId()) || !StringUtils.hasText(provider.tokenUri())) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED, name + " OAuth 설정이 누락되었습니다.");
		}
		return provider;
	}

	private String text(JsonNode node, String field) {
		JsonNode value = node.get(field);
		return value == null || value.isNull() ? null : value.asText();
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record TokenResponse(
			@com.fasterxml.jackson.annotation.JsonProperty("access_token") String accessToken
	) {
	}
}
