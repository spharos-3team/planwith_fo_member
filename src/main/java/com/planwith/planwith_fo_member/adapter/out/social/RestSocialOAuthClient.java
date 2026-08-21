package com.planwith.planwith_fo_member.adapter.out.social;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

import com.planwith.planwith_fo_member.application.exception.BusinessException;
import com.planwith.planwith_fo_member.application.exception.ErrorCode;
import com.planwith.planwith_fo_member.application.port.out.SocialOAuthClientPort;
import com.planwith.planwith_fo_member.config.SocialOAuthProperties;
import com.planwith.planwith_fo_member.domain.member.LoginType;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
@ConditionalOnProperty(prefix = "app.social", name = "stub-enabled", havingValue = "false")
public class RestSocialOAuthClient implements SocialOAuthClientPort {

	private static final Logger log = LoggerFactory.getLogger(RestSocialOAuthClient.class);
	private static final int ERROR_BODY_LOG_LIMIT = 500;
	private static final int ERROR_DETAIL_LIMIT = 120;

	private final SocialOAuthProperties properties;
	private final RestClient restClient;
	private final JsonMapper jsonMapper = JsonMapper.builder().build();
	private final SocialAuthorizationCodeCache authorizationCodeCache = new SocialAuthorizationCodeCache();
	private final ConcurrentHashMap<String, Object> exchangeLocks = new ConcurrentHashMap<>();

	public RestSocialOAuthClient(SocialOAuthProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.builder().build();
	}

	@Override
	public SocialUserProfile fetchUser(LoginType provider, String authorizationCode, String redirectUri) {
		return fetchUser(provider, authorizationCode, redirectUri, null);
	}

	@Override
	public SocialUserProfile fetchUser(
			LoginType provider,
			String authorizationCode,
			String redirectUri,
			String oauthState
	) {
		if (!StringUtils.hasText(authorizationCode)) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST, "authorizationCode는 필수입니다.");
		}
		Object lock = exchangeLocks.computeIfAbsent(provider + ":" + authorizationCode.hashCode(), key -> new Object());
		synchronized (lock) {
			SocialUserProfile cached = authorizationCodeCache.get(provider, authorizationCode);
			if (cached != null) {
				return cached;
			}
			SocialUserProfile profile = fetchFromProvider(provider, authorizationCode, redirectUri, oauthState);
			authorizationCodeCache.put(provider, authorizationCode, profile);
			return profile;
		}
	}

	private SocialUserProfile fetchFromProvider(
			LoginType provider,
			String authorizationCode,
			String redirectUri,
			String oauthState
	) {
		try {
			return switch (provider) {
				case GOOGLE -> fetchGoogle(authorizationCode, redirectUri);
				case KAKAO -> fetchKakao(authorizationCode, redirectUri);
				case NAVER -> fetchNaver(authorizationCode, redirectUri, oauthState);
				default -> throw new BusinessException(ErrorCode.UNSUPPORTED_PROVIDER);
			};
		}
		catch (BusinessException exception) {
			throw exception;
		}
		catch (RestClientResponseException exception) {
			String body = truncate(exception.getResponseBodyAsString());
			log.warn(
					"Social OAuth provider call failed. provider={}, redirectUri={}, status={}, body={}",
					provider,
					redirectUri,
					exception.getStatusCode().value(),
					body
			);
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED, providerErrorMessage(body));
		}
		catch (Exception exception) {
			log.warn("Social OAuth unexpected failure. provider={}, redirectUri={}", provider, redirectUri, exception);
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
	}

	private SocialUserProfile fetchGoogle(String code, String redirectUri) {
		SocialOAuthProperties.ProviderProperties google = requireProvider(properties.google(), "google");
		if (!StringUtils.hasText(google.clientSecret())) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED, "google OAuth 설정이 누락되었습니다.");
		}
		String accessToken = exchangeToken(
				google.tokenUri(),
				tokenForm(
						code,
						google.clientId(),
						google.clientSecret(),
						redirectUri,
						null
				)
		);
		JsonNode user = getJson(google.userInfoUri(), accessToken);
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
		String accessToken = exchangeToken(
				kakao.tokenUri(),
				tokenForm(
						code,
						kakao.clientId(),
						kakao.clientSecret(),
						redirectUri,
						null
				)
		);
		JsonNode user = getJson(kakao.userInfoUri(), accessToken);
		if (user == null || !StringUtils.hasText(text(user, "id"))) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED);
		}
		JsonNode account = user.path("kakao_account");
		JsonNode profile = account.path("profile");
		return new SocialUserProfile(
				text(user, "id"),
				text(account, "email"),
				text(profile, "profile_image_url"),
				text(profile, "nickname")
		);
	}

	private SocialUserProfile fetchNaver(String code, String redirectUri, String oauthState) {
		SocialOAuthProperties.ProviderProperties naver = requireProvider(properties.naver(), "naver");
		if (!StringUtils.hasText(naver.clientSecret())) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED, "naver OAuth 설정이 누락되었습니다.");
		}
		String accessToken = exchangeToken(
				naver.tokenUri(),
				tokenForm(
						code,
						naver.clientId(),
						naver.clientSecret(),
						redirectUri,
						oauthState
				)
		);
		JsonNode body = getJson(naver.userInfoUri(), accessToken);
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

	private Map<String, String> tokenForm(
			String code,
			String clientId,
			String clientSecret,
			String redirectUri,
			String oauthState
	) {
		Map<String, String> form = new LinkedHashMap<>();
		form.put("code", code);
		form.put("client_id", clientId);
		form.put("client_secret", nullToEmpty(clientSecret));
		form.put("redirect_uri", nullToEmpty(redirectUri));
		form.put("grant_type", "authorization_code");
		form.put("state", nullToEmpty(oauthState));
		return form;
	}

	private String exchangeToken(String tokenUri, Map<String, String> form) {
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		form.forEach((key, value) -> {
			if (StringUtils.hasText(value)) {
				body.add(key, value);
			}
		});
		JsonNode token = readJson(
				restClient.post()
						.uri(tokenUri)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED)
						.body(body)
						.retrieve()
		);
		String accessToken = token == null ? null : text(token, "access_token");
		if (!StringUtils.hasText(accessToken)) {
			throw new BusinessException(ErrorCode.SOCIAL_AUTH_FAILED, "소셜 토큰 발급에 실패했습니다.");
		}
		return accessToken;
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

	private String providerErrorMessage(String body) {
		String detail = extractProviderError(body);
		if (!StringUtils.hasText(detail)) {
			return ErrorCode.SOCIAL_AUTH_FAILED.message();
		}
		return ErrorCode.SOCIAL_AUTH_FAILED.message() + " (" + detail + ")";
	}

	private String extractProviderError(String body) {
		if (!StringUtils.hasText(body)) {
			return "";
		}
		try {
			JsonNode node = jsonMapper.readTree(body);
			String error = text(node, "error");
			String description = text(node, "error_description");
			if (!StringUtils.hasText(description)) {
				description = text(node, "msg");
			}
			if (!StringUtils.hasText(error) && !StringUtils.hasText(description)) {
				return "";
			}
			if (!StringUtils.hasText(description)) {
				return shorten(error);
			}
			if (!StringUtils.hasText(error)) {
				return shorten(description);
			}
			return shorten(error + ": " + description);
		}
		catch (Exception exception) {
			return "";
		}
	}

	private JsonNode getJson(String uri, String accessToken) {
		return readJson(
				restClient.get()
						.uri(uri)
						.header("Authorization", "Bearer " + accessToken)
						.accept(MediaType.APPLICATION_JSON)
						.retrieve()
		);
	}

	private JsonNode readJson(RestClient.ResponseSpec spec) {
		String raw = spec.body(String.class);
		if (!StringUtils.hasText(raw)) {
			return null;
		}
		return jsonMapper.readTree(raw);
	}

	private String text(JsonNode node, String field) {
		if (node == null) {
			return null;
		}
		JsonNode value = node.get(field);
		if (value == null || value.isNull() || value.isMissingNode()) {
			return null;
		}
		String text = value.asString();
		return StringUtils.hasText(text) ? text : null;
	}

	private String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	private String truncate(String value) {
		if (!StringUtils.hasText(value)) {
			return "";
		}
		String trimmed = value.replaceAll("\\s+", " ").trim();
		if (trimmed.length() <= ERROR_BODY_LOG_LIMIT) {
			return trimmed;
		}
		return trimmed.substring(0, ERROR_BODY_LOG_LIMIT);
	}

	private String shorten(String value) {
		if (value.length() <= ERROR_DETAIL_LIMIT) {
			return value;
		}
		return value.substring(0, ERROR_DETAIL_LIMIT);
	}
}
