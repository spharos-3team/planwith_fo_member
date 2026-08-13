package com.planwith.planwith_fo_member;

import static org.hamcrest.Matchers.nullValue;
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

import com.planwith.planwith_fo_member.adapter.out.persistence.terms.TermsJpaEntity;
import com.planwith.planwith_fo_member.adapter.out.persistence.terms.TermsJpaRepository;
import com.planwith.planwith_fo_member.adapter.out.token.InMemoryRefreshTokenStore;
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryPhoneVerificationStore;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SocialLoginIntegrationTests {

	private static final UUID SERVICE_TERM = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID PRIVACY_TERM = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final String PHONE = "01012345678";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TermsJpaRepository termsJpaRepository;

	@Autowired
	private InMemoryPhoneVerificationStore phoneVerificationStore;

	@Autowired
	private InMemoryRefreshTokenStore refreshTokenStore;

	@BeforeEach
	void setUp() {
		phoneVerificationStore.clearAll();
		refreshTokenStore.clearAll();
		termsJpaRepository.deleteAll();
		saveTerm(SERVICE_TERM, "서비스 이용약관", "SERVICE", true);
		saveTerm(PRIVACY_TERM, "개인정보 처리방침", "PRIVACY", true);
	}

	@Test
	void socialLoginReturnsNewMemberWhenNotRegistered() throws Exception {
		mockMvc.perform(post("/api/v1/auth/google/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"authorizationCode":"stub:google-new:new@example.com"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.isNewMember").value(true))
				.andExpect(jsonPath("$.data.accessToken").value(nullValue()))
				.andExpect(cookie().doesNotExist("refresh_token"));
	}

	@Test
	void socialLoginIssuesTokensForExistingMember() throws Exception {
		verifyPhone(PHONE);
		mockMvc.perform(post("/api/v1/auth/kakao/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "authorizationCode": "stub:kakao-login-1:kakao-login@example.com",
								  "nickname": "카카오닉",
								  "phoneNumber": "%s",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true}
								  ]
								}
								""".formatted(PHONE, SERVICE_TERM, PRIVACY_TERM)))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/auth/kakao/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"authorizationCode":"stub:kakao-login-1:kakao-login@example.com"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.isNewMember").value(false))
				.andExpect(jsonPath("$.data.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.data.user.userId").isNotEmpty())
				.andExpect(cookie().exists("refresh_token"))
				.andExpect(cookie().httpOnly("refresh_token", true));
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
