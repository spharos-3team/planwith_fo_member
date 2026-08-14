package com.planwith.planwith_fo_member.application.port.in;

import java.util.List;
import java.util.UUID;

public interface UpdateMyPageUseCase {

	UpdateMyPageResult update(UUID memberUuid, UpdateMyPageCommand command);

	record AgreementItem(UUID termUuid, boolean agreed) {
	}

	record UpdateMyPageCommand(
			String phoneNumber,
			String name,
			String nickname,
			String profileImage,
			String profileIntro,
			List<AgreementItem> agreements,
			String currentPassword,
			String newPassword
	) {
	}

	record UpdateMyPageResult(
			GetMyMemberUseCase.MyMemberResult member,
			GetMyProfileUseCase.ProfileResult profile,
			List<MemberAgreementUseCase.AgreementView> agreements
	) {
	}
}
