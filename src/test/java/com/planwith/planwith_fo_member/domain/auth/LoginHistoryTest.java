package com.planwith.planwith_fo_member.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LoginHistoryTest {

	@Test
	void userHistoryDerivesDeviceFromUserAgent() {
		LoginHistory history = LoginHistory.user(
				12L,
				"203.0.113.10",
				"Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)"
		);

		assertThat(history.getActorId()).isEqualTo(12L);
		assertThat(history.getActorType()).isEqualTo(ActorType.USER);
		assertThat(history.getIpAddress()).isEqualTo("203.0.113.10");
		assertThat(history.getDeviceInfo()).isEqualTo(DeviceInfo.MOBILE);
		assertThat(history.getCreatedAt()).isNotNull();
	}

	@Test
	void actorIdIsRequired() {
		assertThatThrownBy(() -> LoginHistory.user(null, "127.0.0.1", "ua"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("actorId is required");
	}
}
