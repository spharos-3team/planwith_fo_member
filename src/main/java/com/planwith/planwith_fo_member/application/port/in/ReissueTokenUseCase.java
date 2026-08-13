package com.planwith.planwith_fo_member.application.port.in;

import com.planwith.planwith_fo_member.application.port.in.LocalLoginUseCase.AuthTokenResult;

public interface ReissueTokenUseCase {

	AuthTokenResult reissue(String refreshToken);
}
