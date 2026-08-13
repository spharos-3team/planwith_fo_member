package com.planwith.planwith_fo_member.application.port.in;

import java.util.UUID;

public interface GetPublicProfileUseCase {

	GetMyProfileUseCase.ProfileResult getPublic(UUID memberUuid);
}
