package com.planwith.planwith_fo_member;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class MemberFollowIntegrationTests {

	private static final UUID SERVICE_TERM = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID PRIVACY_TERM = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID MARKETING_TERM = UUID.fromString("33333333-3333-3333-3333-333333333333");
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
	void followUnfollowStatusAndListsWork() throws Exception {
		String targetUuid = signupAndGetUuid("follow-target@example.com", "대상유저", "01011112222");
		String followerUuid = signupAndGetUuid("follow-me@example.com", "팔로워유저", "01033334444");
		String otherUuid = signupAndGetUuid("follow-other@example.com", "다른유저", "01055556666");

		mockMvc.perform(post("/api/v1/members/{memberUuid}/follow", targetUuid))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/members/{memberUuid}/follow", targetUuid)
						.header("X-Auth-User-Id", followerUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.followUuid").isNotEmpty())
				.andExpect(jsonPath("$.data.isActive").value(true));

		mockMvc.perform(post("/api/v1/members/{memberUuid}/follow", targetUuid)
						.header("X-Auth-User-Id", followerUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.isActive").value(true));

		mockMvc.perform(get("/api/v1/members/{memberUuid}/follow-status", targetUuid)
						.header("X-Auth-User-Id", followerUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.isFollowing").value(true));

		mockMvc.perform(get("/api/v1/members/{memberUuid}/follow-status", targetUuid)
						.header("X-Auth-User-Id", otherUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.isFollowing").value(false));

		mockMvc.perform(get("/api/v1/members/{memberUuid}/followers", targetUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content.length()").value(1))
				.andExpect(jsonPath("$.data.content[0].memberUuid").value(followerUuid))
				.andExpect(jsonPath("$.data.content[0].nickname").value("팔로워유저"))
				.andExpect(jsonPath("$.data.page").value(0))
				.andExpect(jsonPath("$.data.totalElements").value(1));

		mockMvc.perform(get("/api/v1/members/{memberUuid}/followings", followerUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content.length()").value(1))
				.andExpect(jsonPath("$.data.content[0].memberUuid").value(targetUuid))
				.andExpect(jsonPath("$.data.content[0].nickname").value("대상유저"));

		mockMvc.perform(get("/api/v1/members/search")
						.param("nickname", "대상유저"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].memberUuid").value(targetUuid))
				.andExpect(jsonPath("$.data.content[0].nickname").value("대상유저"));

		mockMvc.perform(get("/api/v1/members/{memberUuid}/profile", targetUuid)
						.header("X-Auth-User-Id", followerUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.memberUuid").value(targetUuid))
				.andExpect(jsonPath("$.data.followerCount").value(1))
				.andExpect(jsonPath("$.data.followingCount").value(0))
				.andExpect(jsonPath("$.data.isFollowing").value(true));

		mockMvc.perform(get("/api/v1/members/{memberUuid}/profile", targetUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.memberUuid").value(targetUuid))
				.andExpect(jsonPath("$.data.isFollowing").value(org.hamcrest.Matchers.nullValue()));

		mockMvc.perform(delete("/api/v1/members/{memberUuid}/follow", targetUuid)
						.header("X-Auth-User-Id", followerUuid))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/members/{memberUuid}/follow-status", targetUuid)
						.header("X-Auth-User-Id", followerUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.isFollowing").value(false));

		mockMvc.perform(get("/api/v1/members/{memberUuid}/followers", targetUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content.length()").value(0))
				.andExpect(jsonPath("$.data.totalElements").value(0));

		mockMvc.perform(post("/api/v1/members/{memberUuid}/follow", targetUuid)
						.header("X-Auth-User-Id", followerUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.isActive").value(true));

		mockMvc.perform(get("/api/v1/members/{memberUuid}/follow-status", targetUuid)
						.header("X-Auth-User-Id", followerUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.isFollowing").value(true));
	}

	@Test
	void cannotFollowSelfOrMissingMember() throws Exception {
		String memberUuid = signupAndGetUuid("follow-self@example.com", "셀프유저", "01077778888");

		mockMvc.perform(post("/api/v1/members/{memberUuid}/follow", memberUuid)
						.header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error.code").value("CANNOT_FOLLOW_SELF"));

		mockMvc.perform(post("/api/v1/members/{memberUuid}/follow", UUID.randomUUID())
						.header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("MEMBER_NOT_FOUND"));

		mockMvc.perform(get("/api/v1/members/{memberUuid}/followers", UUID.randomUUID()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("MEMBER_NOT_FOUND"));

		mockMvc.perform(get("/api/v1/members/{memberUuid}/follow-status", UUID.randomUUID())
						.header("X-Auth-User-Id", memberUuid))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error.code").value("MEMBER_NOT_FOUND"));
	}

	@Test
	void followerListPaginatesAndHidesInactive() throws Exception {
		String targetUuid = signupAndGetUuid("follow-page-target@example.com", "페이지대상", "01012121212");
		String firstUuid = signupAndGetUuid("follow-page-1@example.com", "페이지일", "01013131313");
		String secondUuid = signupAndGetUuid("follow-page-2@example.com", "페이지이", "01014141414");
		String thirdUuid = signupAndGetUuid("follow-page-3@example.com", "페이지삼", "01015151515");

		mockMvc.perform(post("/api/v1/members/{memberUuid}/follow", targetUuid)
						.header("X-Auth-User-Id", firstUuid))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/members/{memberUuid}/follow", targetUuid)
						.header("X-Auth-User-Id", secondUuid))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/members/{memberUuid}/follow", targetUuid)
						.header("X-Auth-User-Id", thirdUuid))
				.andExpect(status().isOk());
		mockMvc.perform(delete("/api/v1/members/{memberUuid}/follow", targetUuid)
						.header("X-Auth-User-Id", firstUuid))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/v1/members/{memberUuid}/followers", targetUuid)
						.param("page", "0")
						.param("size", "1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content.length()").value(1))
				.andExpect(jsonPath("$.data.page").value(0))
				.andExpect(jsonPath("$.data.size").value(1))
				.andExpect(jsonPath("$.data.totalElements").value(2))
				.andExpect(jsonPath("$.data.totalPages").value(2));
	}

	@Test
	void memberListExcludesViewerAndSupportsNicknameSearch() throws Exception {
		String listedUuid = signupAndGetUuid("follow-list@example.com", "목록검색갑", "01016161616");
		String viewerUuid = signupAndGetUuid("follow-list-viewer@example.com", "목록검색을", "01017171717");

		mockMvc.perform(get("/api/v1/members/search")
						.param("nickname", "목록검색갑"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content.length()").value(1))
				.andExpect(jsonPath("$.data.content[0].memberUuid").value(listedUuid));

		mockMvc.perform(get("/api/v1/members/search")
						.param("nickname", "목록검색을")
						.header("X-Auth-User-Id", viewerUuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content.length()").value(0));
	}

	private String signupAndGetUuid(String email, String nickname, String phone) throws Exception {
		verifyEmail(email);
		verifyPhone(phone);
		MvcResult result = mockMvc.perform(post("/api/v1/members")
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
								    {"termUuid": "%s", "agreed": true},
								    {"termUuid": "%s", "agreed": false}
								  ]
								}
								""".formatted(email, PASSWORD, phone, nickname, SERVICE_TERM, PRIVACY_TERM, MARKETING_TERM)))
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
}
