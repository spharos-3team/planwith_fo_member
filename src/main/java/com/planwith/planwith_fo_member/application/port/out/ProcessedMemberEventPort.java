package com.planwith.planwith_fo_member.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface ProcessedMemberEventPort {

	boolean existsByEventUuid(UUID eventUuid);

	void save(UUID eventUuid, UUID memberUuid, Instant processedAt);
}
