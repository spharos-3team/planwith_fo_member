package com.planwith.planwith_fo_member.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.planwith.planwith_fo_member.application.port.out.ClientRequestInfoPort;

class HttpClientRequestInfoAdapterTest {

	private final HttpClientRequestInfoAdapter adapter = new HttpClientRequestInfoAdapter();

	@AfterEach
	void tearDown() {
		RequestContextHolder.resetRequestAttributes();
	}

	@Test
	void emptyWhenNoRequest() {
		RequestContextHolder.resetRequestAttributes();
		ClientRequestInfoPort.ClientRequestInfo info = adapter.current();
		assertThat(info.ipAddress()).isNull();
		assertThat(info.userAgent()).isNull();
	}

	@Test
	void prefersForwardedForAndTruncatesUserAgent() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");
		request.addHeader("X-Real-IP", "10.0.0.1");
		request.setRemoteAddr("127.0.0.1");
		request.addHeader("User-Agent", "A".repeat(520));
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		ClientRequestInfoPort.ClientRequestInfo info = adapter.current();

		assertThat(info.ipAddress()).isEqualTo("203.0.113.10");
		assertThat(info.userAgent()).hasSize(500);
	}
}
