package com.planwith.planwith_fo_member.application.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ErrorCodeTest {

	@Test
	void conflictCodesUseHttp409() {
		assertThat(ErrorCode.EMAIL_ALREADY_EXISTS.status()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(ErrorCode.NICKNAME_ALREADY_EXISTS.status()).isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	void validationAndBusinessCodesUseHttp400() {
		assertThat(ErrorCode.INVALID_REQUEST.status()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(ErrorCode.EMAIL_NOT_VERIFIED.status()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(ErrorCode.PHONE_NOT_VERIFIED.status()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(ErrorCode.REQUIRED_TERM_NOT_AGREED.status()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
}
