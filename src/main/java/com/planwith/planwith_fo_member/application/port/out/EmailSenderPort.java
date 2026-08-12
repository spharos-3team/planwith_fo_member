package com.planwith.planwith_fo_member.application.port.out;

public interface EmailSenderPort {

	void sendVerificationCode(String email, String code);
}
