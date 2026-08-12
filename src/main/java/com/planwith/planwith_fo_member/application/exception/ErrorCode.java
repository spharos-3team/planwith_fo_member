package com.planwith.planwith_fo_member.application.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청값이 올바르지 않습니다."),
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
	FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."),
	TERM_NOT_FOUND(HttpStatus.NOT_FOUND, "TERM_NOT_FOUND", "약관을 찾을 수 없습니다."),
	EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 사용 중인 이메일입니다."),
	NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "NICKNAME_ALREADY_EXISTS", "이미 사용 중인 닉네임입니다."),
	EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다."),
	EMAIL_VERIFICATION_INVALID(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_INVALID", "인증번호가 올바르지 않습니다."),
	EMAIL_VERIFICATION_EXPIRED(HttpStatus.BAD_REQUEST, "EMAIL_VERIFICATION_EXPIRED", "인증번호가 만료되었습니다."),
	TERM_INACTIVE(HttpStatus.BAD_REQUEST, "TERM_INACTIVE", "비활성화된 약관입니다."),
	REQUIRED_TERM_NOT_AGREED(HttpStatus.BAD_REQUEST, "REQUIRED_TERM_NOT_AGREED", "필수 약관에 동의해야 합니다."),
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
