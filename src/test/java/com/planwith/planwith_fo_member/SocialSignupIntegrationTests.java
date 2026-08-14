package com.planwith.planwith_fo_member;

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
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryEmailVerificationStore;
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryPhoneVerificationStore;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SocialSignupIntegrationTests {

	private static final UUID SERVICE_TERM = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID PRIVACY_TERM = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final String PHONE = "01012345678";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TermsJpaRepository termsJpaRepository;

	@Autowired
	private InMemoryEmailVerificationStore emailVerificationStore;

	@Autowired
	private InMemoryPhoneVerificationStore phoneVerificationStore;

	@BeforeEach
	void setUp() {
		emailVerificationStore.clearAll();
		phoneVerificationStore.clearAll();
		termsJpaRepository.deleteAll();
		saveTerm(SERVICE_TERM, "서비스 이용약관", "SERVICE", true);
		saveTerm(PRIVACY_TERM, "개인정보 처리방침", "PRIVACY", true);
	}

	@Test
	void socialSignupSucceedsWithStubCode() throws Exception {
		verifyPhone(PHONE);

		mockMvc.perform(post("/api/v1/auth/google/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content(signupBody("stub:google-1001:social1@example.com", "소셜유저1")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.email").value("social1@example.com"))
				.andExpect(jsonPath("$.data.nickname").value("소셜유저1"))
				.andExpect(jsonPath("$.data.memberUuid").isNotEmpty())
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty())
				.andExpect(cookie().exists("refresh_token"));
	}

	@Test
	void socialSignupFailsWhenPhoneNotVerified() throws Exception {
		mockMvc.perform(post("/api/v1/auth/kakao/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content(signupBody("stub:kakao-1:kakao@example.com", "카카오유저")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("PHONE_NOT_VERIFIED"));
	}

	@Test
	void socialSignupFailsWhenSocialAccountDuplicated() throws Exception {
		verifyPhone(PHONE);
		mockMvc.perform(post("/api/v1/auth/naver/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content(signupBody("stub:naver-9:naver1@example.com", "네이버1")))
				.andExpect(status().isCreated());

		verifyPhone("01099998888");
		mockMvc.perform(post("/api/v1/auth/naver/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "authorizationCode": "stub:naver-9:naver2@example.com",
								  "nickname": "네이버2",
								  "phoneNumber": "01099998888",
								  "name": "테스트사용자",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true}
								  ]
								}
								""".formatted(SERVICE_TERM, PRIVACY_TERM)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("SOCIAL_ACCOUNT_ALREADY_EXISTS"));
	}

	@Test
	void socialSignupRejectsUnsupportedProvider() throws Exception {
		verifyPhone(PHONE);
		mockMvc.perform(post("/api/v1/auth/facebook/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content(signupBody("stub:fb-1:fb@example.com", "페북유저")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("UNSUPPORTED_PROVIDER"));
	}

	private void verifyPhone(String phoneNumber) throws Exception {
		mockMvc.perform(post("/api/v1/auth/phone-verifications/confirm")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"identityVerificationId\": \"identity-verification-stub-%s\"}".formatted(phoneNumber)))
				.andExpect(status().isOk());
	}

	private String signupBody(String authorizationCode, String nickname) {
		return """
				{
				  "authorizationCode": "%s",
				  "nickname": "%s",
				  "phoneNumber": "%s",
				  "name": "테스트사용자",
				  "profileIntro": "hello",
				  "agreements": [
				    {"termUuid": "%s", "agreed": true},
				    {"termUuid": "%s", "agreed": true}
				  ]
				}
				""".formatted(authorizationCode, nickname, PHONE, SERVICE_TERM, PRIVACY_TERM);
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
