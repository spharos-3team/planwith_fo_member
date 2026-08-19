package com.planwith.planwith_fo_member.application.port.in;

import java.util.UUID;

public interface ListFollowingsUseCase {

	ListFollowersUseCase.PagedProfiles listFollowings(UUID memberUuid, int page, int size);
}
