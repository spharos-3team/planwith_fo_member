package com.planwith.planwith_fo_member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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

import com.planwith.planwith_fo_member.adapter.out.persistence.auth.LoginHistoryJpaEntity;
import com.planwith.planwith_fo_member.adapter.out.persistence.auth.LoginHistoryJpaRepository;
import com.planwith.planwith_fo_member.adapter.out.persistence.member.MemberJpaEntity;
import com.planwith.planwith_fo_member.adapter.out.persistence.member.MemberJpaRepository;
import com.planwith.planwith_fo_member.adapter.out.persistence.terms.TermsJpaEntity;
import com.planwith.planwith_fo_member.adapter.out.persistence.terms.TermsJpaRepository;
import com.planwith.planwith_fo_member.adapter.out.token.InMemoryRefreshTokenStore;
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryEmailVerificationStore;
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryPhoneVerificationStore;
import com.planwith.planwith_fo_member.domain.auth.ActorType;
import com.planwith.planwith_fo_member.domain.auth.DeviceInfo;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginHistoryIntegrationTests {

	private static final UUID SERVICE_TERM = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID PRIVACY_TERM = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final String PHONE = "01012345678";
	private static final String PASSWORD = "Password1!";
	private static final String IPHONE_UA = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TermsJpaRepository termsJpaRepository;

	@Autowired
	private MemberJpaRepository memberJpaRepository;

	@Autowired
	private LoginHistoryJpaRepository loginHistoryJpaRepository;

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
		loginHistoryJpaRepository.deleteAll();
		termsJpaRepository.deleteAll();
		saveTerm(SERVICE_TERM, "서비스 이용약관", "SERVICE", true);
		saveTerm(PRIVACY_TERM, "개인정보 처리방침", "PRIVACY", true);
	}

	@Test
	void localLoginStoresUserHistory() throws Exception {
		String email = "history-ok@example.com";
		signup(email, "이력유저");
		MemberJpaEntity member = memberJpaRepository.findByEmailIgnoreCase(email).orElseThrow();

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.header("X-Forwarded-For", "203.0.113.10")
						.header("User-Agent", IPHONE_UA)
						.content("""
								{"email":"%s","password":"%s"}
								""".formatted(email, PASSWORD)))
				.andExpect(status().isOk());

		List<LoginHistoryJpaEntity> histories = loginHistoryJpaRepository
				.findByActorTypeAndActorIdOrderByCreatedAtDesc(ActorType.USER, member.getMemberId());
		assertThat(histories).hasSize(1);
		LoginHistoryJpaEntity history = histories.get(0);
		assertThat(history.getActorType()).isEqualTo(ActorType.USER);
		assertThat(history.getActorId()).isEqualTo(member.getMemberId());
		assertThat(history.getIpAddress()).isEqualTo("203.0.113.10");
		assertThat(history.getUserAgent()).isEqualTo(IPHONE_UA);
		assertThat(history.getDeviceInfo()).isEqualTo(DeviceInfo.MOBILE);
		assertThat(history.getCreatedAt()).isNotNull();
		assertThat(history.getUpdatedAt()).isNotNull();
	}

	@Test
	void failedLoginDoesNotStoreHistory() throws Exception {
		String email = "history-bad@example.com";
		signup(email, "실패유저");
		MemberJpaEntity member = memberJpaRepository.findByEmailIgnoreCase(email).orElseThrow();

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.header("User-Agent", IPHONE_UA)
						.content("""
								{"email":"%s","password":"wrong-password"}
								""".formatted(email)))
				.andExpect(status().isUnauthorized());

		assertThat(loginHistoryJpaRepository.findByActorTypeAndActorIdOrderByCreatedAtDesc(
				ActorType.USER,
				member.getMemberId()
		)).isEmpty();
	}

	@Test
	void refreshDoesNotStoreHistory() throws Exception {
		String email = "history-refresh@example.com";
		signup(email, "리프레시이력");
		MemberJpaEntity member = memberJpaRepository.findByEmailIgnoreCase(email).orElseThrow();

		MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"%s"}
								""".formatted(email, PASSWORD)))
				.andExpect(status().isOk())
				.andReturn();
		Cookie refreshCookie = loginResult.getResponse().getCookie("refresh_token");

		mockMvc.perform(post("/api/v1/auth/refresh").cookie(refreshCookie))
				.andExpect(status().isOk());

		assertThat(loginHistoryJpaRepository.findByActorTypeAndActorIdOrderByCreatedAtDesc(
				ActorType.USER,
				member.getMemberId()
		)).hasSize(1);
	}

	@Test
	void socialLoginStoresHistoryForExistingMember() throws Exception {
		verifyPhone(PHONE);
		mockMvc.perform(post("/api/v1/auth/kakao/signup")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "authorizationCode": "stub:kakao-history-1:kakao-history@example.com",
								  "nickname": "카카오이력",
								  "phoneNumber": "%s",
								  "name": "테스트사용자",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true}
								  ]
								}
								""".formatted(PHONE, SERVICE_TERM, PRIVACY_TERM)))
				.andExpect(status().isCreated());

		MemberJpaEntity member = memberJpaRepository.findByEmailIgnoreCase("kakao-history@example.com").orElseThrow();
		assertThat(loginHistoryJpaRepository.findByActorTypeAndActorIdOrderByCreatedAtDesc(
				ActorType.USER,
				member.getMemberId()
		)).hasSize(1);

		mockMvc.perform(post("/api/v1/auth/kakao/login")
						.contentType(MediaType.APPLICATION_JSON)
						.header("X-Forwarded-For", "198.51.100.20")
						.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
						.content("""
								{"authorizationCode":"stub:kakao-history-1:kakao-history@example.com"}
								"""))
				.andExpect(status().isOk());

		List<LoginHistoryJpaEntity> histories = loginHistoryJpaRepository
				.findByActorTypeAndActorIdOrderByCreatedAtDesc(ActorType.USER, member.getMemberId());
		assertThat(histories).hasSize(2);
		assertThat(histories.get(0).getIpAddress()).isEqualTo("198.51.100.20");
		assertThat(histories.get(0).getDeviceInfo()).isEqualTo(DeviceInfo.DESKTOP);
	}

	@Test
	void socialLoginDoesNotStoreHistoryForNewMember() throws Exception {
		long before = loginHistoryJpaRepository.count();
		mockMvc.perform(post("/api/v1/auth/google/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"authorizationCode":"stub:google-history-new:new-history@example.com"}
								"""))
				.andExpect(status().isOk());
		assertThat(loginHistoryJpaRepository.count()).isEqualTo(before);
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
								  "name": "테스트사용자",
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
