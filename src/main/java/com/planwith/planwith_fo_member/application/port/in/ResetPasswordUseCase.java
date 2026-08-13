package com.planwith.planwith_fo_member.application.port.in;

public interface ResetPasswordUseCase {

	void reset(String email, String code, String newPassword);
}
