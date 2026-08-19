package com.planwith.planwith_fo_member.adapter.in.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_member.adapter.in.web.auth.AuthenticatedUser;
import com.planwith.planwith_fo_member.adapter.in.web.auth.AuthenticatedUserContext;
import com.planwith.planwith_fo_member.adapter.in.web.auth.GatewayAuthenticationContextResolver;
import com.planwith.planwith_fo_member.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.FollowResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.FollowStatusResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.MemberProfileResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.PagedResponse;
import com.planwith.planwith_fo_member.application.port.in.FollowMemberUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetFollowStatusUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetMyProfileUseCase;
import com.planwith.planwith_fo_member.application.port.in.ListFollowersUseCase;
import com.planwith.planwith_fo_member.application.port.in.ListFollowingsUseCase;
import com.planwith.planwith_fo_member.application.port.in.ListMembersUseCase;
import com.planwith.planwith_fo_member.application.port.in.UnfollowMemberUseCase;
import com.planwith.planwith_fo_member.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(name = "member-follow", description = "팔로우 / 팔로워 / 팔로잉 / 회원 검색")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@SecurityRequirement(name = OpenApiConfig.GATEWAY_USER_ID_SCHEME)
public class MemberFollowController {

	private final GatewayAuthenticationContextResolver authContextResolver;
	private final FollowMemberUseCase followMemberUseCase;
	private final UnfollowMemberUseCase unfollowMemberUseCase;
	private final GetFollowStatusUseCase getFollowStatusUseCase;
	private final ListFollowersUseCase listFollowersUseCase;
	private final ListFollowingsUseCase listFollowingsUseCase;
	private final ListMembersUseCase listMembersUseCase;

	public MemberFollowController(
			GatewayAuthenticationContextResolver authContextResolver,
			FollowMemberUseCase followMemberUseCase,
			UnfollowMemberUseCase unfollowMemberUseCase,
			GetFollowStatusUseCase getFollowStatusUseCase,
			ListFollowersUseCase listFollowersUseCase,
			ListFollowingsUseCase listFollowingsUseCase,
			ListMembersUseCase listMembersUseCase
	) {
		this.authContextResolver = authContextResolver;
		this.followMemberUseCase = followMemberUseCase;
		this.unfollowMemberUseCase = unfollowMemberUseCase;
		this.getFollowStatusUseCase = getFollowStatusUseCase;
		this.listFollowersUseCase = listFollowersUseCase;
		this.listFollowingsUseCase = listFollowingsUseCase;
		this.listMembersUseCase = listMembersUseCase;
	}

	@GetMapping("/members/search")
	@Operation(summary = "회원 목록 조회 (팔로우 대상 UUID)", security = {})
	public ResponseEntity<ApiResponse<PagedResponse<MemberProfileResponse>>> searchMembers(
			@RequestParam(required = false) String nickname,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
	) {
		AuthenticatedUser viewer = AuthenticatedUserContext.get();
		UUID viewerUuid = viewer == null ? null : viewer.userId();
		ListMembersUseCase.PagedProfiles result = listMembersUseCase.list(nickname, viewerUuid, page, size);
		return ResponseEntity.ok(ApiResponse.success(toPagedResponse(result)));
	}

	@PostMapping("/members/{memberUuid}/follow")
	@Operation(summary = "해당 회원 팔로우")
	public ResponseEntity<ApiResponse<FollowResponse>> follow(@PathVariable UUID memberUuid) {
		UUID followerUuid = authContextResolver.requireUser().userId();
		FollowMemberUseCase.FollowResult result = followMemberUseCase.follow(followerUuid, memberUuid);
		return ResponseEntity.ok(ApiResponse.success(new FollowResponse(result.followUuid(), result.isActive())));
	}

	@DeleteMapping("/members/{memberUuid}/follow")
	@Operation(summary = "언팔로우 (비활성화)")
	public ResponseEntity<Void> unfollow(@PathVariable UUID memberUuid) {
		UUID followerUuid = authContextResolver.requireUser().userId();
		unfollowMemberUseCase.unfollow(followerUuid, memberUuid);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/members/{memberUuid}/followers")
	@Operation(summary = "팔로워 목록 조회", security = {})
	public ResponseEntity<ApiResponse<PagedResponse<MemberProfileResponse>>> listFollowers(
			@PathVariable UUID memberUuid,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
	) {
		return ResponseEntity.ok(ApiResponse.success(toPagedResponse(listFollowersUseCase.listFollowers(memberUuid, page, size))));
	}

	@GetMapping("/members/{memberUuid}/followings")
	@Operation(summary = "팔로잉 목록 조회", security = {})
	public ResponseEntity<ApiResponse<PagedResponse<MemberProfileResponse>>> listFollowings(
			@PathVariable UUID memberUuid,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
	) {
		return ResponseEntity.ok(ApiResponse.success(toPagedResponse(listFollowingsUseCase.listFollowings(memberUuid, page, size))));
	}

	@GetMapping("/members/{memberUuid}/follow-status")
	@Operation(summary = "내가 해당 회원을 팔로우 중인지 확인")
	public ResponseEntity<ApiResponse<FollowStatusResponse>> followStatus(@PathVariable UUID memberUuid) {
		UUID followerUuid = authContextResolver.requireUser().userId();
		boolean isFollowing = getFollowStatusUseCase.isFollowing(followerUuid, memberUuid);
		return ResponseEntity.ok(ApiResponse.success(new FollowStatusResponse(isFollowing)));
	}

	private PagedResponse<MemberProfileResponse> toPagedResponse(ListMembersUseCase.PagedProfiles page) {
		return new PagedResponse<>(
				page.content().stream().map(this::toProfileResponse).toList(),
				page.page(),
				page.size(),
				page.totalElements(),
				page.totalPages()
		);
	}

	private PagedResponse<MemberProfileResponse> toPagedResponse(ListFollowersUseCase.PagedProfiles page) {
		return new PagedResponse<>(
				page.content().stream().map(this::toProfileResponse).toList(),
				page.page(),
				page.size(),
				page.totalElements(),
				page.totalPages()
		);
	}

	private MemberProfileResponse toProfileResponse(GetMyProfileUseCase.ProfileResult result) {
		return new MemberProfileResponse(
				result.memberUuid(),
				result.nickname(),
				result.profileImage(),
				result.profileIntro(),
				result.grade(),
				result.profileBadge(),
				result.profileSpecialBorder()
		);
	}
}
