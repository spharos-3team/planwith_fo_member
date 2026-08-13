package com.planwith.planwith_fo_member.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.planwith.planwith_fo_member.adapter.in.web.dto.FindEmailRequest;
import com.planwith.planwith_fo_member.adapter.in.web.dto.FindEmailResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.LocalLoginRequest;
import com.planwith.planwith_fo_member.adapter.in.web.dto.LocalSignupRequest;
import com.planwith.planwith_fo_member.adapter.in.web.dto.LocalSignupResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.NicknameAvailabilityResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.PasswordResetDto;
import com.planwith.planwith_fo_member.adapter.in.web.dto.PasswordResetRequestDto;
import com.planwith.planwith_fo_member.adapter.in.web.dto.PasswordResetRequestResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.PhoneVerificationConfirmRequest;
import com.planwith.planwith_fo_member.adapter.in.web.dto.PhoneVerificationConfirmResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.PhoneVerificationPrepareResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.SocialLoginRequest;
import com.planwith.planwith_fo_member.adapter.in.web.dto.SocialLoginResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.SocialSignupRequest;
import com.planwith.planwith_fo_member.adapter.in.web.dto.SocialSignupResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.TermDetailResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.TermResponse;
import com.planwith.planwith_fo_member.adapter.in.web.dto.TokenResponse;
import com.planwith.planwith_fo_member.application.port.in.CheckNicknameAvailabilityUseCase;
import com.planwith.planwith_fo_member.application.port.in.ConfirmEmailVerificationUseCase;
import com.planwith.planwith_fo_member.application.port.in.ConfirmPhoneVerificationUseCase;
import com.planwith.planwith_fo_member.application.port.in.FindEmailUseCase;
import com.planwith.planwith_fo_member.application.port.in.GetTermDetailUseCase;
import com.planwith.planwith_fo_member.application.port.in.ListTermsUseCase;
import com.planwith.planwith_fo_member.application.port.in.LocalLoginUseCase;
import com.planwith.planwith_fo_member.application.port.in.LocalSignupUseCase;
import com.planwith.planwith_fo_member.application.port.in.LogoutUseCase;
import com.planwith.planwith_fo_member.application.port.in.PreparePhoneVerificationUseCase;
import com.planwith.planwith_fo_member.application.port.in.ReissueTokenUseCase;
import com.planwith.planwith_fo_member.application.port.in.RequestPasswordResetUseCase;
import com.planwith.planwith_fo_member.application.port.in.ResetPasswordUseCase;
import com.planwith.planwith_fo_member.application.port.in.SendEmailVerificationUseCase;
import com.planwith.planwith_fo_member.application.port.in.SocialLoginUseCase;
import com.planwith.planwith_fo_member.application.port.in.SocialSignupUseCase;
import com.planwith.planwith_fo_member.config.JwtProperties;
import com.planwith.planwith_fo_member.config.RefreshCookieProperties;
import com.planwith.planwith_fo_member.domain.member.LoginType;

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
	private final GetTermDetailUseCase getTermDetailUseCase;
	private final LocalSignupUseCase localSignupUseCase;
	private final SocialSignupUseCase socialSignupUseCase;
	private final SocialLoginUseCase socialLoginUseCase;
	private final LocalLoginUseCase localLoginUseCase;
	private final ReissueTokenUseCase reissueTokenUseCase;
	private final LogoutUseCase logoutUseCase;
	private final FindEmailUseCase findEmailUseCase;
	private final RequestPasswordResetUseCase requestPasswordResetUseCase;
	private final ResetPasswordUseCase resetPasswordUseCase;
	private final CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase;
	private final RefreshCookieProperties refreshCookieProperties;
	private final JwtProperties jwtProperties;

	public MemberAuthController(
			SendEmailVerificationUseCase sendEmailVerificationUseCase,
			ConfirmEmailVerificationUseCase confirmEmailVerificationUseCase,
			PreparePhoneVerificationUseCase preparePhoneVerificationUseCase,
			ConfirmPhoneVerificationUseCase confirmPhoneVerificationUseCase,
			ListTermsUseCase listTermsUseCase,
			GetTermDetailUseCase getTermDetailUseCase,
			LocalSignupUseCase localSignupUseCase,
			SocialSignupUseCase socialSignupUseCase,
			SocialLoginUseCase socialLoginUseCase,
			LocalLoginUseCase localLoginUseCase,
			ReissueTokenUseCase reissueTokenUseCase,
			LogoutUseCase logoutUseCase,
			FindEmailUseCase findEmailUseCase,
			RequestPasswordResetUseCase requestPasswordResetUseCase,
			ResetPasswordUseCase resetPasswordUseCase,
			CheckNicknameAvailabilityUseCase checkNicknameAvailabilityUseCase,
			RefreshCookieProperties refreshCookieProperties,
			JwtProperties jwtProperties
	) {
		this.sendEmailVerificationUseCase = sendEmailVerificationUseCase;
		this.confirmEmailVerificationUseCase = confirmEmailVerificationUseCase;
		this.preparePhoneVerificationUseCase = preparePhoneVerificationUseCase;
		this.confirmPhoneVerificationUseCase = confirmPhoneVerificationUseCase;
		this.listTermsUseCase = listTermsUseCase;
		this.getTermDetailUseCase = getTermDetailUseCase;
		this.localSignupUseCase = localSignupUseCase;
		this.socialSignupUseCase = socialSignupUseCase;
		this.socialLoginUseCase = socialLoginUseCase;
		this.localLoginUseCase = localLoginUseCase;
		this.reissueTokenUseCase = reissueTokenUseCase;
		this.logoutUseCase = logoutUseCase;
		this.findEmailUseCase = findEmailUseCase;
		this.requestPasswordResetUseCase = requestPasswordResetUseCase;
		this.resetPasswordUseCase = resetPasswordUseCase;
		this.checkNicknameAvailabilityUseCase = checkNicknameAvailabilityUseCase;
		this.refreshCookieProperties = refreshCookieProperties;
		this.jwtProperties = jwtProperties;
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

	@PostMapping("/auth/login")
	@Operation(summary = "로컬 로그인 (Refresh Token은 HttpOnly Cookie)")
	public ResponseEntity<ApiResponse<TokenResponse>> login(
			@Valid @RequestBody LocalLoginRequest request
	) {
		var result = localLoginUseCase.login(new LocalLoginUseCase.LocalLoginCommand(request.email(), request.password()));
		return tokenResponse(result);
	}

	@PostMapping({"/auth/refresh", "/auth/reissue"})
	@Operation(summary = "액세스 토큰 재발급 (Refresh Cookie, /reissue 별칭 지원)")
	public ResponseEntity<ApiResponse<TokenResponse>> refresh(
			@CookieValue(name = "${app.refresh-cookie.name:refresh_token}", required = false) String refreshToken
	) {
		var result = reissueTokenUseCase.reissue(refreshToken);
		return tokenResponse(result);
	}

	@PostMapping("/auth/logout")
	@Operation(summary = "로그아웃 (Refresh Cookie 폐기)")
	public ResponseEntity<Void> logout(
			@CookieValue(name = "${app.refresh-cookie.name:refresh_token}", required = false) String refreshToken
	) {
		logoutUseCase.logout(refreshToken);
		ResponseCookie cleared = RefreshCookieWriter.clear(refreshCookieProperties);
		return ResponseEntity.noContent()
				.header(HttpHeaders.SET_COOKIE, cleared.toString())
				.build();
	}

	@PostMapping("/auth/find-email")
	@Operation(summary = "아이디 찾기 (본인인증 완료된 휴대폰 번호)")
	public ResponseEntity<ApiResponse<FindEmailResponse>> findEmail(
			@Valid @RequestBody FindEmailRequest request
	) {
		var result = findEmailUseCase.findByVerifiedPhone(request.phoneNumber());
		return ResponseEntity.ok(ApiResponse.success(new FindEmailResponse(
				result.email(),
				result.maskedEmail(),
				result.loginType()
		)));
	}

	@PostMapping("/auth/password/reset-requests")
	@Operation(summary = "비밀번호 재설정 인증번호 발송 (로컬 회원만)")
	public ResponseEntity<ApiResponse<PasswordResetRequestResponse>> requestPasswordReset(
			@Valid @RequestBody PasswordResetRequestDto request
	) {
		var result = requestPasswordResetUseCase.request(request.email());
		return ResponseEntity.ok(ApiResponse.success(new PasswordResetRequestResponse(
				result.email(),
				result.expiresInSeconds(),
				result.message()
		)));
	}

	@PostMapping("/auth/password/reset")
	@Operation(summary = "비밀번호 재설정 (로컬 회원만)")
	public ResponseEntity<Void> resetPassword(
			@Valid @RequestBody PasswordResetDto request
	) {
		resetPasswordUseCase.reset(request.email(), request.code(), request.newPassword());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/auth/{provider}/login")
	@Operation(summary = "소셜 원클릭 로그인 (authorizationCode만, 기가입=토큰 / 미가입=isNewMember)")
	public ResponseEntity<ApiResponse<SocialLoginResponse>> socialLogin(
			@PathVariable String provider,
			@Valid @RequestBody SocialLoginRequest request
	) {
		LoginType loginType = SocialProviderParser.parse(provider);
		var result = socialLoginUseCase.login(loginType, new SocialLoginUseCase.SocialLoginCommand(
				request.authorizationCode(),
				request.redirectUri()
		));
		if (result.isNewMember() || result.tokens() == null) {
			return ResponseEntity.ok(ApiResponse.success(new SocialLoginResponse(
					true,
					null,
					null,
					null,
					null
			)));
		}
		var tokens = result.tokens();
		ResponseCookie cookie = RefreshCookieWriter.write(tokens.refreshToken(), refreshCookieProperties, jwtProperties);
		SocialLoginResponse body = new SocialLoginResponse(
				false,
				"Bearer",
				tokens.accessToken(),
				tokens.accessTokenExpiresIn(),
				new SocialLoginResponse.TokenUser(
						tokens.memberUuid().toString(),
						tokens.roles(),
						tokens.scopes()
				)
		);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(ApiResponse.success(body));
	}

	@PostMapping("/auth/{provider}/signup")
	@Operation(summary = "소셜 회원가입 (비밀번호 없음, 본인인증·닉네임·약관, 가입 직후 토큰 발급)")
	public ResponseEntity<ApiResponse<SocialSignupResponse>> socialSignup(
			@PathVariable String provider,
			@Valid @RequestBody SocialSignupRequest request
	) {
		LoginType loginType = SocialProviderParser.parse(provider);
		var result = socialSignupUseCase.signup(loginType, new SocialSignupUseCase.SocialSignupCommand(
				request.authorizationCode(),
				request.redirectUri(),
				request.nickname(),
				request.profileImage(),
				request.profileIntro(),
				request.phoneNumber(),
				request.agreements().stream()
						.map(item -> new SocialSignupUseCase.AgreementItem(item.termUuid(), Boolean.TRUE.equals(item.agreed())))
						.toList()
		));
		var tokens = result.tokens();
		ResponseCookie cookie = RefreshCookieWriter.write(tokens.refreshToken(), refreshCookieProperties, jwtProperties);
		return ResponseEntity.status(HttpStatus.CREATED)
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(ApiResponse.success(new SocialSignupResponse(
						result.memberUuid(),
						result.email(),
						result.nickname(),
						result.createdAt(),
						"Bearer",
						tokens.accessToken(),
						tokens.accessTokenExpiresIn(),
						new SocialSignupResponse.TokenUser(
								tokens.memberUuid().toString(),
								tokens.roles(),
								tokens.scopes()
						)
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

	@GetMapping("/terms/{termUuid}")
	@Operation(summary = "약관 상세 조회")
	public ResponseEntity<ApiResponse<TermDetailResponse>> getTermDetail(
			@PathVariable UUID termUuid
	) {
		var term = getTermDetailUseCase.get(termUuid);
		return ResponseEntity.ok(ApiResponse.success(new TermDetailResponse(
				term.getTermUuid(),
				term.getTitle(),
				term.getTermType(),
				term.getVersion(),
				term.getContent(),
				term.isRequired(),
				term.isActive()
		)));
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

	private ResponseEntity<ApiResponse<TokenResponse>> tokenResponse(LocalLoginUseCase.AuthTokenResult result) {
		ResponseCookie cookie = RefreshCookieWriter.write(result.refreshToken(), refreshCookieProperties, jwtProperties);
		TokenResponse body = new TokenResponse(
				"Bearer",
				result.accessToken(),
				result.accessTokenExpiresIn(),
				new TokenResponse.TokenUser(
						result.memberUuid().toString(),
						result.roles(),
						result.scopes()
				)
		);
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(ApiResponse.success(body));
	}
}
