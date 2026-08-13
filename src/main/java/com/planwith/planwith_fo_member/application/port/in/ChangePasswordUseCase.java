package com.planwith.planwith_fo_member.application.port.in;

import java.util.UUID;

public interface ChangePasswordUseCase {

	void change(UUID memberUuid, String currentPassword, String newPassword);
}
