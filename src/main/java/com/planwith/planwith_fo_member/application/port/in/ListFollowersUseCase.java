package com.planwith.planwith_fo_member.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListFollowersUseCase {

	PagedProfiles listFollowers(UUID memberUuid, int page, int size);

	record PagedProfiles(
			List<GetMyProfileUseCase.ProfileResult> content,
			int page,
			int size,
			long totalElements,
			int totalPages
	) {
	}
}
