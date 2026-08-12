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
import com.planwith.planwith_fo_member.adapter.in.web.dto.TermResponse;
import com.planwith.planwith_fo_member.application.port.in.ConfirmEmailVerificationUseCase;
import com.planwith.planwith_fo_member.application.port.in.ListTermsUseCase;
import com.planwith.planwith_fo_member.application.port.in.LocalSignupUseCase;
import com.planwith.planwith_fo_member.application.port.in.SendEmailVerificationUseCase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1")
@Tag(name = "member-auth", description = "Member signup / email verification / terms")
public class MemberAuthController {

	private final SendEmailVerificationUseCase sendEmailVerificationUseCase;
	private final ConfirmEmailVerificationUseCase confirmEmailVerificationUseCase;
	private final ListTermsUseCase listTermsUseCase;
	private final LocalSignupUseCase localSignupUseCase;

	public MemberAuthController(
			SendEmailVerificationUseCase sendEmailVerificationUseCase,
			ConfirmEmailVerificationUseCase confirmEmailVerificationUseCase,
			ListTermsUseCase listTermsUseCase,
			LocalSignupUseCase localSignupUseCase
	) {
		this.sendEmailVerificationUseCase = sendEmailVerificationUseCase;
		this.confirmEmailVerificationUseCase = confirmEmailVerificationUseCase;
		this.listTermsUseCase = listTermsUseCase;
		this.localSignupUseCase = localSignupUseCase;
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
