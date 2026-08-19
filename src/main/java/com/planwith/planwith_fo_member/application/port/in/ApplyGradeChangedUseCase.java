package com.planwith.planwith_fo_member.application.port.in;

public interface ApplyGradeChangedUseCase {

	void apply(GradeChangedCommand command);

	record GradeChangedCommand(
			String eventUuid,
			String memberUuid,
			String currentGradeCode,
			boolean profileBadge,
			boolean profileSpecialBorder
	) {
	}
}
