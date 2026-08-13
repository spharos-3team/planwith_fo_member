package com.planwith.planwith_fo_member.adapter.in.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.planwith.planwith_fo_member.adapter.in.web.dto.ApiResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.EmailVerificationConfirmRequest;
import com.planwith.planwith_fo_member.adapter.in.web.dto.EmailVerificationConfirmResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.EmailVerificationSendRequest;
import com.planwith.planwith_fo_member.adapter.in.web.dto.EmailVerificationSendResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.LocalSignupRequest;
import com.planwith.planwith_fo_member.adapter.in.web.dto.LocalSignupResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.NicknameAvailabilityResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.PhoneVerificationConfirmRequest;
import com.planwith.planwith_fo_member.adapter.in.web.dto.PhoneVerificationConfirmResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.PhoneVerificationPrepareResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.TermResponse;
import com.planwith.planwith_fo_member.application.port.in.CheckNicknameAvailabilityUseCase;
import com.planwith.planwith_fo_member.application.port.in.ConfirmEmailVerificationUseCase;
import com.planwith.planwith_fo_member.application.port.in.ConfirmPhoneVerificationUseCase;
import com.planwith.planwith_fo_member.application.port.in.ListTermsUseCase;
import com.planwith.planwith_fo_member.application.port.in.LocalSignupUseCase;
import com.planwith.planwith_fo_member.application.port.in.PreparePhoneVerificationUseCase;
import com.planwith.planwith_fo_member.application.port.in.SendEmailVerificationUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(name = "member-auth", description = "Member signup / email & phone verification / terms")
public class MemberAuthController {

	private final SendEmailVerificationUseCase sendEmailVerificationUseCase;
	private final ConfirmEmailVerificationUseCase confirmEmailVerificationUseCase;
	private final PreparePhoneVerificationUseCase preparePhoneVerificationUseCase;
	private final ConfirmPhoneVerificationUseCase confirmPhoneVerificationUseCase;
	private final ListTermsUseCase listTermsUseCase;
	private final LocalSignupUseCase localSignupUseCase;
	private final CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;

	public MemberAuthController(
			SendEmailVerificationUseCase sendEmailVerificationUseCase,
			ConfirmEmailVerificationUseCase confirmEmailVerificationUseCase,
			PreparePhoneVerificationUseCase preparePhoneVerificationUseCase,
			ConfirmPhoneVerificationUseCase confirmPhoneVerificationUseCase,
			ListTermsUseCase listTermsUseCase,
			LocalSignupUseCase localSignupUseCase,
			CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase
	) {
		this.sendEmailVerificationUseCase = sendEmailVerificationUseCase;
		this.confirmEmailVerificationUseCase = confirmEmailVerificationUseCase;
		this.preparePhoneVerificationUseCase = preparePhoneVerificationUseCase;
		this.confirmPhoneVerificationUseCase = confirmPhoneVerificationUseCase;
		this.listTermsUseCase = listTermsUseCase;
		this.localSignupUseCase = localSignupUseCase;
		this.checkNicknameAvailabilityUseCase = checkNicknameAvailabilityUseCase;
	}

	@PostMapping("/auth/email-verifications")
	@Operation(summary = "이메일 인증번호 발송")
	public ResponseEntity<ApiResponse<EmailVerificationSendResponse>> sendEmailVerification(
			@Valid @RequestBody EmailVerificationSendRequest request
	) {
		var result = sendEmailVerificationUseCase.send(request.email());
		return ResponseEntity.ok(ApiResponse.success(new EmailVerificationSendResponse(
				result.email(),
				result.expiresInSeconds(),
				"인증번호를 발송했습니다."
		)));
	}

	@PostMapping("/auth/email-verifications/confirm")
	@Operation(summary = "이메일 인증번호 확인")
	public ResponseEntity<ApiResponse<EmailVerificationConfirmResponse>> confirmEmailVerification(
			@Valid @RequestBody EmailVerificationConfirmRequest request
	) {
		var result = confirmEmailVerificationUseCase.confirm(request.email(), request.code());
		return ResponseEntity.ok(ApiResponse.success(new EmailVerificationConfirmResponse(
				result.email(),
				result.verified()
		)));
	}

	@PostMapping("/auth/phone-verifications")
	@Operation(summary = "본인인증 요청 준비 (포트원 SDK용 storeId/channelKey/identityVerificationId 발급)")
	public ResponseEntity<ApiResponse<PhoneVerificationPrepareResponse>> preparePhoneVerification() {
		var result = preparePhoneVerificationUseCase.prepare();
		return ResponseEntity.ok(ApiResponse.success(new PhoneVerificationPrepareResponse(
				result.storeId(),
				result.channelKey(),
				result.identityVerificationId()
		)));
	}

	@PostMapping("/auth/phone-verifications/confirm")
	@Operation(summary = "본인인증 완료 확인 (포트원 서버 조회)")
	public ResponseEntity<ApiResponse<PhoneVerificationConfirmResponse>> confirmPhoneVerification(
			@Valid @RequestBody PhoneVerificationConfirmRequest request
	) {
		var result = confirmPhoneVerificationUseCase.confirm(request.identityVerificationId());
		return ResponseEntity.ok(ApiResponse.success(new PhoneVerificationConfirmResponse(
				result.verified(),
				result.phoneNumber(),
				result.maskedPhoneNumber()
		)));
	}

	@GetMapping("/terms")
	@Operation(summary = "약관 목록 조회")
	public ResponseEntity<ApiResponse<List<TermResponse>>> listTerms(
			@RequestParam(required = false) String termType
	) {
		List<TermResponse> terms = listTermsUseCase.list(termType).stream()
				.map(term -> new TermResponse(
						term.getTermUuid(),
						term.getTitle(),
						term.getTermType(),
						term.getVersion(),
						term.isRequired(),
						term.isActive()
				))
				.toList();
		return ResponseEntity.ok(ApiResponse.success(terms));
	}

	@GetMapping("/members/nicknames/availability")
	@Operation(summary = "닉네임 중복확인")
	public ResponseEntity<ApiResponse<NicknameAvailabilityResponse>> checkNicknameAvailability(
			@RequestParam
			@NotBlank(message = "닉네임은 필수입니다.")
			@Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
			String nickname
	) {
		var result = checkNicknameAvailabilityUseCase.check(nickname);
		return ResponseEntity.ok(ApiResponse.success(new NicknameAvailabilityResponse(
				result.nickname(),
				result.available()
		)));
	}

	@PostMapping("/members")
	@Operation(summary = "로컬 회원가입")
	public ResponseEntity<ApiResponse<LocalSignupResponse>> signup(
			@Valid @RequestBody LocalSignupRequest request
	) {
		var result = localSignupUseCase.signup(new LocalSignupUseCase.LocalSignupCommand(
				request.email(),
				request.password(),
				request.phoneNumber(),
				request.nickname(),
				request.profileImage(),
				request.profileIntro(),
				request.agreements().stream()
						.map(item -> new LocalSignupUseCase.AgreementItem(item.termUuid(), Boolean.TRUE.equals(item.agreed())))
						.toList()
		));
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(new LocalSignupResponse(
				result.memberUuid(),
				result.email(),
				result.nickname(),
				result.createdAt()
		)));
	}
}
