package com.planwith.planwith_fo_member.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.planwith.planwith_fo_member.adapter.in.web.auth.AuthenticatedUser;
import com.planwith.planwith_fo_member.adapter.in.web.auth.AuthenticatedUserContext;
import com.planwith.planwith_fo_member.adapter.in.web.auth.GatewayAuthenticationContextResolver;
import com.planwith.planwith_fo_member.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.ChangePasswordRequest;
import com.planwith.planwith_fo_member.adapter.in.web.dto.InternalMemberResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.MemberAgreementResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.MemberMeResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.MemberProfileResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.PublicProfileResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.UpdateMyPageRequest;
import com.planwith.planwith_fo_member.adapter.in.web.dto.UpdateMyPageResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.UpsertAgreementsRequest;
import com.planwith.planwith_fo_member.application.port.in.ChangePasswordUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetMemberInternalUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetMyMemberUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetMyProfileUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetPublicProfileUseCase;
import com.planwith.planwith_fo_member.application.port.in.MemberAgreementUseCase;
import com.planwith.planwith_fo_member.application.port.in.UpdateMyPageUseCase;
import com.planwith.planwith_fo_member.application.port.in.UploadProfileImageUseCase;
import com.planwith.planwith_fo_member.application.port.in.WithdrawMemberUseCase;
import com.planwith.planwith_fo_member.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(name = "member-me", description = "내 회원정보 / 프로필 / 약관 동의")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@SecurityRequirement(name = OpenApiConfig.GATEWAY_USER_ID_SCHEME)
public class MemberMeController {

	private final GatewayAuthenticationContextResolver authContextResolver;
	private final GetMyMemberUseCase getMyMemberUseCase;
	private final UpdateMyPageUseCase updateMyPageUseCase;
	private final ChangePasswordUseCase changePasswordUseCase;
	private final WithdrawMemberUseCase withdrawMemberUseCase;
	private final GetMyProfileUseCase getMyProfileUseCase;
	private final UploadProfileImageUseCase uploadProfileImageUseCase;
	private final MemberAgreementUseCase memberAgreementUseCase;
	private final GetPublicProfileUseCase getPublicProfileUseCase;
	private final GetMemberInternalUseCase getMemberInternalUseCase;

	public MemberMeController(
			GatewayAuthenticationContextResolver authContextResolver,
			GetMyMemberUseCase getMyMemberUseCase,
			UpdateMyPageUseCase updateMyPageUseCase,
			ChangePasswordUseCase changePasswordUseCase,
			WithdrawMemberUseCase withdrawMemberUseCase,
			GetMyProfileUseCase getMyProfileUseCase,
			UploadProfileImageUseCase uploadProfileImageUseCase,
			MemberAgreementUseCase memberAgreementUseCase,
			GetPublicProfileUseCase getPublicProfileUseCase,
			GetMemberInternalUseCase getMemberInternalUseCase
	) {
		this.authContextResolver = authContextResolver;
		this.getMyMemberUseCase = getMyMemberUseCase;
		this.updateMyPageUseCase = updateMyPageUseCase;
		this.changePasswordUseCase = changePasswordUseCase;
		this.withdrawMemberUseCase = withdrawMemberUseCase;
		this.getMyProfileUseCase = getMyProfileUseCase;
		this.uploadProfileImageUseCase = uploadProfileImageUseCase;
		this.memberAgreementUseCase = memberAgreementUseCase;
		this.getPublicProfileUseCase = getPublicProfileUseCase;
		this.getMemberInternalUseCase = getMemberInternalUseCase;
	}

	@GetMapping("/members/me")
	@Operation(summary = "내 회원정보 조회")
	public ResponseEntity<ApiResponse<MemberMeResponse>> getMe() {
		UUID memberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(toMeResponse(getMyMemberUseCase.get(memberUuid))));
	}

	@PatchMapping({"/members/me", "/members/me/profile"})
	@Operation(summary = "마이페이지 저장 (휴대폰·닉네임·선택약관·비밀번호 한 요청)")
	public ResponseEntity<ApiResponse<UpdateMyPageResponse>> updateMyPage(
			@Valid @RequestBody UpdateMyPageRequest request
	) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		var result = updateMyPageUseCase.update(memberUuid, toCommand(request));
		return ResponseEntity.ok(ApiResponse.success(toUpdateResponse(result)));
	}

	@PatchMapping("/members/me/password")
	@Operation(summary = "비밀번호만 단독 변경 (로컬 회원만, 보조)")
	public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		changePasswordUseCase.change(memberUuid, request.currentPassword(), request.newPassword());
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/members/me")
	@Operation(summary = "회원 탈퇴 (soft delete → DELETED)")
	public ResponseEntity<Void> withdraw() {
		UUID memberUuid = authContextResolver.requireUser().userId();
		withdrawMemberUseCase.withdraw(memberUuid);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/members/me/profile")
	@Operation(summary = "내 프로필 조회")
	public ResponseEntity<ApiResponse<MemberProfileResponse>> getMyProfile() {
		UUID memberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(toProfileResponse(getMyProfileUseCase.get(memberUuid))));
	}

	@PostMapping(value = "/members/me/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "프로필 이미지 업로드 (400x400, jpg/png/webp, stub URL)")
	public ResponseEntity<ApiResponse<MemberProfileResponse>> uploadProfileImage(
			@RequestPart("file") MultipartFile file
	) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(toProfileResponse(uploadProfileImageUseCase.upload(memberUuid, file))));
	}

	@GetMapping("/members/me/agreements")
	@Operation(summary = "내 약관 동의 조회 (화면 로드용)")
	public ResponseEntity<ApiResponse<List<MemberAgreementResponse>>> listAgreements() {
		UUID memberUuid = authContextResolver.requireUser().userId();
		return ResponseEntity.ok(ApiResponse.success(toAgreementResponses(memberAgreementUseCase.list(memberUuid))));
	}

	@PostMapping("/members/me/agreements")
	@Operation(summary = "선택 약관만 단독 변경 (보조)")
	public ResponseEntity<ApiResponse<List<MemberAgreementResponse>>> upsertAgreements(
			@Valid @RequestBody UpsertAgreementsRequest request
	) {
		UUID memberUuid = authContextResolver.requireUser().userId();
		List<MemberAgreementResponse> body = toAgreementResponses(memberAgreementUseCase.upsertOptional(
				memberUuid,
				request.agreements().stream()
						.map(item -> new MemberAgreementUseCase.AgreementInput(
								item.termUuid(),
								Boolean.TRUE.equals(item.agreed())
						))
						.toList()
		));
		return ResponseEntity.ok(ApiResponse.success(body));
	}

	@GetMapping("/members/{memberUuid}/profile")
	@Operation(summary = "공개 프로필 조회", security = {})
	public ResponseEntity<ApiResponse<PublicProfileResponse>> getPublicProfile(
			@PathVariable UUID memberUuid
	) {
		var result = getPublicProfileUseCase.getPublic(memberUuid, viewerUuidOrNull());
		return ResponseEntity.ok(ApiResponse.success(new PublicProfileResponse(
				result.memberUuid(),
				result.nickname(),
				result.profileImage(),
				result.profileIntro(),
				result.grade(),
				result.followerCount(),
				result.followingCount(),
				result.isFollowing()
		)));
	}

	@GetMapping("/members/{memberUuid}")
	@Operation(summary = "회원 최소정보 조회 (내부, Gateway Trust)", security = {})
	public ResponseEntity<ApiResponse<InternalMemberResponse>> getInternalMember(
			@PathVariable UUID memberUuid,
			HttpServletRequest request
	) {
		authContextResolver.requireTrustedGateway(request);
		var result = getMemberInternalUseCase.getInternal(memberUuid);
		return ResponseEntity.ok(ApiResponse.success(new InternalMemberResponse(
				result.memberUuid(),
				result.email(),
				result.phoneNumber(),
				result.name(),
				result.loginType(),
				result.status(),
				result.createdAt()
		)));
	}

	private UpdateMyPageUseCase.UpdateMyPageCommand toCommand(UpdateMyPageRequest request) {
		List<UpdateMyPageUseCase.AgreementItem> agreements = request.agreements() == null
				? List.of()
				: request.agreements().stream()
						.map(item -> new UpdateMyPageUseCase.AgreementItem(
								item.termUuid(),
								Boolean.TRUE.equals(item.agreed())
						))
						.toList();
		return new UpdateMyPageUseCase.UpdateMyPageCommand(
				request.phoneNumber(),
				request.name(),
				request.nickname(),
				request.profileImage(),
				request.profileIntro(),
				agreements,
				request.currentPassword(),
				request.newPassword()
		);
	}

	private UpdateMyPageResponse toUpdateResponse(UpdateMyPageUseCase.UpdateMyPageResult result) {
		return new UpdateMyPageResponse(
				toMeResponse(result.member()),
				toProfileResponse(result.profile()),
				toAgreementResponses(result.agreements())
		);
	}

	private List<MemberAgreementResponse> toAgreementResponses(List<MemberAgreementUseCase.AgreementView> agreements) {
		return agreements.stream()
				.map(item -> new MemberAgreementResponse(
						item.termUuid(),
						item.title(),
						item.termType(),
						item.version(),
						item.required(),
						item.agreed(),
						item.agreedAt()
				))
				.toList();
	}

	private MemberMeResponse toMeResponse(GetMyMemberUseCase.MyMemberResult result) {
		return new MemberMeResponse(
				result.memberUuid(),
				result.email(),
				result.phoneNumber(),
				result.name(),
				result.loginType(),
				result.status(),
				result.createdAt(),
				result.lastLoginAt()
		);
	}

	private MemberProfileResponse toProfileResponse(GetMyProfileUseCase.ProfileResult result) {
		return new MemberProfileResponse(
				result.memberUuid(),
				result.nickname(),
				result.profileImage(),
				result.profileIntro(),
				result.grade()
		);
	}

	private UUID viewerUuidOrNull() {
		AuthenticatedUser viewer = AuthenticatedUserContext.get();
		return viewer == null ? null : viewer.userId();
	}
}
