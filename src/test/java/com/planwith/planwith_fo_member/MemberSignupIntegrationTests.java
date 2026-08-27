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

import com.jayway.jsonpath.JsonPath;

import com.planwith.planwith_fo_member.adapter.out.persistence.terms.TermsJpaEntity;
import com.planwith.planwith_fo_member.adapter.out.persistence.terms.TermsJpaRepository;
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryEmailVerificationStore;
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryPhoneVerificationStore;
import com.planwith.planwith_fo_member.application.port.out.EmailVerificationStorePort;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberSignupIntegrationTests {

	private static final UUID SERVICE_TERM = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID PRIVACY_TERM = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MARKETING_TERM = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final String DEFAULT_PHONE = "01012345678";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TermsJpaRepository termsJpaRepository;

	@Autowired
	private EmailVerificationStorePort emailVerificationStorePort;

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
	void preparePhoneVerificationReturnsSdkParams() throws Exception {
		mockMvc.perform(post("/api/v1/auth/phone-verifications"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.storeId").value("store-test"))
				.andExpect(jsonPath("$.data.channelKey").value("channel-key-test"))
				.andExpect(jsonPath("$.data.identityVerificationId").isNotEmpty());
	}

	@Test
	void confirmPhoneVerificationAcceptsStubPhoneAndName() throws Exception {
		mockMvc.perform(post("/api/v1/auth/phone-verifications/confirm")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "identityVerificationId": "identity-verification-stub-01055556666",
								  "phoneNumber": "01055556666",
								  "name": "홍길동"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.verified").value(true))
				.andExpect(jsonPath("$.data.phoneNumber").value("01055556666"))
				.andExpect(jsonPath("$.data.name").value("홍길동"));
	}

	@Test
	void prepareThenConfirmUsesStubPhoneAndName() throws Exception {
		String body = mockMvc.perform(post("/api/v1/auth/phone-verifications")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"phoneNumber": "01077778888", "name": "김플랜"}
								"""))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String identityVerificationId = JsonPath.read(body, "$.data.identityVerificationId");

		mockMvc.perform(post("/api/v1/auth/phone-verifications/confirm")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"identityVerificationId": "%s"}
								""".formatted(identityVerificationId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.phoneNumber").value("01077778888"))
				.andExpect(jsonPath("$.data.name").value("김플랜"));
	}

	@Test
	void signupSucceedsWithCustomVerifiedName() throws Exception {
		String email = "custom-name@example.com";
		String phone = "01055556666";
		verifyEmail(email);
		mockMvc.perform(post("/api/v1/auth/phone-verifications/confirm")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "identityVerificationId": "identity-verification-stub-%s",
								  "phoneNumber": "%s",
								  "name": "홍길동"
								}
								""".formatted(phone, phone)))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "Password1!",
								  "phoneNumber": "%s",
								  "name": "홍길동",
								  "nickname": "커스텀이름",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true}
								  ]
								}
								""".formatted(email, phone, SERVICE_TERM, PRIVACY_TERM)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.data.nickname").value("커스텀이름"));
	}

	@Test
	void signupSucceedsAfterEmailAndPhoneVerification() throws Exception {
		String email = "signup-ok@example.com";
		verifyEmail(email);
		verifyPhone(DEFAULT_PHONE);

		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content(signupBody(email, "플랜유저", DEFAULT_PHONE, true, true, false)))
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
						.content(signupBody("not-verified@example.com", "미인증", DEFAULT_PHONE, true, true, false)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.error.code").value("EMAIL_NOT_VERIFIED"));
	}

	@Test
	void signupFailsWhenPhoneNotVerified() throws Exception {
		String email = "phone-missing@example.com";
		verifyEmail(email);

		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content(signupBody(email, "폰미인증", DEFAULT_PHONE, true, true, false)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("PHONE_NOT_VERIFIED"));
	}

	@Test
	void signupFailsWhenEmailDuplicated() throws Exception {
		String email = "dup@example.com";
		verifyEmail(email);
		verifyPhone(DEFAULT_PHONE);
		signup(email, "닉네임1", DEFAULT_PHONE);

		emailVerificationStorePort.markVerified(email, java.time.Instant.now().plusSeconds(600));
		verifyPhone("01099998888");
		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content(signupBody(email, "닉네임2", "01099998888", true, true, false)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.error.code").value("EMAIL_ALREADY_EXISTS"));
	}

	@Test
	void sendEmailVerificationFailsWhenEmailAlreadyRegistered() throws Exception {
		String email = "exists@example.com";
		verifyEmail(email);
		verifyPhone(DEFAULT_PHONE);
		signup(email, "기존유저", DEFAULT_PHONE);

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
		verifyPhone(DEFAULT_PHONE);

		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content(signupBody(email, "약관미동의", DEFAULT_PHONE, true, false, false)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("REQUIRED_TERM_NOT_AGREED"));
	}

	@Test
	void signupFailsWhenNameDoesNotMatchVerified() throws Exception {
		String email = "name-mismatch@example.com";
		verifyEmail(email);
		verifyPhone(DEFAULT_PHONE);

		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "Password1!",
								  "phoneNumber": "%s",
								  "name": "다른이름",
								  "nickname": "이름불일치",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true}
								  ]
								}
								""".formatted(email, DEFAULT_PHONE, SERVICE_TERM, PRIVACY_TERM)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("NAME_MISMATCH"));
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
				.andExpect(jsonPath("$.data.verified").value(true))
				.andExpect(jsonPath("$.data.verifiedExpiresInSeconds").value(7200));
	}

	@Test
	void signupFailsWhenEmailVerificationExpired() throws Exception {
		String email = "expired-verified@example.com";
		emailVerificationStorePort.markVerified(email, java.time.Instant.now().minusSeconds(1));
		verifyPhone(DEFAULT_PHONE);

		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content(signupBody(email, "만료인증", DEFAULT_PHONE, true, true, false)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("EMAIL_NOT_VERIFIED"));
	}

	private void verifyPhone(String phoneNumber) throws Exception {
		String identityVerificationId = "identity-verification-stub-" + phoneNumber;
		mockMvc.perform(post("/api/v1/auth/phone-verifications/confirm")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"identityVerificationId": "%s"}
								""".formatted(identityVerificationId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.verified").value(true))
				.andExpect(jsonPath("$.data.phoneNumber").value(phoneNumber))
				.andExpect(jsonPath("$.data.name").value("테스트사용자"));
	}

	private void signup(String email, String nickname, String phoneNumber) throws Exception {
		mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content(signupBody(email, nickname, phoneNumber, true, true, false)))
				.andExpect(status().isCreated());
	}

	private String signupBody(
			String email,
			String nickname,
			String phoneNumber,
			boolean serviceAgreed,
			boolean privacyAgreed,
			boolean marketingAgreed
	) {
		return """
				{
				  "email": "%s",
				  "password": "Password1!",
				  "phoneNumber": "%s",
				  "name": "테스트사용자",
				  "nickname": "%s",
				  "profileImage": null,
				  "profileIntro": "hello",
				  "agreements": [
				    {"termUuid": "%s", "agreed": %s},
				    {"termUuid": "%s", "agreed": %s},
				    {"termUuid": "%s", "agreed": %s}
				  ]
				}
				""".formatted(
				email,
				phoneNumber,
				nickname,
				SERVICE_TERM, serviceAgreed,
				PRIVACY_TERM, privacyAgreed,
				MARKETING_TERM, marketingAgreed
		);
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
