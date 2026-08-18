package com.planwith.planwith_fo_member.application.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청값이 올바르지 않습니다."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh Token이 유효하지 않습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."),
	TERM_NOT_FOUND(HttpStatus.NOT_FOUND, "TERM_NOT_FOUND", "약관을 찾을 수 없습니다."),
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다."),
	NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "NICKNAME_ALREADY_EXISTS", "이미 사용 중인 닉네임입니다."),
	EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다."),
	EMAIL_VERIFICATION_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_INVALID", "인증번호가 올바르지 않습니다."),
	EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_EXPIRED", "인증번호가 만료되었습니다."),
	PHONE_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "PHONE_NOT_VERIFIED", "본인인증이 완료되지 않았습니다."),
	PHONE_MISMATCH(HttpStatus.BAD_REQUEST, "PHONE_MISMATCH", "본인인증된 휴대폰 번호와 일치하지 않습니다."),
	NAME_MISMATCH(HttpStatus.BAD_REQUEST, "NAME_MISMATCH", "본인인증된 실명과 일치하지 않습니다."),
	PHONE_VERIFICATION_FAILED(HttpStatus.BAD_REQUEST, "PHONE_VERIFICATION_FAILED", "본인인증에 실패했습니다."),
	EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_SEND_FAILED", "이메일 발송에 실패했습니다."),
	IDENTITY_VERIFICATION_CONFIG_MISSING(HttpStatus.INTERNAL_SERVER_ERROR, "IDENTITY_VERIFICATION_CONFIG_MISSING", "본인인증 설정이 누락되었습니다."),
	TERM_INACTIVE(HttpStatus.BAD_REQUEST, "TERM_INACTIVE", "비활성화된 약관입니다."),
	REQUIRED_TERM_NOT_AGREED(HttpStatus.BAD_REQUEST, "REQUIRED_TERM_NOT_AGREED", "필수 약관에 동의해야 합니다."),
	UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "UNSUPPORTED_PROVIDER", "지원하지 않는 소셜 로그인 제공자입니다."),
	SOCIAL_AUTH_FAILED(HttpStatus.BAD_REQUEST, "SOCIAL_AUTH_FAILED", "소셜 인증에 실패했습니다."),
	SOCIAL_ACCOUNT_ALREADY_EXISTS(HttpStatus.CONFLICT, "SOCIAL_ACCOUNT_ALREADY_EXISTS", "이미 가입된 소셜 계정입니다."),
	SOCIAL_EMAIL_REQUIRED(HttpStatus.BAD_REQUEST, "SOCIAL_EMAIL_REQUIRED", "소셜 계정 이메일 정보가 필요합니다."),
	PASSWORD_RESET_NOT_ALLOWED_FOR_SOCIAL(HttpStatus.BAD_REQUEST, "PASSWORD_RESET_NOT_ALLOWED_FOR_SOCIAL", "소셜 로그인 계정은 비밀번호를 재설정할 수 없습니다."),
	PASSWORD_CHANGE_NOT_ALLOWED_FOR_SOCIAL(HttpStatus.BAD_REQUEST, "PASSWORD_CHANGE_NOT_ALLOWED_FOR_SOCIAL", "소셜 로그인 계정은 비밀번호를 변경할 수 없습니다."),
	REQUIRED_TERM_NOT_MODIFIABLE(HttpStatus.BAD_REQUEST, "REQUIRED_TERM_NOT_MODIFIABLE", "필수 약관 동의는 변경할 수 없습니다."),
	INVALID_PROFILE_IMAGE(HttpStatus.BAD_REQUEST, "INVALID_PROFILE_IMAGE", "프로필 이미지 형식이 올바르지 않습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public HttpStatus status() {
		return status;
	}

	public String code() {
		return code;
	}

	public String message() {
		return message;
	}
}
