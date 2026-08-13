package com.planwith.planwith_fo_member.application.port.in;

public interface CheckNicknameAvailabilityUseCase {

	NicknameAvailabilityResult check(String nickname);

	record NicknameAvailabilityResult(String nickname, boolean available) {
	}
}
