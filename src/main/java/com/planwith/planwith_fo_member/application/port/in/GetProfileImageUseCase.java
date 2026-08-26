package com.planwith.planwith_fo_member.application.port.in;

import java.util.UUID;

public interface GetProfileImageUseCase {

	Result get(UUID memberUuid);

	record Result(String contentType, byte[] bytes) {
	}
}
