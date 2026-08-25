package com.planwith.planwith_fo_member.adapter.in.web;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.planwith.planwith_fo_member.application.port.out.ClientRequestInfoPort;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class HttpClientRequestInfoAdapter implements ClientRequestInfoPort {

	private static final String HEADER_FORWARDED_FOR = "X-Forwarded-For";
	private static final String HEADER_REAL_IP = "X-Real-IP";
	private static final int IP_MAX_LENGTH = 45;
	private static final int USER_AGENT_MAX_LENGTH = 500;

	@Override
	public ClientRequestInfo current() {
		if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
			return new ClientRequestInfo(null, null);
		}
		HttpServletRequest request = attributes.getRequest();
		return new ClientRequestInfo(
				truncate(resolveIp(request), IP_MAX_LENGTH),
				truncate(request.getHeader("User-Agent"), USER_AGENT_MAX_LENGTH)
		);
	}

	private String resolveIp(HttpServletRequest request) {
		String forwardedFor = firstClientIp(request.getHeader(HEADER_FORWARDED_FOR));
		if (StringUtils.hasText(forwardedFor)) {
			return forwardedFor;
		}
		String realIp = blankToNull(request.getHeader(HEADER_REAL_IP));
		if (realIp != null) {
			return realIp;
		}
		return blankToNull(request.getRemoteAddr());
	}

	private String firstClientIp(String forwardedFor) {
		if (!StringUtils.hasText(forwardedFor)) {
			return null;
		}
		for (String candidate : forwardedFor.split(",")) {
			String ip = blankToNull(candidate);
			if (ip != null && !"unknown".equalsIgnoreCase(ip)) {
				return ip;
			}
		}
		return null;
	}

	private String blankToNull(String value) {
		if (!StringUtils.hasText(value)) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private String truncate(String value, int maxLength) {
		if (value == null) {
			return null;
		}
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength);
	}
}
