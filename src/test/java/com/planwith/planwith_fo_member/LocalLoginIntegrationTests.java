package com.planwith.planwith_fo_member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.planwith.planwith_fo_member.adapter.out.persistence.terms.TermsJpaEntity;
import com.planwith.planwith_fo_member.adapter.out.persistence.terms.TermsJpaRepository;
import com.planwith.planwith_fo_member.adapter.out.token.InMemoryRefreshTokenStore;
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryEmailVerificationStore;
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryPhoneVerificationStore;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocalLoginIntegrationTests {

	private static final UUID SERVICE_TERM = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID PRIVACY_TERM = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final String PHONE = "01012345678";
	private static final String PASSWORD = "Password1!";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TermsJpaRepository termsJpaRepository;

	@Autowired
	private InMemoryEmailVerificationStore emailVerificationStore;

	@Autowired
	private InMemoryPhoneVerificationStore phoneVerificationStore;

	@Autowired
	private InMemoryRefreshTokenStore refreshTokenStore;

	@BeforeEach
	void setUp() {
		emailVerificationStore.clearAll();
		phoneVerificationStore.clearAll();
		refreshTokenStore.clearAll();
		termsJpaRepository.deleteAll();
		saveTerm(SERVICE_TERM, "서비스 이용약관", "SERVICE", true);
		saveTerm(PRIVACY_TERM, "개인정보 처리방침", "PRIVACY", true);
	}

	@Test
	void loginSucceedsAndSetsRefreshCookie() throws Exception {
		String email = "login-ok@example.com";
		signup(email, "로그인유저");

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"%s"}
								""".formatted(email, PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.data.accessTokenExpiresIn").value(900))
				.andExpect(jsonPath("$.data.user.userId").isNotEmpty())
				.andExpect(jsonPath("$.data.user.roles[0]").value("USER"))
				.andExpect(cookie().exists("refresh_token"))
				.andExpect(cookie().httpOnly("refresh_token", true));
	}

	@Test
	void loginFailsWithWrongPassword() throws Exception {
		String email = "login-bad@example.com";
		signup(email, "비번틀림");

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"wrong-password"}
								""".formatted(email)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
	}

	@Test
	void refreshRotatesTokenAndReissueAliasWorks() throws Exception {
		String email = "refresh-ok@example.com";
		signup(email, "리프레시유저");
		Cookie first = loginCookie(email);

		MvcResult refreshResult = mockMvc.perform(post("/api/v1/auth/refresh").cookie(first))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
				.andExpect(cookie().exists("refresh_token"))
				.andReturn();
		Cookie rotated = refreshResult.getResponse().getCookie("refresh_token");

		mockMvc.perform(post("/api/v1/auth/reissue").cookie(rotated))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty());

		mockMvc.perform(post("/api/v1/auth/refresh").cookie(first))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error.code").value("INVALID_REFRESH_TOKEN"));
	}

	@Test
	void logoutRevokesRefreshToken() throws Exception {
		String email = "logout-ok@example.com";
		signup(email, "로그아웃유저");
		Cookie cookie = loginCookie(email);

		mockMvc.perform(post("/api/v1/auth/logout").cookie(cookie))
				.andExpect(status().isNoContent())
				.andExpect(cookie().maxAge("refresh_token", 0));

		mockMvc.perform(post("/api/v1/auth/refresh").cookie(cookie))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void jwksReturnsPublicKey() throws Exception {
		mockMvc.perform(get("/oauth2/jwks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.keys").isArray())
				.andExpect(jsonPath("$.keys[0].kty").value("RSA"))
				.andExpect(jsonPath("$.keys[0].kid").value("test-key"));
	}

	private Cookie loginCookie(String email) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"%s"}
								""".formatted(email, PASSWORD)))
				.andExpect(status().isOk())
				.andReturn();
		return result.getResponse().getCookie("refresh_token");
	}

	private void signup(String email, String nickname) throws Exception {
		verifyEmail(email);
		verifyPhone(PHONE);
		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s",
								  "phoneNumber": "%s",
								  "nickname": "%s",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true}
								  ]
								}
								""".formatted(email, PASSWORD, PHONE, nickname, SERVICE_TERM, PRIVACY_TERM)))
				.andExpect(status().isCreated());
	}

	private void verifyEmail(String email) throws Exception {
		mockMvc.perform(post("/api/v1/auth/email-verifications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\": \"%s\"}".formatted(email)))
				.andExpect(status().isOk());
		String code = emailVerificationStore.findCode(email)
				.orElseThrow()
				.code();
		mockMvc.perform(post("/api/v1/auth/email-verifications/confirm")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\": \"%s\", \"code\": \"%s\"}".formatted(email, code)))
				.andExpect(status().isOk());
	}

	private void verifyPhone(String phoneNumber) throws Exception {
		mockMvc.perform(post("/api/v1/auth/phone-verifications/confirm")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"identityVerificationId\": \"identity-verification-stub-%s\"}".formatted(phoneNumber)))
				.andExpect(status().isOk());
	}

	private void saveTerm(UUID termUuid, String title, String termType, boolean required) {
		TermsJpaEntity entity = new TermsJpaEntity();
		entity.setTermUuid(termUuid.toString());
		entity.setTitle(title);
		entity.setTermType(termType);
		entity.setVersion("1.0");
		entity.setContent(title);
		entity.setRequired(required);
		entity.setActive(true);
		termsJpaRepository.save(entity);
	}
}
