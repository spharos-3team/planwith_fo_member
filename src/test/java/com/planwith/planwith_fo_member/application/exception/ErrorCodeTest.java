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
		assertThat(ErrorCode.PASSWORD_CHANGE_NOT_ALLOWED_FOR_SOCIAL.status()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(ErrorCode.REQUIRED_TERM_NOT_MODIFIABLE.status()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(ErrorCode.INVALID_PROFILE_IMAGE.status()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(ErrorCode.CANNOT_FOLLOW_SELF.status()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void missingResourceCodesUseHttp404() {
		assertThat(ErrorCode.MEMBER_NOT_FOUND.status()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(ErrorCode.PROFILE_IMAGE_NOT_FOUND.status()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void emailSendFailedUsesHttp500() {
		assertThat(ErrorCode.EMAIL_SEND_FAILED.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
