package com.planwith.planwith_fo_member.application.port.out;

import java.util.List;

import com.planwith.planwith_fo_member.domain.auth.ActorType;
import com.planwith.planwith_fo_member.domain.auth.LoginHistory;

public interface LoginHistoryRepositoryPort {

	LoginHistory save(LoginHistory loginHistory);

	List<LoginHistory> findByActor(ActorType actorType, Long actorId);
}
