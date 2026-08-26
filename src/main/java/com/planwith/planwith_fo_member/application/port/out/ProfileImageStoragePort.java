package com.planwith.planwith_fo_member.application.port.out;

import java.util.Optional;
import java.util.UUID;

public interface ProfileImageStoragePort {

	void save(UUID memberUuid, String contentType, byte[] bytes);

	Optional<StoredProfileImage> find(UUID memberUuid);

	record StoredProfileImage(String contentType, byte[] bytes) {
	}
}
