package com.planwith.planwith_fo_member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.planwith.planwith_fo_member.adapter.out.persistence.terms.TermsJpaEntity;
import com.planwith.planwith_fo_member.adapter.out.persistence.terms.TermsJpaRepository;
import com.planwith.planwith_fo_member.adapter.out.token.InMemoryRefreshTokenStore;
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryEmailVerificationStore;
import com.planwith.planwith_fo_member.adapter.out.verification.InMemoryPhoneVerificationStore;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberMeIntegrationTests {

	private static final UUID SERVICE_TERM = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID PRIVACY_TERM = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MARKETING_TERM = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final String PHONE = "01012345678";
	private static final String PASSWORD = "Password1!";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

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
		saveTerm(MARKETING_TERM, "마케팅 수신 동의", "MARKETING", false);
	}

	@Test
	void meProfilePasswordWithdrawAndAgreementsWork() throws Exception {
		String email = "me-ok@example.com";
		String memberUuid = signupAndGetUuid(email, "내정보유저");

		mockMvc.perform(get("/api/v1/members/me")
						.header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(email))
				.andExpect(jsonPath("$.data.phoneNumber").value(PHONE))
				.andExpect(jsonPath("$.data.loginType").value("LOCAL"));

		mockMvc.perform(get("/api/v1/members/me"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(patch("/api/v1/members/me")
						.header("X-Auth-User-Id", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nickname":"마이닉변경",
								  "profileIntro":"소개문구",
								  "agreements":[{"termUuid":"%s","agreed":true}],
								  "currentPassword":"%s",
								  "newPassword":"Password2!"
								}
								""".formatted(MARKETING_TERM, PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.member.email").value(email))
				.andExpect(jsonPath("$.data.profile.nickname").value("마이닉변경"))
				.andExpect(jsonPath("$.data.profile.profileIntro").value("소개문구"))
				.andExpect(jsonPath("$.data.agreements[?(@.termUuid=='%s')].agreed".formatted(MARKETING_TERM)).value(true));

		MockMultipartFile file = new MockMultipartFile(
				"file",
				"avatar.png",
				"image/png",
				pngBytes(400, 400)
		);
		mockMvc.perform(multipart("/api/v1/members/me/profile/image")
						.file(file)
						.header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.profileImage").value(org.hamcrest.Matchers.startsWith("stub://profiles/")));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"Password2!"}
								""".formatted(email)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/members/me/agreements")
						.header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.length()").value(3));

		mockMvc.perform(post("/api/v1/members/me/agreements")
						.header("X-Auth-User-Id", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"agreements":[{"termUuid":"%s","agreed":false}]}
								""".formatted(SERVICE_TERM)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("REQUIRED_TERM_NOT_MODIFIABLE"));

		mockMvc.perform(patch("/api/v1/members/me")
						.header("X-Auth-User-Id", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"agreements":[{"termUuid":"%s","agreed":false}]}
								""".formatted(SERVICE_TERM)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("REQUIRED_TERM_NOT_MODIFIABLE"));

		mockMvc.perform(get("/api/v1/terms/{termUuid}", SERVICE_TERM))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content").isNotEmpty())
				.andExpect(jsonPath("$.data.isRequired").value(true));

		mockMvc.perform(get("/api/v1/members/{memberUuid}/profile", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.nickname").value("마이닉변경"));

		mockMvc.perform(get("/api/v1/members/{memberUuid}", memberUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(email));

		mockMvc.perform(delete("/api/v1/members/me")
						.header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/members/me")
						.header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isNotFound());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"Password2!"}
								""".formatted(email)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void phoneUpdateRequiresVerification() throws Exception {
		String email = "me-phone@example.com";
		String memberUuid = signupAndGetUuid(email, "폰변경유저");
		String newPhone = "01099998888";

		mockMvc.perform(patch("/api/v1/members/me")
						.header("X-Auth-User-Id", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"phoneNumber\":\"%s\"}".formatted(newPhone)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("PHONE_NOT_VERIFIED"));

		verifyPhone(newPhone);
		mockMvc.perform(patch("/api/v1/members/me")
						.header("X-Auth-User-Id", memberUuid)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"phoneNumber\":\"%s\"}".formatted(newPhone)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.member.phoneNumber").value(newPhone));
	}

	private String signupAndGetUuid(String email, String nickname) throws Exception {
		verifyEmail(email);
		verifyPhone(PHONE);
		MvcResult result = mockMvc.perform(post("/api/v1/members")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s",
								  "phoneNumber": "%s",
								  "nickname": "%s",
								  "agreements": [
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": false}
								  ]
								}
								""".formatted(email, PASSWORD, PHONE, nickname, SERVICE_TERM, PRIVACY_TERM, MARKETING_TERM)))
				.andExpect(status().isCreated())
				.andReturn();
		JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
		return root.path("data").path("memberUuid").asText();
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
		entity.setContent(title + " 본문");
		entity.setRequired(required);
		entity.setActive(true);
		termsJpaRepository.save(entity);
	}

	private byte[] pngBytes(int width, int height) throws Exception {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		var graphics = image.createGraphics();
		graphics.setColor(Color.BLUE);
		graphics.fillRect(0, 0, width, height);
		graphics.dispose();
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(image, "png", output);
		return output.toByteArray();
	}
}
