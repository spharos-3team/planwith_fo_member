package com.planwith.planwith_fo_member.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DeviceInfoTest {

	@Test
	void unknownWhenUserAgentMissing() {
		assertThat(DeviceInfo.fromUserAgent(null)).isEqualTo(DeviceInfo.UNKNOWN);
		assertThat(DeviceInfo.fromUserAgent("  ")).isEqualTo(DeviceInfo.UNKNOWN);
	}

	@Test
	void detectsMobile() {
		assertThat(DeviceInfo.fromUserAgent(
				"Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15"
		)).isEqualTo(DeviceInfo.MOBILE);
		assertThat(DeviceInfo.fromUserAgent(
				"Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 Mobile Safari/537.36"
		)).isEqualTo(DeviceInfo.MOBILE);
	}

	@Test
	void detectsTablet() {
		assertThat(DeviceInfo.fromUserAgent(
				"Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) AppleWebKit/605.1.15"
		)).isEqualTo(DeviceInfo.TABLET);
		assertThat(DeviceInfo.fromUserAgent(
				"Mozilla/5.0 (Linux; Android 13; SM-X810) AppleWebKit/537.36 Safari/537.36"
		)).isEqualTo(DeviceInfo.TABLET);
	}

	@Test
	void detectsDesktop() {
		assertThat(DeviceInfo.fromUserAgent(
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0"
		)).isEqualTo(DeviceInfo.DESKTOP);
		assertThat(DeviceInfo.fromUserAgent(
				"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36"
		)).isEqualTo(DeviceInfo.DESKTOP);
	}

	@Test
	void unknownWhenUnrecognized() {
		assertThat(DeviceInfo.fromUserAgent("curl/8.5.0")).isEqualTo(DeviceInfo.UNKNOWN);
	}
}
