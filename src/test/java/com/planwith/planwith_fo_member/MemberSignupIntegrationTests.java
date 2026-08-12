package com.planwith.planwith_fo_member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.planwith.planwith_fo_member.application.port.out.EmailVerificationStorePort;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberSignupIntegrationTests {

	private static final UUID SERVICE_TERM = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID PRIVACY_TERM = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MARKETING_TERM = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TermsJpaRepository termsJpaRepository;

	@Autowired
	private EmailVerificationStorePort emailVerificationStorePort;

	@BeforeEach
	void setUp() {
		termsJpaRepository.deleteAll();
		saveTerm(SERVICE_TERM, "서비스 이용약관", "SERVICE", true);
		saveTerm(PRIVACY_TERM, "개인정보 처리방침", "PRIVACY", true);
		saveTerm(MARKETING_TERM, "마케팅 정보 수신 동의", "MARKETING", false);
	}

	@Test
	void listTermsReturnsActiveTerms() throws Exception {
		mockMvc.perform(get("/api/v1/terms"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.length()").value(3))
				.andExpect(jsonPath("$.data[0].isRequired").value(true));
	}

	@Test
	void signupSucceedsAfterEmailVerification() throws Exception {
		String email = "signup-ok@example.com";
		verifyEmail(email);

		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "Password1!",
								  "phoneNumber": "01012345678",
								  "nickname": "플랜유저",
								  "profileImage": null,
								  "profileIntro": "hello",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": false}
								  ]
								}
								""".formatted(email, SERVICE_TERM, PRIVACY_TERM, MARKETING_TERM)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.email").value(email))
				.andExpect(jsonPath("$.data.nickname").value("플랜유저"))
				.andExpect(jsonPath("$.data.memberUuid").isNotEmpty())
				.andExpect(jsonPath("$.data.createdAt").isNotEmpty());
	}

	@Test
	void signupFailsWhenEmailNotVerified() throws Exception {
		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "not-verified@example.com",
								  "password": "Password1!",
								  "nickname": "미인증",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true}
								  ]
								}
								""".formatted(SERVICE_TERM, PRIVACY_TERM)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("EMAIL_NOT_VERIFIED"));
	}

	@Test
	void signupFailsWhenEmailDuplicated() throws Exception {
		String email = "dup@example.com";
		verifyEmail(email);
		signup(email, "닉네임1");

		emailVerificationStorePort.markVerified(email, java.time.Instant.now().plusSeconds(600));
		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "Password1!",
								  "nickname": "닉네임2",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true}
								  ]
								}
								""".formatted(email, SERVICE_TERM, PRIVACY_TERM)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"));
	}

	@Test
	void sendEmailVerificationFailsWhenEmailAlreadyRegistered() throws Exception {
		String email = "exists@example.com";
		verifyEmail(email);
		signup(email, "기존유저");

		mockMvc.perform(post("/api/v1/auth/email-verifications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\": \"%s\"}".formatted(email)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"));
	}

	@Test
	void signupFailsWhenRequiredTermNotAgreed() throws Exception {
		String email = "terms@example.com";
		verifyEmail(email);

		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "Password1!",
								  "nickname": "약관미동의",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": false}
								  ]
								}
								""".formatted(email, SERVICE_TERM, PRIVACY_TERM)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("REQUIRED_TERM_NOT_AGREED"));
	}

	@Test
	void emailVerificationConfirmRejectsInvalidCode() throws Exception {
		mockMvc.perform(post("/api/v1/auth/email-verifications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "code@example.com"}
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/auth/email-verifications/confirm")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "code@example.com", "code": "000000"}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("EMAIL_VERIFICATION_INVALID"));
	}

	private void verifyEmail(String email) throws Exception {
		mockMvc.perform(post("/api/v1/auth/email-verifications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\": \"%s\"}".formatted(email)))
				.andExpect(status().isOk());

		String code = emailVerificationStorePort.findCode(email)
				.orElseThrow()
				.code();

		mockMvc.perform(post("/api/v1/auth/email-verifications/confirm")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email": "%s", "code": "%s"}
								""".formatted(email, code)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.verified").value(true));
	}

	private void signup(String email, String nickname) throws Exception {
		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "Password1!",
								  "nickname": "%s",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true}
								  ]
								}
								""".formatted(email, nickname, SERVICE_TERM, PRIVACY_TERM)))
				.andExpect(status().isCreated());
	}

	private void saveTerm(UUID termUuid, String title, String termType, boolean required) {
		TermsJpaEntity entity = new TermsJpaEntity();
		entity.setTermUuid(termUuid.toString());
		entity.setTitle(title);
		entity.setTermType(termType);
		entity.setVersion("1.0");
		entity.setContent(title + " 내용");
		entity.setRequired(required);
		entity.setActive(true);
		termsJpaRepository.save(entity);
	}
}
