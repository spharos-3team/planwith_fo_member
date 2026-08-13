package com.planwith.planwith_fo_member.adapter.in.web;

import org.springframework.http.ResponseCookie;

import com.planwith.planwith_fo_member.config.JwtProperties;
import com.planwith.planwith_fo_member.config.RefreshCookieProperties;

final class RefreshCookieWriter {

	private RefreshCookieWriter() {
	}

	static ResponseCookie write(String rawToken, RefreshCookieProperties cookieProperties, JwtProperties jwtProperties) {
		ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieProperties.name(), rawToken)
				.httpOnly(true)
				.secure(cookieProperties.secure())
				.path(cookieProperties.path())
				.maxAge(jwtProperties.refreshTokenTtl())
				.sameSite(cookieProperties.sameSite());
		if (cookieProperties.domain() != null && !cookieProperties.domain().isBlank()) {
			builder.domain(cookieProperties.domain());
		}
		return builder.build();
	}

	static ResponseCookie clear(RefreshCookieProperties cookieProperties) {
		ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieProperties.name(), "")
				.httpOnly(true)
				.secure(cookieProperties.secure())
				.path(cookieProperties.path())
				.maxAge(0)
				.sameSite(cookieProperties.sameSite());
		if (cookieProperties.domain() != null && !cookieProperties.domain().isBlank()) {
			builder.domain(cookieProperties.domain());
		}
		return builder.build();
	}
}
