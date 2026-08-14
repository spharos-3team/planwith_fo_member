package com.planwith.planwith_fo_member;

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

import com.planwith.planwith_fo_member.adapter.out.persistence.member.MemberJpaRepository;
import com.planwith.planwith_fo_member.adapter.out.persistence.member.MemberProfileJpaRepository;
import com.planwith.planwith_fo_member.adapter.out.persistence.terms.TermsJpaEntity;
import com.planwith.planwith_fo_member.adapter.out.persistence.terms.TermsJpaRepository;
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryEmailVerificationStore;
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryPasswordResetStore;
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryPhoneVerificationStore;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountRecoveryIntegrationTests {

	private static final UUID SERVICE_TERM = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID PRIVACY_TERM = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final String PHONE = "01012345678";
	private static final String PASSWORD = "Password1!";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TermsJpaRepository termsJpaRepository;

	@Autowired
	private MemberJpaRepository memberJpaRepository;

	@Autowired
	private MemberProfileJpaRepository memberProfileJpaRepository;

	@Autowired
	private InMemoryEmailVerificationStore emailVerificationStore;

	@Autowired
	private InMemoryPhoneVerificationStore phoneVerificationStore;

	@Autowired
	private InMemoryPasswordResetStore passwordResetStore;

	@BeforeEach
	void setUp() {
		emailVerificationStore.clearAll();
		phoneVerificationStore.clearAll();
		passwordResetStore.clearAll();
		memberProfileJpaRepository.deleteAll();
		memberJpaRepository.deleteAll();
		termsJpaRepository.deleteAll();
		saveTerm(SERVICE_TERM, "서비스 이용약관", "SERVICE", true);
		saveTerm(PRIVACY_TERM, "개인정보 처리방침", "PRIVACY", true);
	}

	@Test
	void findEmailReturnsRegisteredEmailAfterPhoneVerification() throws Exception {
		String email = "find-me@example.com";
		signupLocal(email, "찾기유저", PHONE);

		verifyPhone(PHONE);
		mockMvc.perform(post("/api/v1/auth/find-email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"phoneNumber\":\"%s\"}".formatted(PHONE)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(email))
				.andExpect(jsonPath("$.data.maskedEmail").value("f***@example.com"))
				.andExpect(jsonPath("$.data.loginType").value("LOCAL"));
	}

	@Test
	void findEmailFailsWhenPhoneNotVerified() throws Exception {
		mockMvc.perform(post("/api/v1/auth/find-email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"phoneNumber\":\"01000000000\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("PHONE_NOT_VERIFIED"));
	}

	@Test
	void passwordResetWorksForLocalMember() throws Exception {
		String email = "reset-ok@example.com";
		signupLocal(email, "리셋유저", "01022223333");

		mockMvc.perform(post("/api/v1/auth/password/reset-requests")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"%s\"}".formatted(email)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(email));

		String code = passwordResetStore.findCode(email).orElseThrow().code();
		mockMvc.perform(post("/api/v1/auth/password/reset")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","code":"%s","newPassword":"NewPass1!"}
								""".formatted(email, code)))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"NewPass1!"}
								""".formatted(email)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty());
	}

	@Test
	void passwordResetRejectedForSocialMember() throws Exception {
		verifyPhone(PHONE);
		mockMvc.perform(post("/api/v1/auth/kakao/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "authorizationCode": "stub:kakao-reset:social-reset@example.com",
								  "nickname": "소셜리셋",
								  "phoneNumber": "%s",
								  "name": "테스트사용자",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true}
								  ]
								}
								""".formatted(PHONE, SERVICE_TERM, PRIVACY_TERM)))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/v1/auth/password/reset-requests")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"social-reset@example.com\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("PASSWORD_RESET_NOT_ALLOWED_FOR_SOCIAL"));
	}

	@Test
	void findEmailReturnsSocialLoginType() throws Exception {
		String phone = "01077778888";
		verifyPhone(phone);
		mockMvc.perform(post("/api/v1/auth/google/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "authorizationCode": "stub:google-find:social-find@example.com",
								  "nickname": "소셜찾기",
								  "phoneNumber": "%s",
								  "name": "테스트사용자",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true}
								  ]
								}
								""".formatted(phone, SERVICE_TERM, PRIVACY_TERM)))
				.andExpect(status().isCreated());

		verifyPhone(phone);
		mockMvc.perform(post("/api/v1/auth/find-email")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"phoneNumber\":\"%s\"}".formatted(phone)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value("social-find@example.com"))
				.andExpect(jsonPath("$.data.loginType").value("GOOGLE"));
	}

	private void signupLocal(String email, String nickname, String phone) throws Exception {
		verifyEmail(email);
		verifyPhone(phone);
		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s",
								  "phoneNumber": "%s",
								  "name": "테스트사용자",
								  "nickname": "%s",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true}
								  ]
								}
								""".formatted(email, PASSWORD, phone, nickname, SERVICE_TERM, PRIVACY_TERM)))
				.andExpect(status().isCreated());
	}

	private void verifyEmail(String email) throws Exception {
		mockMvc.perform(post("/api/v1/auth/email-verifications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\": \"%s\"}".formatted(email)))
				.andExpect(status().isOk());
		String code = emailVerificationStore.findCode(email).orElseThrow().code();
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
